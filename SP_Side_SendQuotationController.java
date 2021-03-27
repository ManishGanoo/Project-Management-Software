import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.sql.Blob;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Duration;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.controlsfx.control.Notifications;
import org.icepdf.ri.common.SwingController;
import org.icepdf.ri.common.SwingViewBuilder;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.IntervalCategoryDataset;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;
import org.jfree.data.time.SimpleTimePeriod;


public class SP_Side_SendQuotationController extends dbManipulation implements Initializable, ControlledScreen{

    ScreensController myController;
    
    @FXML DatePicker dtpTaskSDate, dtpDateStart;
    @FXML TextField dtpTaskSDate1, txtDuration,txtRMTotal,  txtLCost, txtItemName, txtItemPrice, txtItemQuantity, txtTaskName,  txtTaskDetails, txtTaskDuration;
    @FXML Button    btnAddRM, btnRemoveRM, btnSendQuo, btnAddTask,  btnRemoveTask;
    @FXML TableView tblRawMaterial, tblTask;

    @FXML Label lblCheckDates, lblMasRoof;
    
    //pdf
    @FXML ComboBox  cmbChoosePlan;
    @FXML SwingNode swingNode, gswingNode;  
    private SwingController swingController;
    private JComponent viewerPanel;  
    private static final int BUFFER_SIZE = 4096;
    ObservableList<Object> dataPlan = FXCollections.observableArrayList();
            
//    Vector<RMaterial> rawmVec = new Vector<>();
    ObservableList<RMaterial> rmObsList = FXCollections.observableArrayList();
    ObservableList<Task> taskObsList = FXCollections.observableArrayList();
    
    //room details
    private fillTable fill;
    @FXML TableView tblRooms, tblOpenings;
    @FXML Tab tabRoom, tabGantt;
    
    CurrentSPDetails csp;
    Client cl;
    
    String selectedClientIndex;
   
    String ori_sql = "SELECT * FROM TASK ";
    
    int totalDays;
    Double RMTotal=0.00;
    int durationtotal = 0;

    @FXML ComboBox cmbItemName;
    @FXML Label lblUnitCost;
    @FXML TextField txtUnitCost;

    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //cid has been set by quotation screen
        cl = Context.getInstance().currentClient();  

        selectedClientIndex = String.valueOf(cl.getCID());
        
        csp = Context.getInstance().currentSPDetails();
        txtDuration.setEditable(false);
        txtRMTotal.setEditable(false);
        
        System.out.println(csp.getSpType());
//        
//        if(csp.getSpType()=="Masonry"){
//            lblMasRoof.setText("Provide temporary dates for Roofing Works");
//        }
        
        setUpComboBoxRM();
        createViewer();
        
        setUpComboBoxPlan();
        
        tabGantt.setOnSelectionChanged(e -> {
            createAndSetSwingDrawingPanel(gswingNode);
        });
                
        tabRoom.setOnSelectionChanged(e -> {
            setUpRoomDetails();
        });
        
        setRM();
        
        setDefaultTaskList();
        tblTask.setItems(taskObsList);
        createTaskTable();
        setTask();
        
