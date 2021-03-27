import java.io.IOException;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import java.net.URL;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;


public class SP_Side_ClientController extends dbManipulation implements Initializable, ControlledScreen{

    ScreensController myController;
    
    
    @FXML TableView tblClient, tblTask;
    @FXML TextField txtTaskName, txtTaskDetails, txtTaskDuration;
    @FXML DatePicker dtpTaskSDate;
    @FXML ComboBox cmbTaskStatus;
    @FXML Button btnAddTask, btnRemoveTask, btnRemoveClient, btnClientPhotos;
    
    fillTable fill;
    
    ObservableList<Task> taskObsList = FXCollections.observableArrayList();
    ObservableList<RMaterial> rmObsList = FXCollections.observableArrayList();
    //extend deadline
    @FXML TextField txtClientName, txtStartDate, txtDuration, txtNumDaysExtend;
    @FXML Button btnReqExtension;
    
    @FXML DatePicker dtpDateStart;
    @FXML TextField txtDuration1, txtLCost ,txtItemName , txtItemPrice, txtItemQuantity;
    @FXML TableView tblRawMaterial;
    @FXML Button btnAddRM, btnRemoveRM;
    
    @FXML Button btnEditQuo;
    
    Client cl;  //PID
    CurrentSPDetails csp;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cl = Context.getInstance().currentClient(); //PID
        csp = Context.getInstance().currentSPDetails();
        
