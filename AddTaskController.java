import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

public class AddTaskController extends dbManipulation implements Initializable , ControlledScreen{
    
    ScreensController myController;
    
    @FXML TableView tblTask;
    @FXML TextField txtTaskName, txtTaskDetails, txtTaskDuration;
    @FXML DatePicker dtpTaskSDate;
    @FXML ComboBox cmbTaskStatus;
    @FXML Button btnAddTask, btnRemoveTask;
    
    fillTable fill;

    //below is the code to share the objects
    Client cl;
    CurrentSPDetails csp;

    String ori_sql = "SELECT * FROM TASK ";
    
    ObservableList<Task> taskObsList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cl = Context.getInstance().currentClient();
        csp = Context.getInstance().currentSPDetails();

        setTask();
        
//        System.out.println(csp.getSID());       
//        System.out.println(csp.getQID());

        
    }    
    

    private void setTask(){
        cmbTaskStatus.setItems(setUpComboBox());
        
        createTaskTable();
        
        btnAddTask.setOnMouseClicked(e1 ->{
            try {
                if(Validation.checkTaskDetails(txtTaskName.getText(),txtTaskDetails.getText(),dtpTaskSDate.getValue(),txtTaskDuration.getText(),cmbTaskStatus.getSelectionModel().getSelectedItem().toString())){
                    //insert into database
                    insertTask();
                    clearTaskTextBoxes();
                }
            } catch (SQLException ex) {
                Logger.getLogger(AddTaskController.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        
             
        btnRemoveTask.setOnMouseClicked(e1 -> {
            int selectedIndex = tblTask.getSelectionModel().getSelectedIndex();
            if (selectedIndex >= 0) {
                Delete("DELETE * FROM task WHERE TID = "+taskObsList.get(selectedIndex).getTaskID());
                insertClientTaskDetails();
            } else {
                Notifications notificationBuilder = Notifications.create()
                    .title("No Task Selected")
                    .text("Please select desired task to be remove.")
                    .graphic(null)
                    .hideAfter(Duration.seconds(5))
                    .position(Pos.TOP_RIGHT);
                                //notificationBuilder.darkStyle();
                    notificationBuilder.showError(); 
            }
            updateTaskTable();
        });


    }
      
    private void insertTask(){
        Insert("INSERT INTO task(QID, task_Name, task_details, startDate, duration, task_status) VALUES('Q"+cl.getCID()+csp.getSID()+"', '"+txtTaskName.getText()+"','"+txtTaskDetails.getText()+"','"+String.valueOf(dtpTaskSDate.getValue())+"',"+txtTaskDuration.getText()+",'"+cmbTaskStatus.getSelectionModel().getSelectedItem().toString()+"')");                                                    
        
        Notifications notificationBuilder = Notifications.create()
        .title("Success")
        .text("Task was created")
        .graphic(null)
        .hideAfter(Duration.seconds(5))
        .position(Pos.TOP_RIGHT);
                    //notificationBuilder.darkStyle();
        notificationBuilder.showInformation();
        
        insertClientTaskDetails();
    }
        
    private void insertClientTaskDetails(){
        try{
            rs = stmt.executeQuery("SELECT * FROM task WHERE QID = 'Q"+cl.getCID()+csp.getSID()+"'");  //CID is actually PID
            taskObsList.clear(); //else when clicking on different sp, it will append rm details to the vector
            while(rs.next()){
                taskObsList.add(new Task(rs.getString("TID"),rs.getString("task_Name"),rs.getString("task_details"),rs.getString("startDate"), rs.getString("duration"), rs.getString("task_status")));                                                   
            }
            updateTaskTable();
            myController.reloadScreen(ScreensFramework.screen4ID, ScreensFramework.screen4File);
        }   catch(SQLException e){}
        
    }
        
  private void createTaskTable(){
        tblTask.setEditable(true);
                       
        TableColumn tnameCol = new TableColumn("Task Name");
        TableColumn tdetailCol = new TableColumn("Task Details");
        TableColumn tdurationCol = new TableColumn("Duration");
        TableColumn tsdateCol = new TableColumn("Start Date");
        TableColumn tstatus = new TableColumn("Status");

                
        tnameCol.setCellValueFactory(new PropertyValueFactory<Task,String>("taskName"));
        tdetailCol.setCellValueFactory(new PropertyValueFactory<Task,String>("taskDetails"));
        tdurationCol.setCellValueFactory(new PropertyValueFactory<Task,String>("taskDuration"));
        tsdateCol.setCellValueFactory(new PropertyValueFactory<Task,String>("taskDate"));
        tstatus.setCellValueFactory(new PropertyValueFactory<Task,String>("taskStatus"));
        
        tnameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        tnameCol.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<Task, String>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<Task, String> t) {
                    if(Validation.checkEmptyString(t.getNewValue())){
                        ((Task) t.getTableView().getItems().get(
                                t.getTablePosition().getRow())
                                ).setTaskName(t.getNewValue());

                        String id = ((Task) t.getTableView().getItems().get(
                        t.getTablePosition().getRow())
                        ).getTaskID();

                        String sql = "UPDATE task SET taskName = '"+t.getNewValue()+"' WHERE TID = "+id+"";
                        Update(sql);
                        myController.reloadScreen(ScreensFramework.screen4ID, ScreensFramework.screen4File);
                        myController.refreshScreen(ScreensFramework.screen3ID, ScreensFramework.screen3File);
                    }
                    tblTask.refresh();
                }
            }
        );
        
        tdetailCol.setCellFactory(TextFieldTableCell.forTableColumn());
        tdetailCol.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<Task, String>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<Task, String> t) {
                    ((Task) t.getTableView().getItems().get(
                            t.getTablePosition().getRow())
                            ).setTaskDetails(t.getNewValue());
                    
                    String id = ((Task) t.getTableView().getItems().get(
                    t.getTablePosition().getRow())
                    ).getTaskID();
                    
                    String sql = "UPDATE task SET taskDetails = '"+t.getNewValue()+"' WHERE TID = "+id+"";
                    Update(sql);
                    myController.reloadScreen(ScreensFramework.screen4ID, ScreensFramework.screen4File);
                    myController.refreshScreen(ScreensFramework.screen3ID, ScreensFramework.screen3File);
                    tblTask.refresh();
                }
                
            }
        );
        
        tsdateCol.setCellFactory(TextFieldTableCell.forTableColumn());
        tsdateCol.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<Task, String>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<Task, String> t) {
//                    if(Validation.checkdatewithtempsDate(t.getNewValue())){
                        ((Task) t.getTableView().getItems().get(
                                t.getTablePosition().getRow())
                                ).setTaskDate(t.getNewValue());

                        String id = ((Task) t.getTableView().getItems().get(
                        t.getTablePosition().getRow())
                        ).getTaskID();
                    

                        String sql = "UPDATE task SET taskDate = '"+t.getNewValue()+"' WHERE TID = "+id+"";
                        Update(sql);
                        myController.reloadScreen(ScreensFramework.screen4ID, ScreensFramework.screen4File);
                        myController.refreshScreen(ScreensFramework.screen3ID, ScreensFramework.screen3File);
//                    }
                        tblTask.refresh();
                }
            }
        );
        
        tdurationCol.setCellFactory(TextFieldTableCell.forTableColumn());
        tdurationCol.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<Task, String>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<Task, String> t) {
                    if(Validation.checkInteger(t.getNewValue())){
                        ((Task) t.getTableView().getItems().get(
                                t.getTablePosition().getRow())
                                ).setTaskDuration(t.getNewValue());

                        String id = ((Task) t.getTableView().getItems().get(
                        t.getTablePosition().getRow())
                        ).getTaskID();
                    

                        String sql = "UPDATE task SET taskDuration = '"+t.getNewValue()+"' WHERE TID = "+id+"";
                        Update(sql);
                        myController.reloadScreen(ScreensFramework.screen4ID, ScreensFramework.screen4File);
                        myController.refreshScreen(ScreensFramework.screen3ID, ScreensFramework.screen3File);
                    }
                    tblTask.refresh();
                }
            }
        );
                
        tstatus.setCellFactory(ComboBoxTableCell.forTableColumn(setUpComboBox()));
        tstatus.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<Task, String>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<Task, String> t) {
                    if(Validation.checkTaskStatus(t.getNewValue())){
                        ((Task) t.getTableView().getItems().get(
                                t.getTablePosition().getRow())
                                ).setTaskStatus(t.getNewValue());

                        String id = ((Task) t.getTableView().getItems().get(
                        t.getTablePosition().getRow())
                        ).getTaskID();
                    
                    
                        String sql = "UPDATE task SET task_status = '"+t.getNewValue()+"' WHERE TID = "+id+"";
                        Update(sql);
                        myController.reloadScreen(ScreensFramework.screen4ID, ScreensFramework.screen4File);
                        myController.refreshScreen(ScreensFramework.screen3ID, ScreensFramework.screen3File);
                    }
                    tblTask.refresh();
                }
            }
        );
                
                
        tnameCol.setMinWidth(100);
        tdetailCol.setMinWidth(100);
        tdurationCol.setMinWidth(100);
        tsdateCol.setMinWidth(150);       
        tstatus.setMinWidth(150); 
           
        fillTask();
        tblTask.setItems(taskObsList);
        tblTask.getColumns().addAll(tnameCol,tdetailCol,tsdateCol,tdurationCol,tstatus);
    }
    
    private void fillTask(){
        taskObsList.clear();
        try{
            rs = stmt.executeQuery("SELECT * FROM TASK WHERE QID ='"+csp.getQID()+"'");
            while(rs.next()){
                taskObsList.add(new Task(String.valueOf(rs.getInt("TID")),rs.getString("task_Name"), rs.getString("task_details"), String.valueOf(rs.getDate("startDate")), String.valueOf(rs.getInt("duration")), rs.getString("task_status") ));
            }
        }   catch(SQLException e){}
        
    }
  
    private void updateTaskTable(){
        tblTask.setItems(taskObsList);
    }
    
    private  ObservableList<Object>  setUpComboBox(){
        ObservableList<Object> data = FXCollections.observableArrayList();
        data.add("Pending");
        data.add("Ongoing");
        data.add("Completed");
        data.add("Verified");
        
        cmbTaskStatus.setItems(data);
        cmbTaskStatus.setPromptText("Select Status");
        
        return data;
    }
    
    private void clearTaskTextBoxes() {
        txtTaskName.setText("");
        txtTaskDetails.setText("");
        txtTaskDuration.setText("");
        
        dtpTaskSDate.setPromptText("");
    }
    
    
    public void setScreenParent(ScreensController screenParent){
        myController = screenParent;
    }
}