        setbtnSendQuo();
        setComboBoxPlan();
        CalcLabourCost();
    }    
    
    private void setbtnSendQuo(){
        btnSendQuo.setOnMouseClicked(e ->{
            if(Validation.checkSendQuotation(dtpDateStart.getValue(), txtDuration.getText(), txtLCost.getText(), rmObsList, taskObsList)){
                csp.setQID("Q"+cl.getCID()+csp.getSID());

                //if textbox empty send message,

                //display option if user agrees to send quotation then
                ButtonType okay = new ButtonType("OKAY", ButtonBar.ButtonData.OK_DONE);
                ButtonType cancel = new ButtonType("CANCEL", ButtonBar.ButtonData.CANCEL_CLOSE);
                Alert alert = new Alert(AlertType.WARNING,
                        "Are you sure you want to proceed in sending the quotation?", //put quotation details
                        okay,
                        cancel);

                alert.setTitle("Confirm sending request");
                Optional<ButtonType> result = alert.showAndWait();

                if (result.isPresent() && result.get() == okay) {
    //                System.out.println("Insert quotation in db");

                    //copied from quotation btn accept
                    Update("UPDATE quotationrequest SET req_status = 'Received' WHERE SID = "+csp.getSID()+" AND CID = "+selectedClientIndex);

                    //  

                    Insert("INSERT INTO quotation(QID, startDate, Duration, LabourCost) VALUES ('"+csp.getQID()+"','"+dtpDateStart.getValue()+"',"+txtDuration.getText()+","+txtLCost.getText()+")"); 
                    Insert("INSERT INTO selected_sp(CID, SID, QID, Comments, quo_status) VALUES("+cl.getCID()+","+csp.getSID()+",'"+csp.getQID()+"','', 'RECEIVED')");

                    //getData from raw material table and insert in db for last QID for CID
                    Iterator<RMaterial> iter = rmObsList.iterator();
                    while(iter.hasNext()){
                        RMaterial g = iter.next();
                        Insert("INSERT INTO rawmaterial(QID, itemName, price, quantity, tCost) VALUES ('"+csp.getQID()+"','"+g.getItemName()+"', "+g.getItemPrice()+","+g.getItemQuantity()+","+g.getTotalCost()+")");
                    }
                    
                    Date tstartdate = null;
                    
                    Iterator<Task> iterT = taskObsList.iterator();
                    while(iterT.hasNext()){
                        Task g = iterT.next();
                        //g.toString();
                        if(csp.getSpType().compareTo("Masonry")!= 0){ //spType != masonry
                            Insert("INSERT INTO task(QID, task_Name, task_details, startDate, duration, task_status) VALUES ('"+csp.getQID()+"','"+g.getTaskName()+"','"+g.getTaskDetails()+"','"+g.getTaskDate()+"',"+g.getTaskDuration()+",'Pending')");                                                                                                                               
                            
                            tstartdate = Date.valueOf(g.getTaskDate());
                            totalDays=Integer.valueOf(g.getTaskDuration());

                        }

                        else {  //sp_Type == Masonry

                            if(g.getTaskName().equalsIgnoreCase("Preparation of Roof")){
//                                Insert("INSERT INTO tempStartDate(PID, sp_type, startDate) VALUES ("+cl.getCID()+",'Roof-Formwork','"+g.getTaskDate()+"')");
//                                totalDays+=Integer.valueOf(g.getTaskDuration());
                                Insert("INSERT INTO task(QID, task_Name, task_details, startDate, duration, task_status) VALUES ('"+csp.getQID()+"','"+g.getTaskName()+"','"+g.getTaskDetails()+"','"+g.getTaskDate()+"',"+g.getTaskDuration()+",'Pending')"); 
                            }

                            else if(g.getTaskName().equalsIgnoreCase("Concrete to Roof")){
//                                Insert("INSERT INTO tempStartDate(PID, sp_type, startDate) VALUES ("+cl.getCID()+",'Roof-Concrete','"+g.getTaskDate()+"')");
                                Insert("INSERT INTO task(QID, task_Name, task_details, startDate, duration, task_status) VALUES ('"+csp.getQID()+"','"+g.getTaskName()+"','"+g.getTaskDetails()+"','"+g.getTaskDate()+"',"+g.getTaskDuration()+",'Pending')");                                                                                                                              
                            }

                            else {
                                Insert("INSERT INTO task(QID, task_Name, task_details, startDate, duration, task_status) VALUES ('"+csp.getQID()+"','"+g.getTaskName()+"','"+g.getTaskDetails()+"','"+g.getTaskDate()+"',"+g.getTaskDuration()+",'Pending')");                                                                                                                               
            
                                tstartdate = Date.valueOf(g.getTaskDate());
                                totalDays=Integer.valueOf(g.getTaskDuration());

                            }
                        }
                    }


//                    Date tstartdate = Date.valueOf(dtpDateStart.getValue());;
                    Date tnextdate = new Date(tstartdate.getTime() + TimeUnit.DAYS.toMillis(totalDays));
//                    if(csp.getSpType().compareTo("Masonry")!= 0){ //spType != masonry
//                        Update("UPDATE TempStartDate SET startDate='"+tnextdate+"' WHERE predecessor='"+csp.getSpType()+"'");
//                    }
//                    else { //Masonry
//                      Update("UPDATE TempStartDate SET startDate='"+tnextdate+"' WHERE predecessor='Openings'");
//                    }

                    Insert("INSERT INTO task(QID, task_Name, task_details, startDate, duration, task_status) VALUES ('"+csp.getQID()+"','"+csp.getSpType()+"','Date for successor','"+tnextdate+"',0,'Pending')");                                                                                                                               
                    
                    //go back to previous screen
                    myController.reloadScreen(ScreensFramework.SP_Side_QuotationID, ScreensFramework.SP_Side_QuotationFile);
                }
            } 
        });
    }

    private void setComboBoxPlan(){
        dataPlan.clear();
        try{
        rs = stmt.executeQuery("SELECT * FROM pdfplan WHERE PID = "+cl.getCID() );
            while (rs.next()){
                dataPlan.add(rs.getString("planName"));
            }
        }catch(SQLException e){}
        cmbChoosePlan.setPromptText("Select Plan");
        cmbChoosePlan.setItems(dataPlan);
        cmbChoosePlan.getEditor().textProperty().addListener((obs, oldText, newText) -> {
            int selectedIndex;
            selectedIndex = cmbChoosePlan.getSelectionModel().getSelectedIndex();
            readpdffromdb(dataPlan.get(selectedIndex).toString());
        });
    }
    
   
    
    private void setRM(){
        createRawMaterialTable();
        setUpComboBoxRM();
        
        btnAddRM.setOnMouseClicked(e -> {
            if(Validation.checkaddrm(cmbItemName.getSelectionModel().getSelectedItem().toString(),txtItemPrice.getText(), txtItemQuantity.getText(), rmObsList)){
                RMaterial rm = new RMaterial(cmbItemName.getSelectionModel().getSelectedItem().toString(),txtItemPrice.getText(), txtItemQuantity.getText(), String.valueOf(Double.valueOf(txtItemPrice.getText()) * Integer.valueOf(txtItemQuantity.getText())));          
                rmObsList.add(rm);
                
                RMTotal += Double.valueOf(txtItemPrice.getText()) * Integer.valueOf(txtItemQuantity.getText());
                txtRMTotal.setText(String.valueOf(RMTotal));

                updateRMTable();
                clearRMTextBoxes();
            }

        });
        
        btnRemoveRM.setOnMouseClicked(e1 -> {
            int selectedIndex = tblRawMaterial.getSelectionModel().getSelectedIndex();
            if (selectedIndex >= 0) {
                RMTotal -= Double.parseDouble(rmObsList.get(selectedIndex).getTotalCost());
                txtRMTotal.setText(String.valueOf(RMTotal));    

//                tblRawMaterial.getItems().remove(selectedIndex); //delete from table            
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


    }
    
    
    private void createRawMaterialTable() {
            
        tblRawMaterial.setEditable(true);
        
        TableColumn nameCol = new TableColumn("Item Name");
        TableColumn priceCol = new TableColumn("Price");
        TableColumn qtyCol = new TableColumn("Quantity");
        TableColumn tcostCol = new TableColumn("Total Cost");
                
        nameCol.setCellValueFactory(new PropertyValueFactory<RMaterial,String>("itemName"));
        priceCol.setCellValueFactory(new PropertyValueFactory<RMaterial,String>("itemPrice"));
        qtyCol.setCellValueFactory(new PropertyValueFactory<RMaterial,String>("itemQuantity"));
        tcostCol.setCellValueFactory(new PropertyValueFactory<RMaterial,String>("totalCost"));
        
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
                  //      String price = t.getTableView().getItems().get(t.getTablePosition().getRow()).getItemPrice();
                        String price = t.getNewValue();
                        String quantity = t.getTableView().getItems().get(t.getTablePosition().getRow()).getItemQuantity();

                        int indexRM = tblRawMaterial.getSelectionModel().getFocusedIndex();
               //         System.out.print("indexRM: "+indexRM);
                        RMTotal -= Double.parseDouble(rmObsList.get(indexRM).getTotalCost());

                        RMaterial rm = new RMaterial(name,price,quantity,String.valueOf(Double.valueOf(price)* Double.valueOf(quantity)));
                        rmObsList.set(indexRM, rm);
                        updateRMTable();

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
    //                    String quantity = t.getTableView().getItems().get(t.getTablePosition().getRow()).getItemQuantity();
                        String quantity = t.getNewValue();

                        int indexRM = tblRawMaterial.getSelectionModel().getFocusedIndex();

                        RMTotal -= Double.parseDouble(rmObsList.get(indexRM).getTotalCost());

                        RMaterial rm = new RMaterial(name,price,quantity,String.valueOf(Double.valueOf(price)* Double.valueOf(quantity)));
                        rmObsList.set(indexRM, rm);
                        updateRMTable();
                        RMTotal += Double.parseDouble(rmObsList.get(indexRM).getTotalCost());
                        txtRMTotal.setText(String.valueOf(RMTotal));

                    }
                    tblRawMaterial.refresh();
                }
            }
        );
        

        
        nameCol.setMinWidth(187);
        priceCol.setMinWidth(187);
        qtyCol.setMinWidth(187);
        tcostCol.setMinWidth(312);
        
        tblRawMaterial.setItems(rmObsList);
        
        tblRawMaterial.getColumns().addAll(nameCol,priceCol,qtyCol,tcostCol);
        
    }
       
    private void updateRMTable(){
        tblRawMaterial.setItems(rmObsList);
    }
    
    private void clearRMTextBoxes() {
        cmbItemName.getSelectionModel().clearSelection();
//        cmbItemName.getItems().clear( );
        cmbItemName.setValue(null);        

     
        txtItemPrice.setText("");
        txtItemQuantity.setText("");
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

        if(csp.getSpType().compareTo("Masonry")==0){
            cmbItemName.setItems(optionsMasonry);
        }
    
        else if(csp.getSpType().compareTo("Roof-Formwork")==0){
            cmbItemName.setItems(optionsRoofFormwork);
        }
        
        else if(csp.getSpType().compareTo("Roof-Concrete")==0){
            cmbItemName.setItems(optionsRoofConcrete);
        }
        
        else if(csp.getSpType().compareTo("Openings")==0){
            cmbItemName.setItems(optionsOpenings);
        }
        
        else if(csp.getSpType().compareTo("Electrical")==0){
            cmbItemName.setItems(optionsElectrical);
        }
        
        else if(csp.getSpType().compareTo("Plumbing")==0){
            cmbItemName.setItems(optionsPlumbing);
        }
        
        else if(csp.getSpType().compareTo("Plumbing")==0){
            cmbItemName.setItems(optionsPlumbing);
        }
        
        else if(csp.getSpType().compareTo("Flooring")==0){
            cmbItemName.setItems(optionsFlooring);
        }
    }

    
    
    private void setDefaultTaskList(){
        
       // System.out.println("Default Task List");
        if(csp.getSpType().equals("Masonry")){
            int result1 = checkIfTaskDone("Roof-Formwork");
            if(result1 == 1){
                int result2 = checkIfTaskDone("Roof-Concrete");
                if(result2 == 0){
                    lblMasRoof.setText("Provide temporary dates for Roofing Works");
                }
            }
            else {
               lblMasRoof.setText("Provide temporary dates for Roofing Works"); 
            }
        }
        String query1 = "SELECT taskID,taskName FROM defaultTasks WHERE serviceProviderType IN ('Masonry','Roof-Formwork','Roof-Concrete')";
        String query2 = "SELECT taskID,taskName FROM defaultTasks WHERE serviceProviderType='"+csp.getSpType()+"'";
        
        String selectedQuery = query2;
        
        if(csp.getSpType().compareTo("Masonry")==0){
            selectedQuery = query1;
        }
        
        try{
            rs = stmt.executeQuery(selectedQuery);

            while (rs.next()){
                String taskID = String.valueOf(rs.getInt("taskID"));
                String taskName = rs.getString("taskName");

                taskObsList.add(new Task(taskID,taskName,"","","0","Pending"));
            } 
        }catch(SQLException e){}
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
            
    private void setTask(){
        //createTaskTable();
        
        txtTaskName.setOnMouseClicked(e2 ->{
            lblCheckDates.setText("");
        });
        
        btnAddTask.setOnMouseClicked(e1 ->{
            try {
                if(Validation.checkTaskDetails(txtTaskName.getText(),txtTaskDetails.getText(), dtpTaskSDate.getValue(), txtTaskDuration.getText(), "Pending")){
                    int d = Integer.valueOf(txtTaskDuration.getText());
                    
                    taskObsList.add(new Task("",txtTaskName.getText(),txtTaskDetails.getText(),String.valueOf(dtpTaskSDate.getValue()), txtTaskDuration.getText(), "Pending"));
                    checkDates(String.valueOf(dtpTaskSDate.getValue()),d);
                    durationtotal += Integer.parseInt(txtTaskDuration.getText());
                    txtDuration.setText(String.valueOf(durationtotal));

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
                          //  System.out.println("result: "+result);
                        if(result == 1){
                            taskObsList.remove(selectedIndex);  //delete from vector  
                            //tblTask.getItems().remove(selectedIndex); //delete from table 
    //                                    updateTaskTable();
                        }
                    }
                    else {
                        durationtotal -= Integer.parseInt(taskObsList.get(selectedIndex).getTaskDuration());
                        txtDuration.setText(String.valueOf(durationtotal));

                        taskObsList.remove(selectedIndex); 
                    }

                   //updateTaskTable();
                }
    //                    
                else {   //Masonry
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
    
    private void updateTaskTable(){
        tblTask.setItems(taskObsList);
    }
    
    private void createTaskTable(){
        tblTask.setEditable(true);
                       
        TableColumn tnameCol = new TableColumn("Task Name");
        TableColumn tdetailCol = new TableColumn("Task Details");
        TableColumn tdurationCol = new TableColumn("Duration");
        TableColumn tsdateCol = new TableColumn("Start Date");

                
        tnameCol.setCellValueFactory(new PropertyValueFactory<Task,String>("taskName"));
        tdetailCol.setCellValueFactory(new PropertyValueFactory<Task,String>("taskDetails"));
        tdurationCol.setCellValueFactory(new PropertyValueFactory<Task,String>("taskDuration"));
        tsdateCol.setCellValueFactory(new PropertyValueFactory<Task,String>("taskDate"));
        
        tnameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        tnameCol.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<Task, String>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<Task, String> t) {
                    if(Validation.checkEmptyString(t.getNewValue())){
                        ((Task) t.getTableView().getItems().get(
                                t.getTablePosition().getRow())
                                ).setTaskName(t.getNewValue());
                    }
                    tblTask.refresh();
                }
            }
        );
        
        tdetailCol.setCellFactory(TextFieldTableCell.forTableColumn());
        tdetailCol.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<Task, String>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<Task, String> t) {
                    if(Validation.checkEmptyString(t.getNewValue())){
                        ((Task) t.getTableView().getItems().get(
                                t.getTablePosition().getRow())
                                ).setTaskDetails(t.getNewValue());
                    }
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
                String sdate = t.getTableView().getItems().get(t.getTablePosition().getRow()).getTaskDate();

                if(!sdate.isEmpty()){
                    if(csp.getSpType().compareTo("Masonry")==0){
                        if ((t.getTableView().getItems().get(t.getTablePosition().getRow()).getTaskName().compareTo("Preparation of Roof")!=0) && (t.getTableView().getItems().get(t.getTablePosition().getRow()).getTaskName().compareTo("Concrete to Roof"))!=0){
                            checkDates(sdate,Integer.valueOf(t.getNewValue()));
                            durationtotal = durationtotal - Integer.parseInt(t.getOldValue()) + Integer.parseInt(t.getNewValue());
                            txtDuration.setText(String.valueOf(durationtotal));
//                            System.out.println("old "+t.getOldValue());
//                            System.out.println("new "+t.getNewValue());
//                            System.out.println("duration " + durationtotal);
                        }
                    }
                    else {  //Not Masonry
                        checkDates(sdate,Integer.valueOf(t.getNewValue()));
                        durationtotal = durationtotal - Integer.parseInt(t.getOldValue()) + Integer.parseInt(t.getNewValue());
                        txtDuration.setText(String.valueOf(durationtotal));
                    }

                    ((Task) t.getTableView().getItems().get(
                        t.getTablePosition().getRow())
                        ).setTaskDuration(t.getNewValue());

                    LocalDate currentStartDate = LocalDate.parse(sdate);	
                    LocalDate nextStartDate = currentStartDate.plusDays(Integer.parseInt(t.getNewValue()));

                    int indexT = tblTask.getSelectionModel().getFocusedIndex();
                    if(indexT < tblTask.getItems().size()-1){
                        ((Task) t.getTableView().getItems().get(indexT+1)
                        ).setTaskDate(String.valueOf(nextStartDate));
                    }

                }
                 tblTask.refresh();  
            }
        });
        

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
                    if(Validation.checkDateStringFormat(t.getNewValue())){
                        ((Task) t.getTableView().getItems().get(
                                t.getTablePosition().getRow())
                                ).setTaskDate(t.getNewValue());

                        String dur = t.getTableView().getItems().get(t.getTablePosition().getRow()).getTaskDuration();
                        if(!dur.isEmpty()){
                            int duration = Integer.parseInt(dur);
    //                        System.out.println("Duration: "+duration);

                            checkDates(t.getNewValue(),duration);
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

        //tblTask.setItems(taskObsList);
        
        tblTask.getColumns().addAll(tnameCol,tdetailCol,tsdateCol,tdurationCol);
    }
    
  
    private void clearTaskTextBoxes() {
        txtTaskName.setText("");
        txtTaskDetails.setText("");
        txtTaskDuration.setText("");
        
        dtpTaskSDate.setPromptText("");
    }
    
    
    
    private void checkDates(String strstartdate, int duration){
        
        boolean available = TRUE;
        Date startdate, enddate;
        Date tstartdate = Date.valueOf(strstartdate);;
        Date tenddate = new Date(tstartdate.getTime() + TimeUnit.DAYS.toMillis(duration));
        
        try{
            rs = stmt.executeQuery("SELECT TID,task.QID,startDate,duration,ADDDATE(startDate, INTERVAL duration DAY) AS endDate\n" +
            "FROM task,selected_sp\n" +
            "WHERE task.QID = selected_sp.QID AND SID="+csp.getSID());

            while(rs.next()){
                if(available==FALSE){
                    break;
                }
                startdate = rs.getDate("startDate");
                enddate = rs.getDate("endDate");

//                System.out.println("StartDate: "+startdate+"\t EndDate: "+enddate);

                //if((tDate.before(enddate)) && (tDate.after(startdate))){
                if(((tstartdate.compareTo(startdate)<=0) && (tenddate.compareTo(enddate)<=0) && (tenddate.compareTo(startdate)>=0)) || ((tstartdate.compareTo(startdate)<=0) && (tenddate.compareTo(enddate)>=0)) || ((tstartdate.compareTo(startdate)>=0) && (tenddate.compareTo(enddate)<=0)) || ((tstartdate.compareTo(startdate)>=0) && (tstartdate.compareTo(enddate)<=0) && (tenddate.compareTo(enddate)>0))){
//                        System.out.println("Available: FALSE");
                        available = FALSE;
                        lblCheckDates.setText("Unavailable Time Slot");
                }

            }
                 
        }catch(SQLException e2){}
        
    }
    
    private void CalcLabourCost(){
        
        txtUnitCost.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
            Double totalTSA = 0.00;
                if(!newValue.isEmpty()){
                    try{
                    rs = stmt.executeQuery("SELECT SUM(length*width) AS totalTSA FROM roomdetails WHERE PID="+cl.getCID());

                    while (rs.next()){
                        totalTSA = rs.getDouble("totalTSA");
                        totalTSA = totalTSA * 10.7639;  //square metre to square foot
                    } 
                    }catch(SQLException e){}

                    Double totalcost = round(totalTSA * Integer.valueOf(newValue),2);
                    txtLCost.setText(String.valueOf(totalcost));
                }
                else {
                    txtLCost.setText(String.valueOf(0.00));
                }
            }
        });
        
    }
    
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    
    
    //Gantt Chart
    public void createAndSetSwingDrawingPanel(final SwingNode swingNode) {
        final IntervalCategoryDataset dataset = createDataset();
        final JFreeChart chart = createChart(dataset);
        
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));

        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                swingNode.setContent(chartPanel);
            }
        });
    }
    
        public  IntervalCategoryDataset createDataset() {
        
        String task;
        java.util.Date startDate;
        int duration;
        
        final TaskSeries s1 = new TaskSeries("Scheduled");
        
        try{
            rs = stmt.executeQuery("SELECT * FROM task WHERE QID IN (SELECT QID FROM selected_sp WHERE  CID = "+cl.getCID()+") OR QID = "+cl.getCID()+" ORDER BY startDate ASC");

            while (rs.next()){

                //Start Date
                task = rs.getString("task_Name");
                startDate = rs.getDate("startDate");
                duration = rs.getInt("duration");

                if(duration == 1){
                    duration=2;
                }

                java.util.Date date = startDate; 
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE");
                simpleDateFormat = new SimpleDateFormat("MMMM");
                String smonth = simpleDateFormat.format(date).toUpperCase();

                Calendar cal = Calendar.getInstance(); 
                cal.setTime(startDate);
                int sday = cal.get(Calendar.DAY_OF_MONTH);
                int syear = cal.get(Calendar.YEAR);
                int sm = cal.get(Calendar.MONTH);

                //End Date
                SimpleDateFormat dt1 = new SimpleDateFormat("yyyy-MM-dd");
                String sDate = dt1.format(startDate);

                LocalDate parsedDate = LocalDate.parse(sDate);
                LocalDate addedDate = parsedDate.plusDays(duration-1);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                String eD = addedDate.format(formatter);

                int eday = Integer.parseInt(eD.substring(8,10));
                int eyear = Integer.parseInt(eD.substring(0,4));
                int em = Integer.parseInt(eD.substring(5,7));
                em = em-1;

                //System.out.println(task + "\t start: "+ sday +" " +sm+ " "+ syear + "\t End: "+ eday + " "+ (em-1) + " "+ eyear+ "\n");


            s1.add(new org.jfree.data.gantt.Task(task, new SimpleTimePeriod(date(sday,sm,syear), date(eday,em,eyear))));

            }
        }catch(SQLException e){}
           
        final TaskSeriesCollection collection = new TaskSeriesCollection();
        collection.add(s1);
        return collection;
    }
    
    private JFreeChart createChart(final IntervalCategoryDataset dataset) {
        JFreeChart chart = ChartFactory.createGanttChart(
            "",  // chart title
            "Tasks",              // domain axis label
            "Date",              // range axis label
            dataset,             // data
            false,                // include legend
            true,                // tooltips
            false                // urls
        );  
        
//       chart.getCategoryPlot().getDomainAxis().setMaxCategoryLabelWidthRatio(10.0f);
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.white);
        CategoryItemRenderer renderer = plot.getRenderer();
        renderer.setSeriesPaint(0, Color.blue);
        
        ChartPanel chartPanel = new ChartPanel(chart);
        chart = chartPanel.getChart();
        chart.setBackgroundPaint(Color.white);
        
        //ChartUtilities.saveChartAsJPEG(new File("C:\\chartA.jpg"), chart, 800, 30+activities.size()*30);
        return chart;    
    }
    
    private static java.util.Date date(final int day, final int month, final int year) {
        final Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        final java.util.Date result = calendar.getTime();
        return result;
    }
    
    //Room details
    
    private void setUpRoomDetails(){
        fill = new fillTable(tblRooms,"SELECT RID, room_name, height, length, width, openingTSA FROM roomdetails WHERE PID = "+cl.getCID());
        tblRooms.setOnMouseClicked(e -> {   
            if (tblRooms.getSelectionModel().getSelectedItem() != null) {
                String RID = tblRooms.getSelectionModel().getSelectedItem().toString(); 
                RID = RID.substring(1, RID.indexOf(","));  
                fill = new fillTable(tblOpenings,"SELECT * FROM openings WHERE RID ="+ RID ); 
            }     
        });
    }
    
    

    
    
    //pdf viewer
    
    private void setUpComboBoxPlan(){
        dataPlan.clear();
        try{
            rs = stmt.executeQuery("SELECT planName FROM pdfplan WHERE PID = "+cl.getCID());
            while (rs.next()){
                dataPlan.add(rs.getString("planName"));
            }
        }catch(SQLException e){}
        cmbChoosePlan.setPromptText("Select Plan");
        cmbChoosePlan.setItems(dataPlan);
        
        cmbChoosePlan.getSelectionModel().selectedItemProperty().addListener( (options, oldValue, newValue) -> {   
            int selectedIndex;
            selectedIndex = cmbChoosePlan.getSelectionModel().getSelectedIndex();
            readpdffromdb(dataPlan.get(selectedIndex).toString());
//            openDocument("C:\\Users\\user\\Documents\\NetBeansProjects\\MergedClientApp_1\\Plan\\"+dataPlan.get(selectedIndex).toString()+".pdf");
        });
    }
    
    private void readpdffromdb(String pdfName){
        
        try {
            String sql = ("SELECT pdf FROM pdfplan WHERE planName = ? AND PID = ?");
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, pdfName);
            statement.setInt(2, cl.getCID());
            
            rs = statement.executeQuery();
            if (rs.next()){          
                String outputfilePath = "C:\\Users\\user\\Documents\\NetBeansProjects\\MergedClientApp_1\\Plan\\"+pdfName+".pdf" ;
                
                Blob blob = rs.getBlob("pdf");
                InputStream inputStream = blob.getBinaryStream();
                
                OutputStream outputStream = new FileOutputStream(outputfilePath);
 
                int bytesRead = -1;
                byte[] buffer = new byte[BUFFER_SIZE];
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                System.out.println("Done");
                openDocument(outputfilePath);
                
                
                inputStream.close();
                outputStream.close();
                System.out.println("File saved");
            }
        
        } catch (SQLException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
    }
    
    private void createViewer() {
        try {
            SwingUtilities.invokeAndWait(() -> {
                // create the viewer ri components.
                swingController = new SwingController();
                swingController.setIsEmbeddedComponent(true);
                swingController.getDocumentViewController().setAnnotationCallback(
                new org.icepdf.ri.common.MyAnnotationCallback(swingController.getDocumentViewController()));
                
                SwingViewBuilder factory = new SwingViewBuilder(swingController);
                viewerPanel = factory.buildViewerPanel();
                swingNode.setContent(viewerPanel);
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
    
    private void openDocument(String document) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                swingController.openDocument(document);
                viewerPanel.revalidate();
            }
        });
    }
    
    
  
    
    
    @FXML
    private void changeScreen(ActionEvent event) {
         Button btn = (Button) event.getSource();
         switch(btn.getId()){
            case "btnBackToQuo" : myController.reloadScreen(ScreensFramework.SP_Side_QuotationID, ScreensFramework.SP_Side_QuotationFile);
            break;     
         }
    }
    
    public void setScreenParent(ScreensController screenParent){
        myController = screenParent;
    }
    
}