        setClient();
        setRM();
    }    
    
    private void setClient(){
//        fill=new fillTable(tblClient, "SELECT * FROM userclient WHERE CID IN (SELECT CID FROM selected_sp WHERE quo_status = 'ACCEPTED' AND SID = "+csp.getSID()+")");
        fill=new fillTable(tblClient, "SELECT PID AS ProjectID, proj_Name AS ProjectName, cName AS Client, p.address,telephone, email,  no_floors AS NumberOfFloors, temp_StartDate \n" +
        "FROM project p, userclient c WHERE p.CID=c.CID AND PID IN (SELECT CID FROM selected_sp WHERE quo_status = 'ACCEPTED' AND SID = "+csp.getSID()+")");
        
        tblClient.setOnMouseClicked(e -> {
            if (tblClient.getSelectionModel().getSelectedItem() != null) {
                String selectedIndex;
                selectedIndex = tblClient.getSelectionModel().getSelectedItem().toString(); //get SELECTED ROW
                selectedIndex = selectedIndex.substring(1, selectedIndex.indexOf(",")); //GET ID OF SELECTED ROW
                
                cl.setCID(Integer.valueOf(selectedIndex));
                
                insertClientTaskDetails();
                insertClientRMDetails();
                insertExtendDeadline();
                setDeleteClient();

                btnClientPhotos.setOnMouseClicked(e2 ->{
                    try {
                        AnchorPane root = (AnchorPane) FXMLLoader.load(getClass().getResource("SP_Side_ClientPhotos.fxml"));
                        Stage secondStage = new Stage();
                        secondStage.setScene(new Scene(root));
                        secondStage.setTitle("Photos");
                        secondStage.show();

                        secondStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                            public void handle(WindowEvent we) {
//                                myController.reloadScreen(ScreensFramework.SP_Side_ClientPhotoID,ScreensFramework.SP_Side_ClientPhotoFile);
                            }
                        }); 
                    } catch (IOException ex) {
                        Logger.getLogger(SP_ScreenController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                });
                
                btnEditQuo.setOnMouseClicked(e1 -> {
                    if(Validation.checkSPQuoEdit(dtpDateStart.getValue(),txtDuration1.getText(),txtLCost.getText())){
                        String sql = "UPDATE quotation SET startDate = '"+dtpDateStart.getValue()+"' , Duration = "+txtDuration1.getText()+", LabourCost = "+txtLCost.getText()+"  WHERE QID = 'Q"+cl.getCID()+csp.getSID()+"'";
                        Update(sql);
                    }
                });
                setTask();
            }     
            
        });
    }
    
    private void insertExtendDeadline(){
        try {
            rs = stmt.executeQuery("SELECT * FROM quotation WHERE QID IN (SELECT QID FROM selected_sp WHERE quo_status = 'ACCEPTED' AND QID = 'Q"+cl.getCID()+csp.getSID()+"')");
            while(rs.next()){
                txtStartDate.setText(String.valueOf(rs.getDate("startDate")));
                txtDuration.setText(String.valueOf(rs.getInt("Duration")));
                
                dtpDateStart.setValue(rs.getDate("startDate").toLocalDate());
                txtDuration1.setText(String.valueOf(rs.getInt("Duration")));
                txtLCost.setText(String.valueOf(rs.getInt("LabourCost")));
            }
            
            btnReqExtension.setOnMouseClicked(e1 -> {
                int selectedIndex = tblClient.getSelectionModel().getSelectedIndex();
                if (selectedIndex >= 0) {
                    if(txtNumDaysExtend.getText().equals("")){
                        Notifications notificationBuilder = Notifications.create()
                            .title("Error")
                            .text("Number of days to extend is empty")
                            .graphic(null)
                            .hideAfter(Duration.seconds(3))
                            .position(Pos.TOP_RIGHT);
                            //notificationBuilder.darkStyle();
                            notificationBuilder.showError(); 
                    }else{
                        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                        Date dateobj = new Date();
                        
                        Insert("INSERT INTO notification(notificationType, receiverType, senderID, receiverID, details, dateSent) VALUES('Extension','client',"+csp.getSID()+","+cl.getCID()+",'"+ txtNumDaysExtend.getText()+"','"+df.format(dateobj)+"')");
                        
            
                        String spName="";
                        try {
                            rs = stmt.executeQuery("SELECT * FROM serviceprovider WHERE SID ="+csp.getSID()+"");
                            while(rs.next()){
                                spName = rs.getString("sp_Name");
                            }
                        } catch (SQLException ex) {
                            Logger.getLogger(SP_Side_ClientController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        
                        String rec ="";
                        try {
                            rs = stmt.executeQuery("SELECT email FROM userclient c, project p WHERE c.CID = p.CID AND PID ="+cl.getCID()+"");
                            while(rs.next()){
                                rec = rs.getString("email");
                            }
                        } catch (SQLException ex) {
                            Logger.getLogger(SP_Side_ClientController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        if(!rec.equals("")){
                            String[] to ={rec};
                            String subject = "Extension";
                            String body ="Service provider "+spName+" has requested extension of "+txtNumDaysExtend.getText()+" days. \n Enter your home construction planner to accept or reject.";
                           
                            ExecutorService emailExecutor = Executors.newSingleThreadExecutor();
                            emailExecutor.execute(new Runnable() {
                                @Override
                                public void run() {
                                    SendMail.sendFromGMail(to, subject, body);
                                }
                            });
                            emailExecutor.shutdown();
                           
                        }  
                        txtNumDaysExtend.setText("");
                        Notifications notificationBuilder = Notifications.create()
                        .title("Extensions")
                        .text("Request for extensions was sent to client.")
                        .graphic(null)
                        .hideAfter(Duration.seconds(3))
                        .position(Pos.TOP_RIGHT);
                        //notificationBuilder.darkStyle();
                        notificationBuilder.showInformation(); 
                    }
                } else {
                    Notifications notificationBuilder = Notifications.create()
                        .title("No Client Selected")
                        .text("Please select desired task to be remove.")
                        .graphic(null)
                        .hideAfter(Duration.seconds(3))
                        .position(Pos.TOP_RIGHT);
                                    //notificationBuilder.darkStyle();
                        notificationBuilder.showError(); 
                }
           
            });
            
        } catch (SQLException ex) {
            Logger.getLogger(SP_Side_ClientController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // ---------task----------
    private void insertClientTaskDetails(){
        try{
            rs = stmt.executeQuery("SELECT * FROM task WHERE QID = 'Q"+cl.getCID()+csp.getSID()+"'");  //CID is actually PID
            taskObsList.clear(); //else when clicking on different sp, it will append rm details to the vector
            while(rs.next()){
                taskObsList.add(new Task(rs.getString("TID"),rs.getString("task_Name"),rs.getString("task_details"), rs.getString("startDate"),rs.getString("duration"),  rs.getString("task_status")));                                                   
            }
            updateTaskTable();
        }   catch(SQLException e){}
        
    }
     
    private void setTask(){
        cmbTaskStatus.setItems(setUpComboBox());
        
        createTaskTable();
        btnAddTask.setOnMouseClicked(e1 ->{
            try {
                //insert into database
                insertTask();
            } catch (SQLException ex) {
                Logger.getLogger(SP_Side_ClientController.class.getName()).log(Level.SEVERE, null, ex);
            }
            clearTaskTextBoxes();
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
    private void setDeleteClient(){
        
         btnRemoveClient.setOnMouseClicked(e1 ->{
            
            int countSP=1;
            try{
      //          System.out.println("SELECT COUNT(QID) AS count FROM selected_sp WHERE status='ACCEPTED' AND CID="+cl.getCID()+" AND SID IN (SELECT SID FROM serviceprovider WHERE sp_Type='"+csp.getSpType()+"')");  
                rs = stmt.executeQuery("SELECT COUNT(QID) AS count FROM selected_sp WHERE quo_status='ACCEPTED' AND CID="+cl.getCID()+" AND SID IN (SELECT SID FROM serviceprovider WHERE sp_Type='"+csp.getSpType()+"')");
                while(rs.next()){
                    countSP = rs.getInt("count");
                    //System.out.println("COUNT: "+countSP);
                }
            }catch(SQLException e2){}  
            
            if(countSP == 1){
                Update("UPDATE TempStartDate SET exist=0 WHERE PID = "+cl.getCID()+" AND sp_type='"+csp.getSpType()+"'");
            }
              
            try{
                boolean getTempDate=FALSE;
                //System.out.println("SELECT task_status FROM task WHERE QID=(SELECT QID FROM selected_sp WHERE CID="+cl.getCID()+" AND SID="+csp.getSID()+")");
                rs = stmt.executeQuery("SELECT task_status FROM task WHERE QID=(SELECT QID FROM selected_sp WHERE CID="+cl.getCID()+" AND SID="+csp.getSID()+")");
                while(rs.next()){
                    if(getTempDate == TRUE) break;
                    String status = rs.getString("task_status");

                    if(status.compareTo("Ongoing")==0){
                        getTempDate=TRUE;
                        LocalDate tomorrow = LocalDate.now().plusDays(1);
                        
                        Update("UPDATE TempStartDate SET startDate='"+tomorrow+"' WHERE PID = "+cl.getCID()+" AND sp_type ='"+csp.getSpType()+"'");
                    }
                }
                if(csp.getSpType().compareTo("Masonry")==0){
                    Update("UPDATE TempStartDate SET startDate=null WHERE PID = "+cl.getCID()+" AND sp_type IN ('Roof-Concrete', 'Roof-Formwork')");
                }
            }catch(SQLException e3){}
           
            Delete("DELETE FROM task WHERE task_status IN('Ongoing','Pending') AND QID = (SELECT QID FROM selected_sp WHERE CID="+cl.getCID()+" AND SID="+csp.getSID()+")");
            Update("UPDATE selected_sp SET quo_status='DECLINED' WHERE QID= (SELECT QID FROM selected_sp WHERE CID="+cl.getCID()+" AND SID="+csp.getSID()+")");
                  
            Notifications notificationBuilder = Notifications.create()
                .title("Success")
                .text("Client was successfully deleted.")
                .graphic(null)
                .hideAfter(Duration.seconds(5))
                .position(Pos.TOP_RIGHT);
                            //notificationBuilder.darkStyle();
                notificationBuilder.showInformation(); 
        });
    }

    
    private void insertTask() throws SQLException{
        if(Validation.checkTaskDetails(txtTaskName.getText(),txtTaskDetails.getText(),dtpTaskSDate.getValue(),txtTaskDuration.getText(),cmbTaskStatus.getSelectionModel().getSelectedItem().toString())){
            Insert("INSERT INTO task(QID, task_Name, task_details, startDate, duration, task_status) VALUES('Q"+cl.getCID()+csp.getSID()+"', '"+txtTaskName.getText()+"','"+txtTaskDetails.getText()+",'"+String.valueOf(dtpTaskSDate.getValue())+"','"+txtTaskDuration.getText()+" , "+cmbTaskStatus.getSelectionModel().getSelectedItem().toString()+"')");                                                    
            insertClientTaskDetails();
        }
    }
    
    private void updateTaskTable(){
        tblTask.setItems(taskObsList);
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
                }
            }
        );
        
//        tdurationCol.setCellFactory(TextFieldTableCell.forTableColumn());
//        tdurationCol.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<Task, String>>() {
//                @Override
//                public void handle(TableColumn.CellEditEvent<Task, String> t) {
//                    ((Task) t.getTableView().getItems().get(
//                            t.getTablePosition().getRow())
//                            ).setTaskDuration(t.getNewValue());
//                    
//                    String id = ((Task) t.getTableView().getItems().get(
//                    t.getTablePosition().getRow())
//                    ).getTaskID();
//                    
//                    String sql = "UPDATE task SET taskDuration = '"+t.getNewValue()+"' WHERE TID = "+id+"";
//                    Update(sql);
//                }
//            }
//        );
        

//        tsdateCol.setCellFactory(TextFieldTableCell.forTableColumn());
//        tsdateCol.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<Task, String>>() {
//                @Override
//                public void handle(TableColumn.CellEditEvent<Task, String> t) {
//                    ((Task) t.getTableView().getItems().get(
//                            t.getTablePosition().getRow())
//                            ).setTaskDate(t.getNewValue());
//                    
//                    String id = ((Task) t.getTableView().getItems().get(
//                    t.getTablePosition().getRow())
//                    ).getTaskID();
//                    
//                    String sql = "UPDATE task SET taskDate = '"+t.getNewValue()+"' WHERE TID = "+id+"";
//                    Update(sql);
//                }
//            }
//        );
        
        tstatus.setCellFactory(ComboBoxTableCell.forTableColumn(setUpComboBox()));
        tstatus.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<Task, String>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<Task, String> t) {
                    if(Validation.validateChangeTStatus(t.getOldValue(), t.getNewValue())){
                        ((Task) t.getTableView().getItems().get(
                                t.getTablePosition().getRow())
                                ).setTaskStatus(t.getNewValue());

                        String id = ((Task) t.getTableView().getItems().get(
                        t.getTablePosition().getRow())
                        ).getTaskID();

                        String sql = "UPDATE task SET task_status = '"+t.getNewValue()+"' WHERE TID = "+id+"";
                        Update(sql);
                        
                        if(t.getNewValue().equals("Completed")){
                            String tname = ((Task) t.getTableView().getItems().get(
                            t.getTablePosition().getRow())
                            ).getTaskName();
                            
                            String details = tname + " has been marked as completed";
                        
                            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                            Date dateobj = new Date();

                            String sql1 = "INSERT INTO notification(notificationType, receiverType, senderID, receiverID, details, dateSent) VALUES('Change in tasks status','client',"+csp.getSID()+","+cl.getCID()+",'"+ details+"','"+df.format(dateobj)+"')" ;
//                            Insert(sql1);

                             
                            String spName="";
                            try {
                                rs = stmt.executeQuery("SELECT * FROM serviceprovider WHERE SID ="+csp.getSID()+"");
                                while(rs.next()){
                                    spName = rs.getString("sp_Name");
                                }
                            } catch (SQLException ex) {
                                Logger.getLogger(SP_Side_ClientController.class.getName()).log(Level.SEVERE, null, ex);
                            }

                            String rec ="";
                            try {
                                rs = stmt.executeQuery("SELECT email FROM userclient c, project p WHERE c.CID = p.CID AND PID ="+cl.getCID()+"");
                                while(rs.next()){
                                    rec = rs.getString("email");
                                }
                            } catch (SQLException ex) {
                                Logger.getLogger(SP_Side_ClientController.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            if(!rec.equals("")){
                                System.out.println("send");
                                String[] to ={rec};
                                String subject = "Task completion";
                                String body ="Service provider "+spName+" has marked task "+tname+" as completed.";
                                ExecutorService emailExecutor = Executors.newSingleThreadExecutor();
                                  emailExecutor.execute(new Runnable() {
                                      @Override
                                      public void run() {
                                          SendMail.sendFromGMail(to, subject, body);
                                      }
                                  });
                                  emailExecutor.shutdown();
                            }  
                                                        
                            Notifications notificationBuilder = Notifications.create()
                            .title("Notification")
                            .text("Client notified that task was marked as completed")
                            .graphic(null)
                            .hideAfter(Duration.seconds(5))
                            .position(Pos.TOP_RIGHT);
                             notificationBuilder.showInformation();
                        }

                    }
                    tblTask.refresh();
                }
            }
        );
                
                
        tnameCol.setMinWidth(174);
        tdetailCol.setMinWidth(174);
        tdurationCol.setMinWidth(174);
        tsdateCol.setMinWidth(174);       
        tstatus.setMinWidth(174); 
           
        tblTask.setItems(taskObsList);
        
        tblTask.getColumns().addAll(tnameCol,tdetailCol,tsdateCol,tdurationCol,tstatus);
    }
    
    private void clearTaskTextBoxes() {
        txtTaskName.setText("");
        txtTaskDetails.setText("");
        txtTaskDuration.setText("");
        
        dtpTaskSDate.setPromptText("");
    }
    
    private ObservableList<Object> setUpComboBox(){
        ObservableList<Object> data = FXCollections.observableArrayList();
        data.add("Pending");
        data.add("Ongoing");
        data.add("Completed");
//        data.add("Verified");     //client verifies task
        return data;
//        cmdTaskStatus.setItems(data);
//        cmdTaskStatus.setPromptText("Select Status");
    }
   
    //----------rm--------------
    
    private void insertClientRMDetails(){
        try{
            rs = stmt.executeQuery("SELECT * FROM rawmaterial WHERE QID = 'Q"+cl.getCID()+csp.getSID()+"'");  //CID is actually PID
            rmObsList.clear(); //else when clicking on different sp, it will append rm details to the vector
            while(rs.next()){
                rmObsList.add(new RMaterial(rs.getString("itemName"),String.valueOf(rs.getInt("price")), String.valueOf(rs.getInt("quantity")),String.valueOf(rs.getInt("tCost"))));                                                   
            }
            updateTaskTable();
        }   catch(SQLException e){}
    }
        
    private void setRM(){
        createRawMaterialTable();
        
        btnAddRM.setOnMouseClicked(e1 ->{
            try {
                //insert into database
                insertRM();
            } catch (SQLException ex) {
                Logger.getLogger(SP_Side_ClientController.class.getName()).log(Level.SEVERE, null, ex);
            }
            clearRMTextBoxes();
        });
        

        btnRemoveRM.setOnMouseClicked(e1 -> {
            int selectedIndex = tblRawMaterial.getSelectionModel().getSelectedIndex();
            if (selectedIndex >= 0) {
                Delete("DELETE FROM rawmaterial WHERE QID = 'Q"+cl.getCID()+csp.getSID()+"' AND itemName = '"+rmObsList.get(selectedIndex).getItemName()+"'");
                insertClientRMDetails();
            } else {
                Notifications notificationBuilder = Notifications.create()
                    .title("No Raw material Selected")
                    .text("Please select desired raw material to be remove.")
                    .graphic(null)
                    .hideAfter(Duration.seconds(5))
                    .position(Pos.TOP_RIGHT);
                                //notificationBuilder.darkStyle();
                    notificationBuilder.showError(); 
            }
            updateRMTable();
        });

    }
        
    private void insertRM() throws SQLException{
        if(Validation.checkrmdetails(txtItemName.getText(),txtItemPrice.getText(),txtItemQuantity.getText())){
            Insert("INSERT INTO rawmaterial(QID, itemName, price, quantity, tCost) VALUES('Q"+cl.getCID()+csp.getSID()+"', '"+txtItemName.getText()+"',"+txtItemPrice.getText()+","+txtItemQuantity.getText()+" , "+String.valueOf(Double.valueOf(txtItemPrice.getText()) * Integer.valueOf(txtItemQuantity.getText()))+")");                                                    
            insertClientRMDetails();
        }
    }
    
    private void updateRMTable(){
        tblRawMaterial.setItems(rmObsList);
    }
    
    private void createRawMaterialTable() {
            
        tblRawMaterial.setEditable(true);
        
        TableColumn nameCol = new TableColumn("Item Name");
        TableColumn priceCol = new TableColumn("Price");
        TableColumn qtyCol = new TableColumn("Quantity");
        TableColumn tcostCol = new TableColumn("Total Cost");
                
        nameCol.setCellValueFactory(new PropertyValueFactory<RMaterial,String>("itemName"));
        priceCol.setCellValueFactory(new PropertyValueFactory<RMaterial,Double>("itemPrice"));
        qtyCol.setCellValueFactory(new PropertyValueFactory<RMaterial,Integer>("itemQuantity"));
        tcostCol.setCellValueFactory(new PropertyValueFactory<RMaterial,Double>("totalCost"));

        nameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        nameCol.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<RMaterial, String>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<RMaterial, String> t) {
                    if(Validation.checkEmptyString(t.getNewValue())){
                        ((RMaterial) t.getTableView().getItems().get(
                        t.getTablePosition().getRow())
                        ).setItemName(t.getNewValue());

                        String sql = "UPDATE rawmaterial SET name = '"+t.getNewValue()+"' WHERE QID = 'Q"+cl.getCID()+csp.getSID()+"' AND itemName = '"+t.getOldValue()+"'" ;
                        Update(sql);
                        insertClientRMDetails();
                    }
                    tblRawMaterial.refresh();
                }
            }
        );
        
        priceCol.setCellFactory(TextFieldTableCell.forTableColumn());
        priceCol.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<RMaterial, String>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<RMaterial, String> t) {
                    if(Validation.checkInteger(t.getNewValue())){
                        ((RMaterial) t.getTableView().getItems().get(
                            t.getTablePosition().getRow())
                            ).setItemPrice(t.getNewValue());
                        
                        
                        String name = t.getTableView().getItems().get(t.getTablePosition().getRow()).getItemName();
                        String price = t.getTableView().getItems().get(t.getTablePosition().getRow()).getItemPrice();
                        String quantity = t.getTableView().getItems().get(t.getTablePosition().getRow()).getItemQuantity();

                        String tcost = String.valueOf(Integer.valueOf(price) * Integer.valueOf(quantity));
                                
                        ((RMaterial) t.getTableView().getItems().get(
                            t.getTablePosition().getRow())
                        ).setTotalCost(tcost);
                        
                        int indexRM = tblRawMaterial.getSelectionModel().getFocusedIndex();

//                        System.out.print("indexRM: "+indexRM);

                        String sql = "UPDATE rawmaterial SET price = "+price+" , tCost = "+Integer.valueOf(quantity)*Integer.valueOf(price)+ " WHERE QID = 'Q"+cl.getCID()+csp.getSID()+"' AND itemName = '"+name+"'" ;
                        Update(sql);
                        insertClientRMDetails();
                    }
                    tblRawMaterial.refresh();
                }
            }
        );
        
        qtyCol.setCellFactory(TextFieldTableCell.forTableColumn());
        qtyCol.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<RMaterial, String>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<RMaterial, String> t) {
                    if(Validation.checkInteger(t.getNewValue())){
                        ((RMaterial) t.getTableView().getItems().get(
                            t.getTablePosition().getRow())
                            ).setItemQuantity(t.getNewValue());
                    
                        String name = t.getTableView().getItems().get(t.getTablePosition().getRow()).getItemName();
                        String price = t.getTableView().getItems().get(t.getTablePosition().getRow()).getItemPrice();
                        String quantity = t.getTableView().getItems().get(t.getTablePosition().getRow()).getItemQuantity();

                        String tcost = String.valueOf(Integer.valueOf(price) * Integer.valueOf(quantity));
                            
                        ((RMaterial) t.getTableView().getItems().get(
                            t.getTablePosition().getRow())
                        ).setTotalCost(tcost );
                        
                        int indexRM = tblRawMaterial.getSelectionModel().getFocusedIndex();

//                        System.out.print("indexRM: "+indexRM);

                        String sql = "UPDATE rawmaterial SET quantity = "+quantity+" , tCost = "+Integer.valueOf(quantity)*Integer.valueOf(price)+ " WHERE QID = 'Q"+cl.getCID()+csp.getSID()+"' AND itemName = '"+name+"'" ;
                        Update(sql);
                        insertClientRMDetails();
                    }
                    tblRawMaterial.refresh();
                }
            }
        );
        
                
        nameCol.setMinWidth(185);
        priceCol.setMinWidth(185);
        qtyCol.setMinWidth(185);
        tcostCol.setMinWidth(187);
        
        
        tblRawMaterial.setItems(rmObsList);
        
        tblRawMaterial.getColumns().addAll(nameCol,priceCol,qtyCol,tcostCol);
        
    }
    
    private void clearRMTextBoxes() {
        txtItemName.setText("");
        txtItemPrice.setText("");
        txtItemQuantity.setText("");
    }
    

    
    
    @FXML
    private void changeScreen(ActionEvent event) {
        Button btn = (Button) event.getSource();
        switch(btn.getText()){
           case "Profile" : myController.reloadScreen(ScreensFramework.SP_Side_ProfileID,ScreensFramework.SP_Side_ProfileFile);
           break;
           case "Quotation" : myController.reloadScreen(ScreensFramework.SP_Side_QuotationID,ScreensFramework.SP_Side_QuotationFile);
           break;
           case "Client" : myController.reloadScreen(ScreensFramework.SP_Side_ClientID,ScreensFramework.SP_Side_ClientFile);
           break;      
        }
    }
    
    public void setScreenParent(ScreensController screenParent){
        myController = screenParent;
    }

    
}
