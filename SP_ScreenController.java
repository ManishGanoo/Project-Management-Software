import java.io.IOException;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

public class SP_ScreenController extends dbManipulation implements Initializable, ControlledScreen{
    ScreensController myController;
    
    private String search_sql="SELECT * FROM serviceprovider ";
    private String ori_sql="SELECT * FROM serviceprovider ";
    private fillTable fill;
 
    //sp tab
    @FXML TableView tblSP, tblRawMaterial;
 
    @FXML TextField txtSPName, txtPhone, txtEmail, txtType;
    @FXML TextArea txaComments;
//    @FXML ComboBox cmbSPType;
    @FXML Button btnEditSP, btnAddNewSP, btnDeleteSP, btnAddRM,btnRemoveRM, btnAddQuo, btnEditQuo;

    @FXML DatePicker dtpDateStart;
    @FXML TextField txtDuration, txtLCost;
    @FXML TextField txtItemPrice, txtItemQuantity;
    @FXML ComboBox cmbItemName;
    @FXML Button btnReview;
    @FXML Label lblCommentsReview;
    
    //task
    int totalDays;
    @FXML Label lblCheckDates,lblMasRoof;
    @FXML TableView tblTask;
    @FXML TextField txtTaskName, txtTaskDetails, txtTaskDuration,txtRMTotal;
    @FXML DatePicker dtpTaskSDate;
    @FXML ComboBox cmbTaskStatus;
    @FXML Button btnAddTask, btnRemoveTask;
    ObservableList<RMaterial> rmObsList = FXCollections.observableArrayList();
    ObservableList<Task> taskObsList = FXCollections.observableArrayList();
    
    //quotation tab
    @FXML ComboBox cmbQuoType;
    @FXML TableView  tblSPReqQuo, tblRecQuotation, tblPlans,tblQuoTask, tblQuoRM; 
    @FXML Button btnSendReq, btnAcpQuo, btnRefQuo, btnViewReview;
    @FXML TextField txtCName, txtCAddr, txtCTel, txtCEmail, txtSID,txtSPType, txtTSDate;
    @FXML TextArea txaQuoComments;
    @FXML Label lblStartDate, lbldatesclash;
    
    @FXML DatePicker dtpQuoDateStart;
    @FXML TextField txtQuoDur, txtQuoLCost;
    
    String selectedIndex;
    String spTYPE;
    boolean exist = FALSE;
    boolean available = TRUE;
    
    Double RMTotal=0.00;
    int durationtotal = 0;
    int usesApp = 1;
    
    Vector<RMaterial> rawmVec = new Vector<>();
       
    Client cl;
    CurrentSPDetails csp;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cl = Context.getInstance().currentClient();
        csp = Context.getInstance().currentSPDetails();
        fill=new fillTable(tblSP, ori_sql+" WHERE SID IN (SELECT SID FROM selected_sp WHERE CID = "+cl.getCID()+" AND quo_status='ACCEPTED')");
         
        btnReview.setVisible(false);
//        LeaveReviewSP();
        setActionSP();      
        setActionQuo();
    }
    
    private void LeaveReviewSP(){
        try {
            rs = stmt.executeQuery("SELECT TID FROM task WHERE task_status != 'Verified' AND QID IN (SELECT QID FROM selected_sp WHERE quo_status ='ACCEPTED' AND CID ="+cl.getCID()+")");
            if(!rs.isBeforeFirst()){
                btnEditSP.setVisible(false);
                btnReview.setVisible(true);
                lblCommentsReview.setText("Review: ");
                txaComments.setText("");
            }
            else if(rs.next()){
                btnReview.setVisible(false);
    //            System.out.println("review found");
            }
        }catch(SQLException e) {}
    
        btnReview.setOnMouseClicked(e -> {
            Insert("INSERT INTO review(SID,CID,review) VALUES ("+selectedIndex+","+cl.getCID()+",'"+txaComments.getText()+"')");
        });
    }
    
    private void setActionSP() {             
        setType("setintialTypeUser");
        createRawMaterialTable();
        createTaskTable();
        
        tblSP.setOnMouseClicked(e -> {
            //txtTSDate.clear();
            lblCheckDates.setText("");
//            lblMasRoof.setText("");
            
            lblCommentsReview.setText("Comments: ");
            btnReview.setVisible(false);
            btnEditSP.setVisible(true);
            
            LeaveReviewSP();
            
            dtpDateStart.getEditor().clear();
            txtRMTotal.setText("");
            txtDuration.setText("");
            txtLCost.setText("");
            
            //lbldatesclash.setText("");
            
            if (tblSP.getSelectionModel().getSelectedItem() != null) {
                selectedIndex = tblSP.getSelectionModel().getSelectedItem().toString(); //get SELECTED ROW
                selectedIndex = selectedIndex.substring(1, selectedIndex.indexOf(",")); //GET ID OF SELECTED ROW
                csp.setQID("Q"+cl.getCID()+selectedIndex);
                insertSPData(selectedIndex);
                setUpComboBoxRM();
                setType(selectedIndex);
                
                //String[] selectedSp = selectedIndex.split(",");    
                
                try{
                    rs = stmt.executeQuery("SELECT sp_Type, usesApp FROM serviceprovider WHERE SID = "+selectedIndex);
                    while(rs.next()){
                        spTYPE = rs.getString("sp_Type");
                        usesApp = rs.getInt("usesApp");
                    }
                }catch(SQLException e1){} 
               // System.out.println("use app: "+usesApp);
                csp.setSpType(spTYPE);
                //System.out.println("spTYPE: "+csp.getSpType());
                
                try{
                    rs = stmt.executeQuery("SELECT review FROM review WHERE CID="+cl.getCID()+" AND SID = "+selectedIndex);
                    if (!rs.isBeforeFirst() ) {
                        btnReview.setDisable(false);
                        }
                    else if(rs.next()){
                        btnReview.setDisable(true);
                       // lblCommentsReview.setText("Review: ");
                        txaComments.setText(rs.getString("review"));
                    }
                }catch(SQLException e1){}   
            }     
        });
    
//        setComboBox(cmbSPType); // corect combobox sp type
        
        btnAddQuo.setOnMouseClicked(e -> { 
            if(Validation.checkSendQuotation(dtpDateStart.getValue(), txtDuration.getText(), txtLCost.getText(), rmObsList, taskObsList)){
                csp.setQID("Q"+cl.getCID()+csp.getSID());

                //if textbox empty send message,

                //display option if user agrees to send quotation then
                ButtonType okay = new ButtonType("OKAY", ButtonBar.ButtonData.OK_DONE);
                ButtonType cancel = new ButtonType("CANCEL", ButtonBar.ButtonData.CANCEL_CLOSE);
                Alert alert = new Alert(Alert.AlertType.WARNING,
                        "Are you sure you want to proceed?", //put quotation details
                        okay,
                        cancel);

                alert.setTitle("Confirmation");
                Optional<ButtonType> result = alert.showAndWait();

                if (result.isPresent() && result.get() == okay) {
                    //copied from quotation btn accept
                   // Update("UPDATE quotationrequest SET req_status = 'Received' WHERE SID = "+csp.getSID()+" AND CID = "+cl.getCID());

                    Insert("INSERT INTO quotation(QID, startDate, Duration, LabourCost) VALUES ('"+csp.getQID()+"','"+dtpDateStart.getValue()+"',"+txtDuration.getText()+","+txtLCost.getText()+")"); 
                  //  Insert("INSERT INTO selected_sp(CID, SID, QID, Comments, quo_status) VALUES("+cl.getCID()+","+csp.getSID()+",'"+csp.getQID()+"','', 'RECEIVED')");        //already added when created sp

                    //getData from raw material table and insert in db for last QID for CID
//                    Iterator<RMaterial> iter = rmObsList.iterator();
//                    while(iter.hasNext()){
//                        RMaterial g = iter.next();
//                        Insert("INSERT INTO rawmaterial(QID, itemName, price, quantity, tCost) VALUES ('"+csp.getQID()+"','"+g.getItemName()+"', "+g.getItemPrice()+","+g.getItemQuantity()+","+g.getTotalCost()+")");
//                    }
                    
                    java.sql.Date tstartdate = null;
                    Iterator<Task> iterT = taskObsList.iterator();
                    while(iterT.hasNext()){
                        Task g = iterT.next();
                        //g.toString();
                        if(csp.getSpType().compareTo("Masonry")!= 0){ //spType != masonry
                            //Insert("INSERT INTO task(QID, task_Name, task_details, startDate, duration, task_status) VALUES ('"+csp.getQID()+"','"+g.getTaskName()+"','"+g.getTaskDetails()+"','"+g.getTaskDate()+"',"+g.getTaskDuration()+",'Pending')");                                                                                                                               
                            
                            tstartdate = java.sql.Date.valueOf(g.getTaskDate());
                            totalDays=Integer.valueOf(g.getTaskDuration());
                        }
                        
                        else {  //sp_Type == Masonry
                            
                            if(g.getTaskName().equalsIgnoreCase("Preparation of Roof")){
                            //    Insert("INSERT INTO task(QID, task_Name, task_details, startDate, duration, task_status) VALUES ('"+csp.getQID()+"','"+g.getTaskName()+"','"+g.getTaskDetails()+"','"+g.getTaskDate()+"',"+g.getTaskDuration()+",'Pending')"); 
                                Update("UPDATE TempStartDate SET startDate = '"+g.getTaskDate()+"' WHERE PID = "+cl.getCID()+" AND sp_type = 'Roof-Formwork' AND PID='"+cl.getCID()+"'");
                                Delete("DELETE FROM task WHERE QID = '"+csp.getQID()+"' AND task_Name ='Preparation of Roof'");
 
                            }

                            else if(g.getTaskName().equalsIgnoreCase("Concrete to Roof")){
                            //    Insert("INSERT INTO task(QID, task_Name, task_details, startDate, duration, task_status) VALUES ('"+csp.getQID()+"','"+g.getTaskName()+"','"+g.getTaskDetails()+"','"+g.getTaskDate()+"',"+g.getTaskDuration()+",'Pending')");                                                                                                                              
                                Update("UPDATE TempStartDate SET startDate = '"+g.getTaskDate()+"' WHERE PID = "+cl.getCID()+" AND sp_type = 'Roof-Concrete' AND PID='"+cl.getCID()+"'");
                                Delete("DELETE FROM task WHERE QID = '"+csp.getQID()+"' AND task_Name = 'Concrete to Roof'");

                            }

                            else {
                                //Insert("INSERT INTO task(QID, task_Name, task_details, startDate, duration, task_status) VALUES ('"+csp.getQID()+"','"+g.getTaskName()+"','"+g.getTaskDetails()+"','"+g.getTaskDate()+"',"+g.getTaskDuration()+",'Pending')");                                                                                                                               
            
                                tstartdate = java.sql.Date.valueOf(g.getTaskDate());
                                totalDays=Integer.valueOf(g.getTaskDuration());

                            }
                            
                        }
                       
                    }
                    
                    java.sql.Date endDate = new java.sql.Date(tstartdate.getTime() + TimeUnit.DAYS.toMillis(totalDays-1));
                    
                    java.sql.Date tnextdate = new java.sql.Date(tstartdate.getTime() + TimeUnit.DAYS.toMillis(totalDays));
                    //Date endDate = new java.sql.Date(tstartdate.getTime();
                   // Insert("INSERT INTO task(QID, task_Name, task_details, startDate, duration, task_status) VALUES ('"+csp.getQID()+"','"+csp.getSpType()+"','Date for successor','"+tnextdate+"',0,'Pending')");                                                                                                                               

                    Update("UPDATE TempStartDate SET startDate='"+tnextdate+"' WHERE predecessor='"+csp.getSpType()+"'");
                    Update("UPDATE TempStartDate SET endDate = '"+endDate+"' WHERE PID = "+cl.getCID()+" AND sp_type = '"+csp.getSpType()+"'");
                       
                    
                    //go back to previous screen
//                    myController.reloadScreen(ScreensFramework.SP_Side_QuotationID, ScreensFramework.SP_Side_QuotationFile);
            
                }
            } 
        });
        
        btnEditQuo.setOnMouseClicked(e -> {
            Update("UPDATE quotation SET LabourCost = "+txtLCost.getText()+" WHERE QID='"+csp.getQID()+"'");
        });
        
        btnAddRM.setOnMouseClicked(e -> {
            if(Validation.checkaddrm(cmbItemName.getSelectionModel().getSelectedItem().toString(),txtItemPrice.getText(), txtItemQuantity.getText(), rmObsList)){
                RMaterial rm = new RMaterial(cmbItemName.getSelectionModel().getSelectedItem().toString(),txtItemPrice.getText(), txtItemQuantity.getText(), String.valueOf(Double.valueOf(txtItemPrice.getText()) * Integer.valueOf(txtItemQuantity.getText())));          
                rmObsList.add(rm);
                
                RMTotal += Double.valueOf(txtItemPrice.getText()) * Integer.valueOf(txtItemQuantity.getText());
                txtRMTotal.setText(String.valueOf(RMTotal));

                Insert("INSERT INTO rawmaterial(QID, itemName, price, quantity, tCost) VALUES('"+csp.getQID()+"', '"+cmbItemName.getSelectionModel().getSelectedItem().toString()+"',"+txtItemPrice.getText()+","+txtItemQuantity.getText()+" , "+String.valueOf(Double.valueOf(txtItemPrice.getText()) * Integer.valueOf(txtItemQuantity.getText()))+")");                                                    
            
                updateRMTable();
                clearRMTextBoxes();
            }
        });
        
        btnRemoveRM.setOnMouseClicked(e -> {
            int selectedIndex = tblRawMaterial.getSelectionModel().getSelectedIndex();
            if (selectedIndex >= 0) {
                //tblRawMaterial.getItems().remove(selectedIndex); //delete from table   
                RMTotal -= Double.parseDouble(rmObsList.get(selectedIndex).getTotalCost());
                txtRMTotal.setText(String.valueOf(RMTotal));
                
                Delete("DELETE FROM rawmaterial WHERE QID = '"+csp.getQID()+"' AND itemName = '"+rmObsList.get(selectedIndex).getItemName()+"'");
                                
                rmObsList.remove(selectedIndex);    //delete from vector    
                updateRMTable();

            } else {
                Notifications notificationBuilder = Notifications.create()
                    .title("No Item Selected")
                    .text("Please select desired item to be remove.")
                    .graphic(null)
                    .hideAfter(Duration.seconds(5))
                    .position(Pos.TOP_RIGHT);
                                //notificationBuilder.darkStyle();
                    notificationBuilder.showError(); 
            }
            updateRMTable();
        });
                
        btnAddNewSP.setOnMouseClicked(e -> {
            try {
                AnchorPane root = (AnchorPane) FXMLLoader.load(getClass().getResource("AddNewSP.fxml"));
                Stage secondStage = new Stage();
                secondStage.setScene(new Scene(root));
                secondStage.setTitle("Create New Service Provider");
                secondStage.show();
                
                secondStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                    public void handle(WindowEvent we) {
                        myController.reloadScreen(ScreensFramework.screen4ID,ScreensFramework.screen4File);
                    }
                }); 
            } catch (IOException ex) {
                Logger.getLogger(SP_ScreenController.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        
        btnDeleteSP.setOnMouseClicked(e -> {
            int selectedIndex = tblSP.getSelectionModel().getSelectedIndex();
            if(selectedIndex>=0){
                int countSP=1;
                try{
    //                System.out.println("SELECT COUNT(QID) AS count FROM selected_sp WHERE status='ACCEPTED' AND CID="+cl.getCID()+" AND SID IN (SELECT SID FROM serviceprovider WHERE sp_Type='"+csp.getSpType()+"')");  
                    rs = stmt.executeQuery("SELECT COUNT(QID) AS count FROM selected_sp WHERE quo_status='ACCEPTED' AND CID="+cl.getCID()+" AND SID IN (SELECT SID FROM serviceprovider WHERE sp_Type='"+csp.getSpType()+"')");
                    while(rs.next()){
                        countSP = rs.getInt("count");
    //                    System.out.println("COUNT: "+countSP);
                    }
                }catch(SQLException e1){}  

                if(countSP == 1){
                    Update("UPDATE TempStartDate SET exist=0 WHERE PID="+cl.getCID()+" AND sp_type='"+csp.getSpType()+"'");
                }

                try{
                    boolean getTempDate=FALSE;
    //                System.out.println("SELECT task_status FROM task WHERE QID='"+csp.getQID()+"'");
                    rs = stmt.executeQuery("SELECT task_status FROM task WHERE QID='"+csp.getQID()+"'");
                    while(rs.next()){
                        if(getTempDate == TRUE) break;
                        String status = rs.getString("task_status");
    //                    System.out.println("status: "+status);
                        if(status.compareTo("Ongoing")==0){
                            getTempDate=TRUE;
//                            Date today = new Date();
//                            Calendar cal = Calendar.getInstance();
//                            cal.setTime(today);
//                            LocalDate date = LocalDate.of(cal.get(Calendar.YEAR),
//                                    cal.get(Calendar.MONTH) + 1,
//                                    cal.get(Calendar.DAY_OF_MONTH));
                            LocalDate tomorrow = LocalDate.now().plusDays(1);
                            
                            Update("UPDATE TempStartDate SET startDate='"+tomorrow+"' WHERE PID = "+cl.getCID()+" AND sp_type='"+csp.getSpType()+"'");
                        }
                    }
                    if(csp.getSpType().compareTo("Masonry")==0){
                        Update("UPDATE TempStartDate SET startDate=null WHERE PID = "+cl.getCID()+" AND sp_type IN ('Roof-Concrete', 'Roof-Formwork')");
                    }

                }catch(SQLException e1){}

                Delete("DELETE FROM task WHERE task_status IN('Ongoing','Pending') AND QID = (SELECT QID FROM selected_sp WHERE CID="+cl.getCID()+" AND SID="+selectedIndex+")");
                Update("UPDATE selected_sp SET quo_status='DECLINED' WHERE QID='"+csp.getQID()+"'");

                Notifications notificationBuilder = Notifications.create()
                    .title("Success")
                    .text("Service provider was successfully deleted.")
                    .graphic(null)
                    .hideAfter(Duration.seconds(5))
                    .position(Pos.TOP_RIGHT);
                                //notificationBuilder.darkStyle();
                    notificationBuilder.showInformation(); 

                myController.reloadScreen(ScreensFramework.screen4ID,ScreensFramework.screen4File);
            }
            else {
                Notifications notificationBuilder = Notifications.create()
                    .title("No Service provider Selected")
                    .text("Please select desired record to be remove.")
                    .graphic(null)
                    .hideAfter(Duration.seconds(5))
                    .position(Pos.TOP_RIGHT);
                                //notificationBuilder.darkStyle();
                    notificationBuilder.showError(); 
            }
       });
    }
    
    
    private void setTask(){
        cmbTaskStatus.setItems(setUpComboBox());
        taskObsList.clear();
        fillTask();
        
        txtTaskName.setOnMouseClicked(e2 ->{
            lblCheckDates.setText("");
        });
                
        btnAddTask.setOnMouseClicked(e1 ->{
            try {
                if(Validation.checkTaskDetails(txtTaskName.getText(),txtTaskDetails.getText(), dtpTaskSDate.getValue(), txtTaskDuration.getText(), "Pending")){
                    int d = Integer.valueOf(txtTaskDuration.getText());
                    
                    durationtotal += Integer.parseInt(txtTaskDuration.getText());
                    txtDuration.setText(String.valueOf(durationtotal));
                    
                    String tid="";
                    String sql = "INSERT INTO task(QID, task_Name, task_details, startDate, duration, task_status) VALUES('"+csp.getQID()+"', '"+txtTaskName.getText()+"','"+txtTaskDetails.getText()+"','"+String.valueOf(dtpTaskSDate.getValue())+"',"+txtTaskDuration.getText()+",'Pending');" ;
                    //run sql below, and get generated CID
                    PreparedStatement statement;
                    try {
                        statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

                        int rows = statement.executeUpdate();
                        rs = statement.getGeneratedKeys();     
                        int ID = 0;
                        if(rs.next()){
                            ResultSetMetaData rsmd = rs.getMetaData();
                            int colCount = rsmd.getColumnCount();
                            do {
                                for (int i = 1; i <= colCount; i++) {
                                    String key = rs.getString(i);
                                    ID = Integer.valueOf(key);
                                    tid = key;
                                }
                            }while (rs.next());
                        }
                    } catch (SQLException ex) {
                        Logger.getLogger(CreateAccountController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                    taskObsList.add(new Task(tid,txtTaskName.getText(),txtTaskDetails.getText(),String.valueOf(dtpTaskSDate.getValue()), txtTaskDuration.getText(), "Pending"));
                    available = checkDates(String.valueOf(dtpTaskSDate.getValue()),d);
                    
                    if(available==FALSE){
                    //System.out.println("Date unavailable");
                        lblCheckDates.setText("Clash with existing tasks");

                    }
                    
                    Update("Update quotation SET Duration = Duration + "+txtTaskDuration.getText()+" WHERE QID='"+csp.getQID()+"'");

                    updateTaskTable();
                    clearTaskTextBoxes();
                }
            } catch (SQLException ex) {
                Logger.getLogger(SP_Side_SendQuotationController.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        
             
        btnRemoveTask.setOnMouseClicked(e1 -> {
            int selectedIndex = tblTask.getSelectionModel().getSelectedIndex();
            if (selectedIndex >= 0) {
                //System.out.println("selectedindex: "+selectedIndex);
                //taskObsList.remove(selectedIndex);
                if(csp.getSpType().compareTo("Masonry")==0){
                    //System.out.println("enters loop");
                    if(taskObsList.get(selectedIndex).getTaskName().compareTo("Preparation of Roof")==0){
                        int result = checkIfTaskDone("Roof-Formwork");
                           // System.out.println("result: "+result);
                            if(result == 1){
                                taskObsList.remove(selectedIndex);  //delete from vector  
                                
                                //tblTask.getItems().remove(selectedIndex); //delete from table 
    //                                    updateTaskTable();
                            }
                    }
                    else if(taskObsList.get(selectedIndex).getTaskName().compareTo("Concrete to Roof")==0){
                        int result = checkIfTaskDone("Roof-Concrete");
                        if(result == 1){
                            taskObsList.remove(selectedIndex);  //delete from vector  
                        }
                    }
                    else {
                        Delete("DELETE FROM task WHERE TID="+taskObsList.get(selectedIndex).getTaskID());
                        taskObsList.remove(selectedIndex);
                    }
                   //updateTaskTable();
                }
                else {   // not Masonry
                    Delete("DELETE FROM task WHERE TID="+taskObsList.get(selectedIndex).getTaskID());
                    Update("Update quotation SET Duration = Duration - "+taskObsList.get(selectedIndex).getTaskDuration()+" WHERE QID='"+csp.getQID()+"'");
                    durationtotal -= Integer.parseInt(taskObsList.get(selectedIndex).getTaskDuration());
                    txtDuration.setText(String.valueOf(durationtotal));
                    taskObsList.remove(selectedIndex); 
                }
                updateTaskTable();
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
        
    private int checkIfTaskDone(String type){
        String status = "Pending";  
        try{
            rs = stmt.executeQuery("SELECT task_status FROM task WHERE QID = (SELECT QID FROM selected_sp WHERE CID = "+cl.getCID()+" AND status='ACCEPTED' AND SID IN (SELECT SID FROM serviceprovider WHERE sp_Type = '"+type+"'))");
            while (rs.next()){
                status = rs.getString("task_status");  
            }
        }catch(SQLException e){}
             
//        System.out.println("status: "+status);
       
        if((status.compareTo("Completed")==0) || (status.compareTo("Verified")==0)){
            return 1;
        }
        
        else {
            return 0;
        }
    }

     
    private void fillTask(){
        
        taskObsList.clear();
        int TaskExist = 1;
        
        try{
            rs = stmt.executeQuery("SELECT * FROM TASK WHERE QID ='"+csp.getQID()+"'");
            if(!rs.isBeforeFirst()){ //rs empty thus no quotation yet for non user sp
               TaskExist=0; 
            }
            
            while(rs.next()){
                taskObsList.add(new Task(String.valueOf(rs.getInt("TID")),rs.getString("task_Name"), rs.getString("task_details"), String.valueOf(rs.getDate("startDate")), String.valueOf(rs.getInt("duration")), rs.getString("task_status") ));
            }
        }   catch(SQLException e){}
        
        //System.out.println("sp Type "+spTYPE);
        String sql1 = "SELECT * FROM defaulttasks WHERE serviceProviderType = '"+txtType.getText()+"'";
        String sql2 = "SELECT * FROM defaulttasks WHERE serviceProviderType IN ('Masonry', 'Roof-Formwork', 'Roof-Concrete')";
        String sql3 = "SELECT * FROM defaulttasks WHERE serviceProviderType IN ('Masonry', 'Roof-Concrete')";
        
        String selectedsql = sql1;
        
        if( (usesApp==0) && (txtType.getText().toString().compareTo("Masonry")==0)){
            
            int resultRF = checkIfTaskDone("Roof-Formwork");
            if(resultRF == 1){
                int resultRC = checkIfTaskDone("Roof-Concrete");
                if(resultRC == 0){
                    //lblMasRoof.setText("Provide dates for Roofing works");
                    selectedsql = sql3;
                }
            }
            else {
                selectedsql = sql2;    
                //lblMasRoof.setText("Provide dates for Roofing works");
            }
            
        }
        
        ArrayList<String> DtaskName = new ArrayList<String>();
        
        if(TaskExist == 0){
            try{
            rs = stmt.executeQuery(selectedsql);            
            while(rs.next()){
                DtaskName.add(rs.getString("taskName"));
            }
            }   catch(SQLException e){}
            
            for(int a=0; a<DtaskName.size();a++){
                
                String tid="";
                    String sql = "INSERT INTO task(QID, task_Name, task_details, startDate, duration, task_status) VALUES('"+csp.getQID()+"', '"+DtaskName.get(a)+"','',null,0,'Pending');" ;
                    //run sql below, and get generated CID
                    PreparedStatement statement;
                    try {
                        statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

                        int rows = statement.executeUpdate();
                        rs = statement.getGeneratedKeys();     
                        int ID = 0;
                        if(rs.next()){
                            ResultSetMetaData rsmd = rs.getMetaData();
                            int colCount = rsmd.getColumnCount();
                            do {
                                for (int i = 1; i <= colCount; i++) {
                                    String key = rs.getString(i);
                                    ID = Integer.valueOf(key);
                                    tid = key;
                                }
                            }while (rs.next());
                        }
                    } catch (SQLException ex) {
                        Logger.getLogger(CreateAccountController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                taskObsList.add(new Task(tid,DtaskName.get(a),"", "","0", ""));
            }
            
            
        }
        
        updateTaskTable();
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

                        String sql = "UPDATE task SET task_Name = '"+t.getNewValue()+"' WHERE TID = "+id+"";
                        Update(sql);
//                        myController.reloadScreen(ScreensFramework.screen4ID, ScreensFramework.screen4File);
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
                    
                    String sql = "UPDATE task SET task_details = '"+t.getNewValue()+"' WHERE TID = "+id+"";
                    Update(sql);
//                    myController.reloadScreen(ScreensFramework.screen4ID, ScreensFramework.screen4File);
                    myController.refreshScreen(ScreensFramework.screen3ID, ScreensFramework.screen3File);
                    tblTask.refresh();
                }
                
            }
        );
        
        tsdateCol.setCellFactory(TextFieldTableCell.forTableColumn());
        tsdateCol.setOnEditStart(new EventHandler() {
            @Override
            public void handle(Event event) {
                lblCheckDates.setText("");
            }
        });
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
                    
                        String dur = ((Task) t.getTableView().getItems().get(
                        t.getTablePosition().getRow())
                        ).getTaskDuration();

                        String sql = "UPDATE task SET startDate = '"+t.getNewValue()+"' WHERE TID = "+id+"";
                        Update(sql);
                        available = checkDates(t.getNewValue(),Integer.valueOf(dur));
                        if(available==FALSE){
                        //System.out.println("Date unavailable");
                            lblCheckDates.setText("Clash with existing tasks");

                        }
                        
//                        myController.reloadScreen(ScreensFramework.screen4ID, ScreensFramework.screen4File);
                        myController.refreshScreen(ScreensFramework.screen3ID, ScreensFramework.screen3File);
//                    }
                        tblTask.refresh();
                }
            }
        );
        
        tdurationCol.setCellFactory(TextFieldTableCell.forTableColumn());
        tdurationCol.setOnEditStart(new EventHandler() {
            @Override
            public void handle(Event event) {
                lblCheckDates.setText("");
            }
        });
        tdurationCol.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<Task, String>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<Task, String> t) {
                    if(Validation.checkInteger(t.getNewValue())){

                        String id = ((Task) t.getTableView().getItems().get(
                        t.getTablePosition().getRow())
                        ).getTaskID();
                    
                        String sdate = t.getTableView().getItems().get(t.getTablePosition().getRow()).getTaskDate();
                                
                        if(!sdate.isEmpty()){
                            
                        durationtotal = durationtotal - Integer.parseInt(t.getOldValue()) + Integer.parseInt(t.getNewValue());
                        txtDuration.setText(String.valueOf(durationtotal));
                        
                        available = checkDates(sdate,Integer.valueOf(t.getNewValue()));
                        if(available==FALSE){
//                        System.out.println("Date unavailable");
                            lblCheckDates.setText("Clash with existing tasks");

                        }
                        
                        ((Task) t.getTableView().getItems().get(
                                t.getTablePosition().getRow())
                                ).setTaskDuration(t.getNewValue());

                        }
                        
                        String sql = "UPDATE task SET duration = '"+t.getNewValue()+"' WHERE TID = "+id+"";
                        Update(sql);
//                        myController.reloadScreen(ScreensFramework.screen4ID, ScreensFramework.screen4File);
                        myController.refreshScreen(ScreensFramework.screen3ID, ScreensFramework.screen3File);
                    }
                    tblTask.refresh();
                }
            }
        );
                
        tstatus.setCellFactory(ComboBoxTableCell.forTableColumn(setUpComboBox()));
        tsdateCol.setOnEditStart(new EventHandler() {
            @Override
            public void handle(Event event) {
                //lblCheckDates.setText("");
            }
        });
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
                    
                        String dur = t.getTableView().getItems().get(t.getTablePosition().getRow()).getTaskDuration();
                        if(!dur.isEmpty()){
                            int duration = Integer.parseInt(dur);
                            //checkDates(t.getNewValue(),duration);
                        }
                    
                        String sql = "UPDATE task SET task_status = '"+t.getNewValue()+"' WHERE TID = "+id+"";
                        Update(sql);
//                        myController.reloadScreen(ScreensFramework.screen4ID, ScreensFramework.screen4File);
                        myController.refreshScreen(ScreensFramework.screen3ID, ScreensFramework.screen3File);
                    }
                    tblTask.refresh();
                }
            }
        );
                
                
        tnameCol.setMinWidth(170);
        tdetailCol.setMinWidth(170);
        tdurationCol.setMinWidth(175);
        tsdateCol.setMinWidth(170);       
        tstatus.setMinWidth(170); 
           
//        fillTask();
//        tblTask.setItems(taskObsList);
        tblTask.getColumns().addAll(tnameCol,tdetailCol,tsdateCol,tdurationCol,tstatus);
    }
    
    private void clearTaskTextBoxes() {
        txtTaskName.setText("");
        txtTaskDetails.setText("");
        txtTaskDuration.setText("");
        
        dtpTaskSDate.setPromptText("");
    }
    
    private void setType(String ID){
        if(ID == "setintialTypeUser"){
            txtSPName.setEditable(false);
            txtPhone.setEditable(false);
            txtEmail.setEditable(false);
            btnEditSP.setText("Edit Comment");
//            cmbSPType.setVisible(false);
//            cmbSPType.setEditable(false);
            btnEditSP.setOnMouseClicked(e -> {
                editComment(ID);
            });

            dtpDateStart.setEditable(false);

            txtDuration.setEditable(false);
            txtLCost.setEditable(false);

            cmbItemName.setVisible(false);
            txtItemPrice.setVisible(false);
            txtItemQuantity.setVisible(false);
            btnAddRM.setVisible(false);
            btnAddQuo.setVisible(false);
            btnEditQuo.setVisible(false);
            btnRemoveRM.setVisible(false);

            tblTask.setEditable(false);
            txtTaskName.setVisible(false);
            txtTaskDetails.setVisible(false);
            txtTaskDuration.setVisible(false);
            dtpTaskSDate.setVisible(false);
            cmbTaskStatus.setVisible(false);
            btnAddTask.setVisible(false);
            btnRemoveTask.setVisible(false);
        }
        try{
            rs = stmt.executeQuery( "SELECT usesAPP FROM serviceprovider WHERE SID = '"+ID+"'");
            
            while (rs.next()){
                boolean usesApp = rs.getBoolean("usesAPP");
                if(usesApp == true){
                    txtSPName.setEditable(false);
                    txtPhone.setEditable(false);
                    txtEmail.setEditable(false);
                    btnEditSP.setText("Edit Comment");
//                    cmbSPType.setVisible(false);
//                    cmbSPType.setEditable(false);
                    btnEditSP.setOnMouseClicked(e -> {
                        editComment(ID);
                    });
                    
                    dtpDateStart.setEditable(false);
                    
                    txtDuration.setEditable(false);
                    txtLCost.setEditable(false);
                    
                    cmbItemName.setVisible(false);
                    txtItemPrice.setVisible(false);
                    txtItemQuantity.setVisible(false);
                    btnAddRM.setVisible(false);
                    btnAddQuo.setVisible(false);
                    btnEditQuo.setVisible(false);
                    btnRemoveRM.setVisible(false);
                    
                    tblTask.setEditable(false);
                    txtTaskName.setVisible(false);
                    txtTaskDetails.setVisible(false);
                    txtTaskDuration.setVisible(false);
                    dtpTaskSDate.setVisible(false);
                    cmbTaskStatus.setVisible(false);
                    btnAddTask.setVisible(false);
                    btnRemoveTask.setVisible(false);
                }
                else{ //non user
                    txtSPName.setEditable(true);
                    txtPhone.setEditable(true);
                    txtEmail.setEditable(true);
                    btnEditSP.setText("Edit Service Provider");
//                    cmbSPType.setVisible(true);
//                    cmbSPType.setEditable(true);
                    btnEditSP.setOnMouseClicked(e -> {
                        editSP(ID);
                    });
                                        
                    dtpDateStart.setEditable(true);
                    txtDuration.setEditable(true);
                    txtLCost.setEditable(true);
                    
                    cmbItemName.setVisible(true);
                    txtItemPrice.setVisible(true);
                    txtItemQuantity.setVisible(true);
                    btnAddRM.setVisible(true);
                    //btnAddQuo.setVisible(true);
                    btnRemoveRM.setVisible(true);
                    
                    tblTask.setEditable(true);
                    txtTaskName.setVisible(true);
                    txtTaskDetails.setVisible(true);
                    txtTaskDuration.setVisible(true);
                    dtpTaskSDate.setVisible(true);
                    cmbTaskStatus.setVisible(true);
                    btnAddTask.setVisible(true);
                    btnRemoveTask.setVisible(true);
                }
                //setUpTableUser(usesApp);
            }
        }   catch(SQLException e){}

    }
    
    private void insertSPData(String ID) {
       // cl.setCID(1);
        //System.out.println(cl.getCID());
        RMTotal=0.00;
        
        try{
            rs = stmt.executeQuery( ori_sql + " WHERE SID = "+ID);
            while (rs.next()){
                csp.setSID(rs.getInt("SID"));
                txtSPName.setText(rs.getString("sp_Name")); 
                txtPhone.setText(String.valueOf(rs.getInt("telephone")));  
                txtEmail.setText(rs.getString("email"));
                txtType.setText(rs.getString("sp_Type"));
            }
        }   catch(SQLException e){}
        
        
        try{
            rs = stmt.executeQuery("SELECT * FROM selected_sp WHERE CID = "+cl.getCID()+" AND SID= "+csp.getSID());
            while(rs.next()){
                csp.setQID(rs.getString("QID"));
                //System.out.println("QID= "+csp.getQID());
                txaComments.setText(rs.getString("Comments"));
                
                //get task details for current sp
//                fill=new fillTable(tblTask, "SELECT * FROM task WHERE QID = '"+csp.getQID()+"'");
                setTask();
            }
        }   catch(SQLException e){}


        
        boolean provideTSD = false;
        //get rmaterial details for current sp
        try{
            rs = stmt.executeQuery("SELECT * FROM quotation WHERE QID = '"+csp.getQID()+"'");
            if(!rs.isBeforeFirst()){ //rs empty thus no quotation yet for non user sp
                provideTSD = true;
                durationtotal = 0;
                btnEditQuo.setVisible(false);
                btnAddQuo.setVisible(true);
            }
            while(rs.next()){
                dtpDateStart.setValue(rs.getDate("startDate").toLocalDate());
                txtDuration.setText(String.valueOf(rs.getInt("Duration")));
                durationtotal = rs.getInt("Duration");
                txtLCost.setText(String.valueOf(rs.getInt("LabourCost")));
                
                btnEditQuo.setVisible(true);
                btnAddQuo.setVisible(false);
            }
        }   catch(SQLException e){}
        
        try{
            rs = stmt.executeQuery("SELECT * FROM rawmaterial WHERE QID = '"+csp.getQID()+"'");
            rmObsList.clear(); //else when clicking on different sp, it will append rm details to the vector
            while(rs.next()){
               
                RMTotal += rs.getDouble("price")*rs.getInt("quantity");          
                RMaterial rm = new RMaterial(rs.getString("itemName"),String.valueOf(rs.getDouble("price")), String.valueOf(rs.getInt("quantity")), String.valueOf(rs.getDouble("price")*rs.getInt("quantity")));          
                rmObsList.add(rm);                
            }
            updateRMTable();
            txtRMTotal.setText(String.valueOf(RMTotal));
        }   catch(SQLException e){}
        
        LocalDate tsdate = null;
        
        if(provideTSD == true){
            try{
            rs = stmt.executeQuery("SELECT startDate FROM tempstartdate WHERE PID = "+cl.getCID()+" AND sp_type = '"+txtType.getText()+"'");
            if(rs.wasNull()){
                dtpDateStart.setPromptText("Select Date");    
            }
            while(rs.next()){
                tsdate = rs.getDate("startDate").toLocalDate();
                dtpDateStart.setValue(tsdate);
            }
        }   catch(SQLException e){}
        }
       
        
        
        
        if(txtDuration.getText() == ""){
            btnAddQuo.setText("Edit Quotation");
        }
        
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
        
        nameCol.setMinWidth(100);
        priceCol.setMinWidth(100);
        qtyCol.setMinWidth(100);
        tcostCol.setMinWidth(100);

        nameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        nameCol.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<RMaterial, String>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<RMaterial, String> t) {
                    if(Validation.checkEmptyString(t.getNewValue())){
                            ((RMaterial) t.getTableView().getItems().get(
                            t.getTablePosition().getRow())
                            ).setItemName(t.getNewValue());
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

                        int indexRM = tblRawMaterial.getSelectionModel().getFocusedIndex();
                        RMTotal -= Double.parseDouble(rmObsList.get(indexRM).getTotalCost());

//                        System.out.print("indexRM: "+indexRM);

                        RMaterial rm = new RMaterial(name,price,quantity,String.valueOf(Double.valueOf(price)* Double.valueOf(quantity)));
                        rmObsList.set(indexRM, rm);
                        updateRMTable();
                        
                        String sql = "UPDATE rawmaterial SET price = "+price+" , tCost = "+Integer.valueOf(quantity)*Integer.valueOf(price)+ " WHERE QID = 'Q"+cl.getCID()+csp.getSID()+"' AND itemName = '"+name+"'" ;
                        Update(sql);
                        
                        RMTotal += Double.parseDouble(rmObsList.get(indexRM).getTotalCost());
                        txtRMTotal.setText(String.valueOf(RMTotal));
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

                        int indexRM = tblRawMaterial.getSelectionModel().getFocusedIndex();
                        RMTotal -= Double.parseDouble(rmObsList.get(indexRM).getTotalCost());

//                        System.out.print("indexRM: "+indexRM);

                        RMaterial rm = new RMaterial(name,price,quantity,String.valueOf(Double.valueOf(price)* Double.valueOf(quantity)));
                        rmObsList.set(indexRM, rm);
                        updateRMTable();
                        
                        String sql = "UPDATE rawmaterial SET quantity = "+quantity+" , tCost = "+Integer.valueOf(quantity)*Integer.valueOf(price)+ " WHERE QID = 'Q"+cl.getCID()+csp.getSID()+"' AND itemName = '"+name+"'" ;
                        Update(sql);
                        
                        RMTotal += Double.parseDouble(rmObsList.get(indexRM).getTotalCost());
                        txtRMTotal.setText(String.valueOf(RMTotal));

                    }
                    tblRawMaterial.refresh();

                }
            }
        );
        
        tblRawMaterial.setItems(rmObsList);
        tblRawMaterial.getColumns().addAll(nameCol,priceCol,qtyCol,tcostCol);
        
    }
    
    private void updateRMTable(){
        tblRawMaterial.setItems(rmObsList);
    }
    
    private void clearRMTextBoxes() {
        cmbItemName.getSelectionModel().clearSelection();
        //cmbItemName.getItems().clear( );
        cmbItemName.setValue(null);  
        txtItemPrice.setText("");
        txtItemQuantity.setText("");
    }
      
    private void setComboBox(ComboBox sptypecombo){
        ObservableList<Object> data = FXCollections.observableArrayList();
        try{
            rs = stmt.executeQuery("SELECT DISTINCT sp_Type FROM serviceprovider" );
            while (rs.next()){
                data.add(rs.getString("sp_Type"));
            }
        }catch(SQLException e){}
        sptypecombo.setPromptText("Select Type");
        sptypecombo.setItems(data);
        sptypecombo.getEditor().textProperty().addListener((obs, oldText, newText) -> {
        sptypecombo.setValue(newText);
        });
    }
   
    public void refreshSPTable() {
        fill=new fillTable(tblSP, ori_sql+" WHERE SID IN (SELECT SID FROM selected_sp WHERE quo_status='ACCEPTED')");
    }

    private void UpdateSPTable(){
        fill=new fillTable(tblSP, ori_sql+" WHERE SID IN (SELECT SID FROM selected_sp WHERE quo_status='ACCEPTED')");
    }
    
    private void editComment(String ID){//user
        if(!ID.equals("setintialTypeUser")){
            Insert("UPDATE selected_sp SET Comments = '"+txaComments.getText()+ "' WHERE SID = "+Integer.valueOf(ID)+" AND CID ="+cl.getCID());
            UpdateSPTable();
        }
    }

    private void editSP(String ID){ //non user
        //issue on spType. its null because of combobox not yet finalised
        if( Validation.checkEditSP(txtSPName.getText(), txtPhone.getText(),txtEmail.getText(),txtType.getText())){
            Insert("UPDATE serviceprovider SET sp_Name = '"+txtSPName.getText()+"' , telephone = "+txtPhone.getText()+" , email = '"+txtEmail.getText()+"' , sp_Type = '"+txtType.getText()+"' WHERE SID = "+Integer.valueOf(ID));
            //Insert("UPDATE serviceprovider SET sp_Name = '"+txtSPName.getText()+"' , telephone = "+txtPhone.getText()+" , email = '"+txtEmail.getText()+"' , sp_Type = '"+cmbSPType.getValue()+"' WHERE SID = "+Integer.valueOf(ID));
            Insert("UPDATE selected_sp SET Comments = '"+txaComments.getText()+ "' WHERE SID = "+Integer.valueOf(ID)+" AND CID ="+cl.getCID());
            UpdateSPTable();
        }
    }
    
    private void clearTextBoxes(){
        txtSPName.setText("");
        txtPhone.setText("");
        txtEmail.setText("");
        txaComments.setText("");
    }
    
      private void setUpComboBoxRM(){
        
        cmbItemName.setEditable(true);
        cmbItemName.setVisibleRowCount(5);
                
        ObservableList<String> optionsMasonry = 
        FXCollections.observableArrayList(
            "Binding wire",
            "Cement",
            "Coarse aggregates",
            "Concrete block",
            "Concrete Nail",
            "Metal Bar(1/4)",
            "Metal Bar(12\")",
            "nails 2\"",
            "Permofix",
            "Soft aggregates",
            "Timber"
        );

        ObservableList<String> optionsRoofFormwork = 
        FXCollections.observableArrayList(
            "Collar Beams",
            "Conical Roof",
            "Dome",
            "Plates"  
        );   

        ObservableList<String> optionsRoofConcrete = 
        FXCollections.observableArrayList(
            "Concrete"
        );

        ObservableList<String> optionsOpenings = 
        FXCollections.observableArrayList(
            "Aluminium bar" 
        );

        ObservableList<String> optionsElectrical = 
        FXCollections.observableArrayList(
            "AC power plugs",
            "Cables",
            "Circuit breaker",
            "Electrical connector",
            "Electrical wiring",
            "Sockets",
            "Switches"
        );

        ObservableList<String> optionsPlumbing = 
        FXCollections.observableArrayList(
            "Drain",
            "Dual-Piping",
            "Flow Limiter",
            "Four-way Valve",
            "Pipes",
            "Tap",
            "Tubes",
            "Valves"
        );

        ObservableList<String> optionsPainting = 
        FXCollections.observableArrayList(
            "Paint"     
        );

        ObservableList<String> optionsFlooring = 
        FXCollections.observableArrayList(
            "Ceramic tile",
            "Cement render",    
            "Marble",
            "Mosaic",
            "Plaster",
            "Plastic laminate",    
            "Staff- Artificial Stone",
            "Surface finishing",    
            "Veneer", 
            "Wide plank",
            "Wood finishing",
            "Wood pane",
            "Wood stain"       
        );

        if(txtType.getText().compareTo("Masonry")==0){
            cmbItemName.setItems(optionsMasonry);
        }
    
        else if(txtType.getText().compareTo("Roof-Formwork")==0){
            cmbItemName.setItems(optionsRoofFormwork);
        }
        
        else if(txtType.getText().compareTo("Roof-Concrete")==0){
            cmbItemName.setItems(optionsRoofConcrete);
        }
        
        else if(txtType.getText().compareTo("Openings")==0){
            cmbItemName.setItems(optionsOpenings);
        }
        
        else if(txtType.getText().compareTo("Electrical")==0){
            cmbItemName.setItems(optionsElectrical);
        }
        
        else if(txtType.getText().compareTo("Plumbing")==0){
            cmbItemName.setItems(optionsPlumbing);
        }
        
        else if(txtType.getText().compareTo("Plumbing")==0){
            cmbItemName.setItems(optionsPlumbing);
        }
        
        else if(txtType.getText().compareTo("Flooring")==0){
            cmbItemName.setItems(optionsFlooring);
        }
    }

    
    
    
    
    
    
    
    
    
    
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    
    
    
    
    
    
    private void setActionQuo(){
        setComboBox(cmbQuoType);
            cmbQuoType.setOnMouseClicked(e -> {
                cmbQuoType.getSelectionModel().selectedItemProperty().addListener( (options, oldValue, newValue) -> {
                    setUpRequestQuotation(newValue.toString());
                    setUpReceivedQuotation(newValue.toString());
                    
                    lbldatesclash.setText("");
                    lblStartDate.setText("");
                    txtTSDate.clear();
                    
                    try{
//                        System.out.println("SELECT startDate, exist FROM TempStartDate WHERE PID="+cl.getCID()+" AND sp_type = '"+newValue.toString()+"'");
                        rs = stmt.executeQuery("SELECT * FROM TempStartDate WHERE PID="+cl.getCID()+" AND sp_type = '"+newValue.toString()+"'");
                        if(rs.next()){
                            String sd = String.valueOf(rs.getDate("startDate"));
                            exist = rs.getBoolean("exist");
//                            System.out.println("exist :"+exist);

                            if(sd.compareToIgnoreCase("null")!=0){
                                txtTSDate.setText(String.valueOf(sd));
                            }
                            else {
                                lblStartDate.setText("No previous dates available");
                            }
                        }
                            
                        else if (rs.wasNull()){   
                            lblStartDate.setText("No previous dates available");
                            txtTSDate.setText("");
                        }
                    }catch(SQLException ex){}
                });
            });

        setUpClientDetails();
    }

    
    

    //method to set up table to send quotation request with buttons
    private void setUpRequestQuotation(String spType) {
        setUpSPTable(spType);
    }

    //method to setup table for receiving quotation
    private void setUpReceivedQuotation(String spType){
        tblRecQuotation.getItems().clear();
        setUpQuoTable(spType);
    }


    
    private void setUpQuoTable(String spType){
        String sql = "SELECT q.QID,sp.sp_Name,q.startDate, q.Duration, IFNULL(LabourCost,0.00) AS LabourCost, SUM(tCost) AS RawMaterialCost, IFNULL(LabourCost,0.00) + SUM(tCost) AS TotalCost, (((LabourCost+SUM(tCost))/remainingBudget)*100) AS PercentageBudgetUsed \n" +
        "FROM quotation q, selected_sp s, serviceprovider sp, rawmaterial rm, project p \n" +
        "WHERE q.QID=s.QID AND s.SID = sp.SID AND q.QID=rm.QID AND p.PID = s.CID "
        + "AND q.QID IN (SELECT QID FROM selected_sp WHERE quo_status = 'RECEIVED' "
        + "AND CID = "+cl.getCID()+" AND SID IN (SELECT SID FROM serviceprovider WHERE sp_Type = '"+spType+"')) GROUP BY QID;";

        
        try {
            rs = stmt.executeQuery(sql);
            while(rs.next()){
                Double lb = rs.getDouble("LabourCost");

                if(lb!=0.00){
                    fill = new fillTable(tblRecQuotation,sql );
                    break;
                }    
                else {
                    tblRecQuotation.getItems().clear();
                }
            }
        }catch(SQLException e2){}
         
        tblRecQuotation.setOnMouseClicked(e -> {
            if (tblRecQuotation.getSelectionModel().getSelectedItem() != null) {
                selectedIndex = tblRecQuotation.getSelectionModel().getSelectedItem().toString(); //get SELECTED ROW
                selectedIndex = selectedIndex.substring(1, selectedIndex.indexOf(",")); //GET ID OF SELECTED ROW
                
                lbldatesclash.setText("");
                setLabelDateClash();
                
                setUpTaskTable(selectedIndex);
                setUpRawMaterialTable(selectedIndex);
                
                btnAcpQuo.setOnMouseClicked(e1 -> {
                    //System.out.println("quo exist "+exist);
                    //Check is sp_type already exist
                
                    //calculate total cost to decrease remaining budget
                    Date projStartDate = null;
                    Double rmCost = 0.00;
                    Double lbCost = 0.00;
                    Double totalCost;

                    try {
                        rs = stmt.executeQuery("SELECT startDate, SUM(tCost) AS sum, LabourCost\n" +
                        "FROM rawmaterial rm, quotation q\n" +
                        "WHERE rm.QID = q.QID\n" +
                        "AND rm.QID='"+selectedIndex+"'");

                        while (rs.next()){
                            projStartDate = rs.getDate("startDate");
                            rmCost = rs.getDouble("sum");
                            lbCost = rs.getDouble("LabourCost");
                        }
                    }catch(SQLException e2){}

                    totalCost = rmCost + lbCost;

                    // get tempstartdate
                            
                    Date prepRoof = null;
                    Date concRoof = null;
                    Date next = null;
                    
                    if(cmbQuoType.getSelectionModel().getSelectedItem().toString().compareTo("Masonry")==0){
                        try {
                            rs = stmt.executeQuery("SELECT * FROM task WHERE QID='"+selectedIndex+"' AND task_Name = 'Preparation of Roof' ");

                            while (rs.next()){
                                prepRoof = rs.getDate("startDate");
                            }
                        }catch(SQLException e2){}
                        
                        try {
                            rs = stmt.executeQuery("SELECT * FROM task WHERE QID='"+selectedIndex+"' AND task_Name = 'Concrete to Roof' ");

                            while (rs.next()){
                                concRoof = rs.getDate("startDate");
                            }
                        }catch(SQLException e2){}
                        
                    }
                    
                    try {
                        rs = stmt.executeQuery("SELECT * FROM task WHERE QID='"+selectedIndex+"'");

                        while (rs.next()){
                            next = rs.getDate("startDate");
                        }
                    }catch(SQLException e2){}
                    
                    Date endDate = new java.sql.Date(next.getTime() - TimeUnit.DAYS.toMillis(1));

                    
                    if(exist == TRUE){
                        ButtonType okay = new ButtonType("OKAY", ButtonBar.ButtonData.OK_DONE);
                        ButtonType cancel = new ButtonType("CANCEL", ButtonBar.ButtonData.CANCEL_CLOSE);
                        Alert alert = new Alert(Alert.AlertType.WARNING,
                                "You already have a service provider of this type, do you wish to continue?", //put quotation details
                                okay,
                                cancel);

                        alert.setTitle("Confirm sending request");
                        Optional<ButtonType> result = alert.showAndWait();

                        if (result.isPresent() && result.get() == okay) {
                            Update("UPDATE selected_sp SET quo_status = 'ACCEPTED' WHERE QID = '"+selectedIndex+"'");
                           
                            Update("UPDATE project SET remainingBudget = remainingBudget - "+totalCost+" WHERE PID="+cl.getCID()+"");
                            
                            Update("UPDATE TempStartDate SET startDate = '"+next+"' WHERE PID = "+cl.getCID()+" AND predecessor = '"+cmbQuoType.getSelectionModel().getSelectedItem()+"'");
                            
                            Update("UPDATE TempStartDate SET endDate = '"+endDate+"' WHERE PID = "+cl.getCID()+" AND sp_type = '"+cmbQuoType.getSelectionModel().getSelectedItem()+"'");
                            
                            if(cmbQuoType.getSelectionModel().getSelectedItem().toString().compareTo("Masonry")==0){
                                Update("UPDATE TempStartDate SET startDate = '"+prepRoof+"' WHERE sp_type = 'Roof-Formwork' AND PID='"+cl.getCID()+"'");
                            
                                Update("UPDATE TempStartDate SET startDate = '"+concRoof+"' WHERE sp_type = 'Roof-Concrete' AND PID='"+cl.getCID()+"'");
                            
                                Delete("DELETE FROM task WHERE QID= '"+selectedIndex+"' AND task_Name IN ('Preparation of Roof', 'Concrete to Roof')");
                            
                                Update("UPDATE project SET temp_StartDate = '"+projStartDate+"' WHERE PID='"+cl.getCID()+"'");    
                            }
                            
                            Delete("DELETE FROM task WHERE QID='"+selectedIndex+"' AND task_Name = '"+cmbQuoType.getSelectionModel().getSelectedItem()+"'");

                            Notifications notificationBuilder = Notifications.create()
                            .title("Success")
                            .text("Quotation was accepted")
                            .graphic(null)
                            .hideAfter(Duration.seconds(5))
                            .position(Pos.TOP_RIGHT);
                                        //notificationBuilder.darkStyle();
                            notificationBuilder.showInformation();
                            
                            //send notification to sp , quotation accepted
                            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                            Date dateobj = new Date();
                            
                            
                            String sid = selectedIndex.substring(String.valueOf(cl.getCID()).length()+1);
                            Insert("INSERT INTO notification(notificationType, receiverType, senderID, receiverID, details, dateSent) VALUES('Quotation accepted','serviceprovider',"+cl.getCID()+","+sid+",'Quotation accepted','"+df.format(dateobj)+"')");

                            
                            myController.reloadScreen(ScreensFramework.screen4ID,ScreensFramework.screen4File);
                        }
                    }
                    
                    else { //new SP
                        Update("UPDATE selected_sp SET quo_status = 'ACCEPTED' WHERE QID = '"+selectedIndex+"'");
                        Update("UPDATE TempStartDate SET exist= 1 WHERE sp_type='"+cmbQuoType.getSelectionModel().getSelectedItem()+"'");
//                        Update("UPDATE TempStartDate SET startDate = (SELECT ADDDATE(startDate,Duration) FROM quotation WHERE QID='"+selectedIndex+"') WHERE predecessor='"+cmbQuoType.getSelectionModel().getSelectedItem()+"'");

                        Update("UPDATE project SET remainingBudget = remainingBudget - "+totalCost+" WHERE PID="+cl.getCID()+"");
                        
                        Update("UPDATE TempStartDate SET startDate = '"+next+"' WHERE PID = "+cl.getCID()+" AND predecessor = '"+cmbQuoType.getSelectionModel().getSelectedItem()+"'");
                        Update("UPDATE TempStartDate SET endDate = '"+endDate+"' WHERE PID = "+cl.getCID()+" AND sp_type = '"+cmbQuoType.getSelectionModel().getSelectedItem()+"'");
                                
                        if(cmbQuoType.getSelectionModel().getSelectedItem().toString().compareTo("Masonry")==0){
                            Update("UPDATE TempStartDate SET startDate = '"+prepRoof+"' WHERE sp_type = 'Roof-Formwork' AND PID='"+cl.getCID()+"'");

                            Update("UPDATE TempStartDate SET startDate = '"+concRoof+"' WHERE sp_type = 'Roof-Concrete' AND PID='"+cl.getCID()+"'");

                            Delete("DELETE FROM task WHERE QID= '"+selectedIndex+"' AND task_Name IN ('Preparation of Roof', 'Concrete to Roof')");
                        
                            Update("UPDATE project SET temp_StartDate = '"+projStartDate+"' WHERE PID='"+cl.getCID()+"'");    
                        }

                        Delete("DELETE FROM task WHERE QID='"+selectedIndex+"' AND task_Name = '"+cmbQuoType.getSelectionModel().getSelectedItem()+"'");

                        
                        Notifications notificationBuilder = Notifications.create()
                        .title("Sucess")
                        .text("Quotation was accepted")
                        .graphic(null)
                        .hideAfter(Duration.seconds(5))
                        .position(Pos.TOP_RIGHT);
                                    //notificationBuilder.darkStyle();
                        notificationBuilder.showInformation(); 
                        
                        myController.reloadScreen(ScreensFramework.screen4ID,ScreensFramework.screen4File); 
                        myController.refreshScreen(ScreensFramework.screen1ID,ScreensFramework.screen1File); 
                        myController.refreshScreen(ScreensFramework.screen3ID,ScreensFramework.screen3File);
                        myController.refreshScreen(ScreensFramework.screen5ID,ScreensFramework.screen5File);

                    }
                                        
                });
                
                btnRefQuo.setOnMouseClicked(e1 -> {
                    
//                    Update("UPDATE selected_sp SET quo_status = 'REFUSED' WHERE QID = '"+selectedIndex+"'");
                    Delete("DELETE FROM selected_sp WHERE QID = '"+selectedIndex+"'");
                    Delete("DELETE FROM task WHERE QID = '"+selectedIndex+"'");
                    Delete("DELETE FROM rawmaterial WHERE QID = '"+selectedIndex+"'");
                    Delete("DELETE FROM quotation WHERE QID = '"+selectedIndex+"'");
                    
                    myController.reloadScreen(ScreensFramework.screen4ID,ScreensFramework.screen4File); 
                    
                    
                    Notifications notificationBuilder = Notifications.create()
                    .title("Sucess")
                    .text("Quotation was refused")
                    .graphic(null)
                    .hideAfter(Duration.seconds(5))
                    .position(Pos.TOP_RIGHT);
                                //notificationBuilder.darkStyle();
                    notificationBuilder.showInformation(); 
                });
                
            }     
        });
    }
    
    private void setLabelDateClash(){
        
        int countTask;
        //check if dates clash
        try {
            rs = stmt.executeQuery("SELECT COUNT(TID) as countTask FROM task WHERE QID='"+selectedIndex+"'");
            countTask = rs.getInt("countTask");

        } catch (SQLException ex) {}

        ArrayList<Integer> taskID = new ArrayList<Integer>();
        ArrayList<String> taskStD = new ArrayList<String>();
        ArrayList<Integer> taskDur = new ArrayList<Integer>();


        try{
            rs = stmt.executeQuery("SELECT TID, startDate,duration,ADDDATE(startDate, INTERVAL duration-1 DAY) AS endDate FROM task WHERE task_details<>'Date for successor' AND QID='"+selectedIndex+"'");
            while(rs.next()){
                taskID.add(rs.getInt("TID"));
                Date sd = rs.getDate("startDate");
                taskStD.add(String.valueOf(sd));
                taskDur.add(rs.getInt("duration"));
            }
        }catch(SQLException e2){}
                    
        for(int i=0; i<taskID.size();i++){
//                    if(available==FALSE){
//                                System.out.println("Date unavailable");
//                                break;
//                            }
            available = checkDates(taskStD.get(i), taskDur.get(i));
//                    System.out.println(i+"\tavailable "+ available);
            if(available==FALSE){
//                        System.out.println("Date unavailable");
                lbldatesclash.setText("This quotation has a task which clashes with your list of accepted tasks");
                break;
            }
        }
    }
    
    private boolean checkDates(String strstartdate, int duration){
        
        available = TRUE;
        
        String taskName;
        java.sql.Date startdate, enddate;
        java.sql.Date tstartdate = java.sql.Date.valueOf(strstartdate);;
        java.sql.Date tenddate = new java.sql.Date(tstartdate.getTime() + TimeUnit.DAYS.toMillis(duration-1));
        
        try{
            rs = stmt.executeQuery("SELECT TID,task_Name, task.QID,startDate,duration,ADDDATE(startDate, INTERVAL duration-1 DAY) AS endDate\n" +
            "FROM task,selected_sp\n" +
            "WHERE task.QID = selected_sp.QID AND selected_sp.QID <> '"+csp.getQID()+"' AND quo_status='ACCEPTED' AND CID="+cl.getCID());
            
            while(rs.next()){
                if(available==FALSE){
                    break;
                }
                startdate = rs.getDate("startDate");
                enddate = rs.getDate("endDate");
                taskName = rs.getString("task_Name");
               // System.out.println("Task Name "+taskName+" StartDate: "+startdate+"\t EndDate: "+enddate);

                //if((tDate.before(enddate)) && (tDate.after(startdate))){
                if(((tstartdate.compareTo(startdate)<=0) && (tenddate.compareTo(enddate)<=0) && (tenddate.compareTo(startdate)>=0)) || ((tstartdate.compareTo(startdate)<=0) && (tenddate.compareTo(enddate)>=0)) || ((tstartdate.compareTo(startdate)>=0) && (tenddate.compareTo(enddate)<=0)) || ((tstartdate.compareTo(startdate)>=0) && (tstartdate.compareTo(enddate)<=0) && (tenddate.compareTo(enddate)>0))){
//                        System.out.println("Available: FALSE");
                    available = FALSE;
                    //lblCheckDates.setText("Unavailable Time Slot");
                }
            }
                 
        }catch(SQLException e2){}
        
        return available;
        
    }   
    
    private void setUpClientDetails(){
        txtCName.setDisable(true);
        txtCAddr.setDisable(true);
        txtCTel.setDisable(true);
        txtCEmail.setDisable(true);
        txtSID.setDisable(true);
        try{
            rs = stmt.executeQuery("SELECT cName, telephone, email, p.address FROM userclient c , project p WHERE c.CID = p.CID AND PID = "+cl.getCID());
            while (rs.next()){
                txtCName.setText(rs.getString("cName"));
                txtCTel.setText(String.valueOf(rs.getInt("telephone")));
                txtCEmail.setText(rs.getString("email"));
                txtCAddr.setText(rs.getString("address"));
            }
        }catch(SQLException e){}
        

    }
    
    private void setUpTaskTable(String qid){
        String sql1 = "SELECT * FROM task WHERE QID = '"+qid+"' AND task_Name <> '"+cmbQuoType.getSelectionModel().getSelectedItem()+"'";
        String sql2 = "SELECT * FROM task WHERE QID = '"+qid+"' AND task_Name <> '"+cmbQuoType.getSelectionModel().getSelectedItem()+"' AND task_Name NOT IN ('Preparation of Roof', 'Concrete to Roof')";
        
        String sql = sql1;
        
        if(cmbQuoType.getSelectionModel().getSelectedItem().toString().compareTo("Masonry")==0){
            sql = sql2;
        }
        fill = new fillTable(tblQuoTask, sql);

    }
    
    private void setUpRawMaterialTable(String qid){
        fill = new fillTable(tblQuoRM, "SELECT * FROM rawmaterial WHERE QID = '"+qid+"'");
    }
    
    private void setUpSPTable(String spType){
        fill = new fillTable(tblSPReqQuo, "SELECT * FROM serviceprovider WHERE sp_Type = '"+spType+"' AND usesApp = 1 AND SID NOT IN (SELECT SID FROM selected_sp WHERE CID ="+cl.getCID()+" )");
      
        tblSPReqQuo.setOnMouseClicked(e -> {
            lblStartDate.setText("");
            
            if (tblSPReqQuo.getSelectionModel().getSelectedItem() != null) {
                selectedIndex = tblSPReqQuo.getSelectionModel().getSelectedItem().toString(); //get SELECTED ROW
                selectedIndex = selectedIndex.substring(1, selectedIndex.indexOf(",")); //GET ID OF SELECTED ROW
                csp.setSID(Integer.valueOf(selectedIndex));
                txtSID.setText(String.valueOf(csp.getSID()));
                
                txtSPType.setText(cmbQuoType.getSelectionModel().getSelectedItem().toString());
                        
                
                btnViewReview.setOnMouseClicked(e1 -> {
                    if(tblSPReqQuo.getSelectionModel().getSelectedItem() == null){
                        Notifications notificationBuilder = Notifications.create()
                        .title("Error")
                        .text("Select a service provider.")
                        .graphic(null)
                        .hideAfter(Duration.seconds(5))
                        .position(Pos.TOP_RIGHT);
                        notificationBuilder.showError(); 
                    }
                    else{
                        try {
                            AnchorPane root = (AnchorPane) FXMLLoader.load(getClass().getResource("ViewReview.fxml"));
                            Stage secondStage = new Stage();
                            secondStage.setScene(new Scene(root));
                            secondStage.setTitle("Reviews");
                            secondStage.show();
                        } catch (IOException ex) {
                            Logger.getLogger(SP_ScreenController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                });
                
                btnSendReq.setOnMouseClicked(e2 -> {
                    if(exist == TRUE){
                        ButtonType okay = new ButtonType("OKAY", ButtonBar.ButtonData.OK_DONE);
                        ButtonType cancel = new ButtonType("CANCEL", ButtonBar.ButtonData.CANCEL_CLOSE);
                        Alert alert = new Alert(Alert.AlertType.WARNING,
                                "You already have a service provider of this type, do you wish to continue?", //put quotation details
                                okay,
                                cancel);

                        alert.setTitle("Confirm sending request");
                        Optional<ButtonType> result = alert.showAndWait();

                        if (result.isPresent() && result.get() == okay) {
                            Notifications notificationBuilder = Notifications.create()
                            .title("Success")
                            .text("Request was sent.")
                            .graphic(null)
                            .hideAfter(Duration.seconds(5))
                            .position(Pos.TOP_RIGHT);
                                        //notificationBuilder.darkStyle();
                            notificationBuilder.showInformation(); 
                            Insert("INSERT INTO quotationrequest(CID, SID, Notes, req_status, tempStartDate) VALUES ("+cl.getCID()+","+csp.getSID()+",'"+txaQuoComments.getText()+"','Pending','"+txtTSDate.getText()+"')");
                        }
                    }else{
                        ButtonType okay = new ButtonType("OKAY", ButtonBar.ButtonData.OK_DONE);
                        ButtonType cancel = new ButtonType("CANCEL", ButtonBar.ButtonData.CANCEL_CLOSE);
                        Alert alert = new Alert(Alert.AlertType.WARNING,
                                "Do you wish to continue?", //put quotation details
                                okay,
                                cancel);

                        alert.setTitle("Confirm sending request");
                        Optional<ButtonType> result = alert.showAndWait();

                        if (result.isPresent() && result.get() == okay) {
                            Notifications notificationBuilder = Notifications.create()
                            .title("Success")
                            .text("Request was sent.")
                            .graphic(null)
                            .hideAfter(Duration.seconds(5))
                            .position(Pos.TOP_RIGHT);
                                        //notificationBuilder.darkStyle();
                            notificationBuilder.showInformation(); 
                            Insert("INSERT INTO quotationrequest(CID, SID, Notes, req_status, tempStartDate) VALUES ("+cl.getCID()+","+csp.getSID()+",'"+txaQuoComments.getText()+"','Pending','"+txtTSDate.getText()+"')");
                        }
                    }
                });
            }

            
        });
        


    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    @FXML
    private void changeScreen(ActionEvent event) {
         Button btn = (Button) event.getSource();
         switch(btn.getText()){
            case "Dashboard" : myController.setScreen(ScreensFramework.screen1ID);
            break;
            case "Plan" : myController.setScreen(ScreensFramework.screen2ID);
            break;
            case "Task" : myController.setScreen(ScreensFramework.screen3ID);
            break;
            case "Service Provider" : myController.setScreen(ScreensFramework.screen4ID);
            break;
            case "Expense" : myController.setScreen(ScreensFramework.screen5ID);
            break;
            case "Photos" : myController.setScreen(ScreensFramework.screen6ID);
            break;
            case "Calculator" : myController.setScreen(ScreensFramework.screen7ID);
            break;
         }
    }

    public void setScreenParent(ScreensController screenParent){
        myController = screenParent;
    }
}
