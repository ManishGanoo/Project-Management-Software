import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.event.EventHandler;
import java.net.URL;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;

import java.util.Arrays;
import javafx.scene.paint.Color;

import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class ExpenseController extends dbManipulation  implements Initializable, ControlledScreen{
    ScreensController myController;
    Client cl;
    
    ObservableList<expense> listOfExp = FXCollections.observableArrayList();
    private final String search = "SELECT expID,date,serviceProvider,expType,details,amount FROM expense";
    private fillTable fill;
     
    private CoxcombChart chart;
    
    @FXML    private TableView<expense> tblExpense;
    @FXML    private Pane paneFirstChart;
    @FXML    private Pane paneBarChart;
    @FXML    private Button btnAddExp, btnRemoveExp;
    @FXML    private DatePicker datePicker;
    @FXML    private ComboBox cmbSP;
    @FXML    private ComboBox cmbExpType;
    @FXML    private TextField txtExpDetails;
    @FXML    private TextField txtExpAmount;
    @FXML    private Label lblExpDetails;
    
    Double PlannedrmCost = 0.00;
    Double PlannedlbCost = 0.00;
    Double PaidCost = 0.00;
    Double remainAmount = 0.00;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cl = Context.getInstance().currentClient();
        
        LocalDate now = LocalDate.now();
        datePicker.setValue(now);
            
        setSPComboBox();
        setExpTypeComboBox();

        ManageNewExp();
//        try{
//            rs = stmt.executeQuery( "SELECT * FROM expense WHERE CID = "+cl.getCID());
//            while (rs.isBeforeFirst()){
                setExpenseList();
                setColumns();

                tblExpense.setEditable(true);
                tblExpense.setItems(listOfExp);
                
                CoxcombChart();
                
  //          }
//        }catch(SQLException e){}
        
        //HorizontalBarChart();
        int numSP = 0;
        try {
            rs = stmt.executeQuery("SELECT COUNT(SID) AS count FROM selected_sp WHERE quo_status='ACCEPTED' AND CID = " + cl.getCID());
            while (rs.next()) {
                numSP = rs.getInt("count");
            }

            //        if(numSP >0){
            HorizontalBarChart(numSP);
            //        }

        } catch (SQLException e) {  }

    
    }
    
    private void setExpenseList(){
        
        try{
            rs = stmt.executeQuery( "SELECT * FROM expense WHERE CID = "+cl.getCID());

            while (rs.next()){

                String expID = String.valueOf(rs.getInt("expID"));
                String cid = String.valueOf(rs.getInt("CID"));
                Date date = rs.getDate("date");
                String ssdate = String.valueOf(date);
                
                String sp = rs.getString("serviceProvider");
                String expType = rs.getString("expType");
                String details = rs.getString("details");
                String amount = String.valueOf(rs.getDouble("amount"));

                listOfExp.add(new expense(expID,cid,ssdate,sp,expType,details,amount));
            } 
        }catch(SQLException e){}
        
    }
    
    
    private void setColumns(){
        
        //expID -- Not editable
        TableColumn expIDCol = new TableColumn("Expense ID");
        expIDCol.setMinWidth(100);
        expIDCol.setCellValueFactory(
            new PropertyValueFactory<expense, String>("expID"));
        expIDCol.setCellFactory(TextFieldTableCell.forTableColumn());
        expIDCol.setOnEditCommit(
            new EventHandler<CellEditEvent<expense, String>>() {
                @Override
                public void handle(CellEditEvent<expense, String> t) {
                    ((expense) t.getTableView().getItems().get(
                            t.getTablePosition().getRow())
                            ).setExpID(t.getNewValue());
                }
            }
        );

        //expCID
        
        //expDate
        TableColumn expDateCol = new TableColumn("Date");
        expDateCol.setMinWidth(100);
        expDateCol.setCellValueFactory( new PropertyValueFactory<expense, String>("expDate"));
        expDateCol.setCellFactory(TextFieldTableCell.forTableColumn());
        expDateCol.setOnEditCommit(new EventHandler<CellEditEvent<expense, String>>() {
                @Override
                public void handle(CellEditEvent<expense, String> t) {
                    if(Validation.checkDateStringFormat(t.getNewValue())){
                        ((expense) t.getTableView().getItems().get(
                                t.getTablePosition().getRow())
                                ).setExpDate(t.getNewValue());

                        String id = ((expense) t.getTableView().getItems().get(
                            t.getTablePosition().getRow())
                            ).getExpID();

                        String sql = "UPDATE expense SET date = '"+t.getNewValue()+"' WHERE expID = "+id+"";
                        Update(sql);
                    }

                }
            }
        );
        
        //expSName
        
        ObservableList<Object> data = FXCollections.observableArrayList();
        
        try {
            rs = stmt.executeQuery("SELECT sp_Name FROM serviceprovider WHERE SID IN (SELECT SID FROM selected_sp WHERE quo_status = 'ACCEPTED') " );
            while (rs.next()){
                data.add(rs.getString("sp_Name"));
            }
        }catch(SQLException e){}
        
        TableColumn expSNameCol = new TableColumn("Service Provider");
        expSNameCol.setMinWidth(105);
        expSNameCol.setCellValueFactory(new PropertyValueFactory<expense, String>("expSName"));
        expSNameCol.setCellFactory(ComboBoxTableCell.forTableColumn(data));
        expSNameCol.setOnEditCommit(new EventHandler<CellEditEvent<expense, String>>() {
                @Override
                public void handle(CellEditEvent<expense, String> t) {
                    if(Validation.checkInteger(t.getNewValue())){
                        String id = ((expense) t.getTableView().getItems().get(
                            t.getTablePosition().getRow())
                            ).getExpID();

                        String ExpType = ((expense) t.getTableView().getItems().get(
                            t.getTablePosition().getRow())
                            ).getExpType();

                        String amt = ((expense) t.getTableView().getItems().get(
                            t.getTablePosition().getRow())
                            ).getExpAmount();

                        Double amount = Double.parseDouble(amt);

                        if(ExpType.equals("Miscellaneous")){

                            ((expense) t.getTableView().getItems().get(
                            t.getTablePosition().getRow())
                            ).setExpSName(t.getNewValue());
                            String sql = "UPDATE expense SET serviceProvider = '"+t.getNewValue()+"' WHERE expID = "+id+"";
                            Update(sql);
                        }

                        else {
                            calcPlannedCost(t.getNewValue());
                            calcPaidCost(ExpType,t.getNewValue());
                            //PaidCost = PaidCost - amount;
                            boolean exceedCost = checkIfExceedCost(amount,ExpType);

                            if(exceedCost == true){
                                Alert alert = new Alert(AlertType.ERROR);
                                alert.setTitle("ERROR");
                                alert.setHeaderText("Invalid Input");
                                alert.setContentText("Amount is higher than the remainder amount of the quotation to be paid.\n The remainder amount is "+remainAmount+".");

                                alert.showAndWait();
                            }
                            else {
                                ((expense) t.getTableView().getItems().get(
                                t.getTablePosition().getRow())
                                ).setExpSName(t.getNewValue());

                                String sql = "UPDATE expense SET serviceProvider = '"+t.getNewValue()+"' WHERE expID = "+id+"";
                                Update(sql);

                            }

                        }
                        tblExpense.refresh();
                    }
                }
             }
        );
        
        //expType
        TableColumn expTypeCol = new TableColumn("Expense Type");
        expTypeCol.setMinWidth(100);
        expTypeCol.setCellValueFactory(
            new PropertyValueFactory<expense, String>("expType"));
        expTypeCol.setCellFactory(ComboBoxTableCell.forTableColumn("Payment", "Raw Materials", "Miscellaneous"));
        expTypeCol.setOnEditCommit(
            new EventHandler<CellEditEvent<expense, String>>() {
                @Override
                public void handle(CellEditEvent<expense, String> t) {
                    if(Validation.checkExpenseType(t.getNewValue())){
                        String id = ((expense) t.getTableView().getItems().get(
                           t.getTablePosition().getRow())
                           ).getExpID();

                        String SName = ((expense) t.getTableView().getItems().get(
                           t.getTablePosition().getRow())
                           ).getExpSName();

                        String amt = ((expense) t.getTableView().getItems().get(
                           t.getTablePosition().getRow())
                           ).getExpAmount();

                           Double amount = Double.parseDouble(amt);

                        boolean ori_misc = true;
                        if(t.getNewValue().equals("Miscellaneous")){
                            ori_misc = false;
                            Update("UPDATE project SET remainingBudget = remainingBudget - "+amount+" WHERE PID="+cl.getCID()+"");  

                            ((expense) t.getTableView().getItems().get(
                             t.getTablePosition().getRow())
                             ).setExpType(t.getNewValue());

                            String sql = "UPDATE expense SET expType = '"+t.getNewValue()+"' WHERE expID = "+id+"";
                            Update(sql);
                            CoxcombChart();
                       }

                      else { //Payment or Raw Materials
                            calcPlannedCost(SName);
                            calcPaidCost(t.getNewValue(),SName);
                            //PaidCost = PaidCost - amount;
                            boolean exceedCost = checkIfExceedCost(amount,t.getNewValue());

                            if(exceedCost == true){
                                Alert alert = new Alert(AlertType.ERROR);
                                alert.setTitle("ERROR");
                                alert.setHeaderText("Invalid Input");
                                alert.setContentText("Amount is higher than the remainder amount of the quotation to be paid.\n The remainder amount is "+remainAmount+".");

                                alert.showAndWait();
                            }
                            else {
                                ((expense) t.getTableView().getItems().get(
                                t.getTablePosition().getRow())
                                ).setExpType(t.getNewValue());

                                String sql = "UPDATE expense SET expType = '"+t.getNewValue()+"' WHERE expID = "+id+"";
                                Update(sql); 

                                if(ori_misc == true){
                                   Update("UPDATE project SET remainingBudget = remainingBudget + "+amount+" WHERE PID="+cl.getCID()+"");      
                                }

                                //myController.reloadScreen(ScreensFramework.screen5ID, ScreensFramework.screen5File);
                                CoxcombChart();
                            }
                      }
                      tblExpense.refresh();
                    }
                    
                }
             }
        );
        
        //expDetails
        TableColumn expDetailsCol = new TableColumn("Expense Details");
        expDetailsCol.setMinWidth(100);
        expDetailsCol.setCellValueFactory(
            new PropertyValueFactory<expense, String>("expDetails"));
        expDetailsCol.setCellFactory(TextFieldTableCell.forTableColumn());
        expDetailsCol.setOnEditCommit(
            new EventHandler<CellEditEvent<expense, String>>() {
                @Override
                public void handle(CellEditEvent<expense, String> t) {
                    ((expense) t.getTableView().getItems().get(
                        t.getTablePosition().getRow())
                        ).setExpDetails(t.getNewValue());
                    
                    String id = ((expense) t.getTableView().getItems().get(
                        t.getTablePosition().getRow())
                        ).getExpID();
                    
                    String sql = "UPDATE expense SET details = '"+t.getNewValue()+"' WHERE expID = "+id+"";
                    Update(sql);
                }
             }
        );
        
        //expAmount
        TableColumn expAmountCol = new TableColumn("Amount");
        expAmountCol.setMinWidth(100);
        expAmountCol.setCellValueFactory(
            new PropertyValueFactory<expense, String>("expAmount"));
        expAmountCol.setCellFactory(TextFieldTableCell.forTableColumn());
        expAmountCol.setOnEditCommit(
            new EventHandler<CellEditEvent<expense, String>>() {
                @Override
                public void handle(CellEditEvent<expense, String> t) {
                    if(Validation.checkInteger(t.getNewValue())){
                        String id = ((expense) t.getTableView().getItems().get(
                            t.getTablePosition().getRow())
                            ).getExpID();

                        String ExpType = ((expense) t.getTableView().getItems().get(
                            t.getTablePosition().getRow())
                            ).getExpType();

                        String SName = ((expense) t.getTableView().getItems().get(
                            t.getTablePosition().getRow())
                            ).getExpSName();

                        Double amount = Double.parseDouble(t.getNewValue());

                        if(ExpType.equals("Miscellaneous")){
                            Update("UPDATE project SET remainingBudget = remainingBudget + "+t.getOldValue()+" - "+t.getNewValue()+" WHERE PID="+cl.getCID()+"");  
                            ((expense) t.getTableView().getItems().get(
                            t.getTablePosition().getRow())
                            ).setExpAmount(t.getNewValue());
                            CoxcombChart();

                        }

                        else { //ExpType Payment or Raw Materials
                            calcPlannedCost(SName);
                            calcPaidCost(ExpType,SName);
                            PaidCost = PaidCost - Double.parseDouble(t.getOldValue());
                            boolean exceedCost = checkIfExceedCost(amount,ExpType);

                            if(exceedCost == true){

                                Alert alert = new Alert(AlertType.ERROR);
                                alert.setTitle("ERROR");
                                alert.setHeaderText("Invalid Input");
                                alert.setContentText("The amount exceeds the remainder of the quotation to be paid.\n The remainder amount is "+remainAmount+".");

                                alert.showAndWait();
                            }
                            else {
                                ((expense) t.getTableView().getItems().get(
                                t.getTablePosition().getRow())
                                ).setExpAmount(t.getNewValue());

                                String sql = "UPDATE expense SET amount = "+t.getNewValue()+" WHERE expID = "+id+"";
                                Update(sql); 

                                //myController.reloadScreen(ScreensFramework.screen5ID, ScreensFramework.screen5File);
                                CoxcombChart();
                            }

                        }                         
                    
                    tblExpense.refresh(); 
                    }
                }
                
             }
        );
        
        tblExpense.getColumns().addAll(expIDCol,expDateCol,expSNameCol,expTypeCol,expDetailsCol,expAmountCol);
        
    }
    
    private void setSPComboBox(){
        ObservableList<Object> data = FXCollections.observableArrayList();
        
        try {
            rs = stmt.executeQuery("SELECT sp_Name FROM serviceprovider WHERE SID in (SELECT SID FROM selected_sp WHERE quo_status = 'ACCEPTED' AND CID = "+cl.getCID()+")" );
            while (rs.next()){
                data.add(rs.getString("sp_Name"));
            }
        }catch(SQLException e){}
        cmbSP.setPromptText("Select Type");
        cmbSP.setEditable(true);
        cmbSP.setItems(data);
        cmbSP.getEditor().textProperty().addListener((obs, oldText, newText) -> 
        {
            cmbSP.setValue(newText);
        });

    }
     
    private void setExpTypeComboBox(){
        cmbExpType.getItems().addAll(
            "Payment",
            "Raw Materials",
            "Miscellaneous"
        ); 
    }

    private void ManageNewExp(){
            
        btnAddExp.setOnMouseClicked(e ->{
            
            LocalDate date;
            String SPName ;
            String ExpType;


            date = datePicker.getValue(); //2018-03-09
            String sDate = String.valueOf(date);

            if(cmbSP.getSelectionModel().getSelectedItem() != null){
                SPName = cmbSP.getSelectionModel().getSelectedItem().toString();
            }else{
                SPName = "";
            }
            if(cmbExpType.getSelectionModel().getSelectedItem() != null){
                ExpType = cmbExpType.getSelectionModel().getSelectedItem().toString();
            }
            else{
                ExpType="";
            }
//            System.out.println(date+SPName+ExpType+txtExpDetails.getText()+txtExpAmount.getText());
            
            try {
                if(Validation.checkNewExpense(date,SPName,ExpType,txtExpDetails.getText(),txtExpAmount.getText())){

                    String ExpDetails = txtExpDetails.getText();
                    String ExpAmount = txtExpAmount.getText();
                    Double amount = Double.parseDouble(ExpAmount);
            
                    String expCID = String.valueOf(cl.getCID());
                    String expID = "";
                    
                    if(ExpType.equals("Miscellaneous")){
                        Update("UPDATE project SET remainingBudget = remainingBudget - "+ExpAmount+" WHERE PID="+cl.getCID()+"");  
                    }
                    //Validation if payment > planned quotation
                    calcPlannedCost(SPName);
                    calcPaidCost(ExpType,SPName);
                    //check
                    boolean exceedCost = checkIfExceedCost(amount,ExpType);
            
                    if(exceedCost == true){
                        Alert alert = new Alert(AlertType.ERROR);
                        alert.setTitle("ERROR");
                        alert.setHeaderText("Invalid Input");
                        alert.setContentText("You have entered an amount which exceeds the remainder amount of the quotation to be paid \n The remainder amount is "+remainAmount+".");

                        alert.showAndWait();
                    }
                    else {
                        String sql = "INSERT INTO expense(CID,date, serviceProvider, expType, details, amount) VALUES("+expCID+", '"+date+"','"+SPName+"','"+ExpType+"','"+ExpDetails+"',"+ExpAmount+");" ;
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
                                        expID = key;
                                    }
                                }while (rs.next());
                            }

                        } catch (SQLException ex) {
                            Logger.getLogger(CreateAccountController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    
                        listOfExp.add(new expense(expID,expCID,sDate,SPName,ExpType,ExpDetails,ExpAmount));
                        tblExpense.setItems(listOfExp);
                        datePicker.getEditor().clear();
                        cmbSP.getEditor().clear();
                        cmbExpType.getEditor().clear();
                        txtExpDetails.clear();
                        txtExpAmount.clear();

                        CoxcombChart();
                    }
                }
            } catch (SQLException ex) {
                Logger.getLogger(ExpenseController.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
            
        btnRemoveExp.setOnMouseClicked(e -> {
            int selectedIndex = tblExpense.getSelectionModel().getSelectedIndex();
            if (selectedIndex >= 0) {
                String id = tblExpense.getSelectionModel().getSelectedItem().getExpID();
                
                String expType = tblExpense.getSelectionModel().getSelectedItem().getExpType();
                Double expAmt = Double.parseDouble(tblExpense.getSelectionModel().getSelectedItem().getExpAmount());
                
                Delete("DELETE FROM expense WHERE expID = "+id+"");
//                myController.reloadScreen(ScreensFramework.screen5ID, ScreensFramework.screen5File);
                if(expType.equals("Miscellaneous")){
                    Update("UPDATE project SET remainingBudget = remainingBudget + "+expAmt+" WHERE PID="+cl.getCID()+"");  
                }

                listOfExp.remove(selectedIndex);
                
//                tblExpense.getItems().remove(selectedIndex);
                tblExpense.setItems(listOfExp);
//                System.out.println("DELETE FROM expense WHERE expID = "+id+"");
                //fill = new fillTable(tblExpense, search);
//                myController.reloadScreen(ScreensFramework.screen5ID, ScreensFramework.screen5File);
                CoxcombChart();
            }
        });
    }
    
    private void calcPlannedCost(String SPName){
        //planned cost
        try {
            rs = stmt.executeQuery("SELECT SUM(tCost) AS sum, LabourCost\n" +
            "FROM rawmaterial rm, quotation q, selected_sp sp, serviceprovider s\n" +
            "WHERE rm.QID = q.QID AND q.QID=sp.QID AND sp.SID = s.SID\n" +
            "AND sp_Name='"+SPName+"'");

            while (rs.next()){
                PlannedrmCost = rs.getDouble("sum");
                PlannedlbCost = rs.getDouble("LabourCost");
            }
        }catch(SQLException e2){}
    }
    
    private void calcPaidCost(String ExpType, String SPName){
        try {
            rs = stmt.executeQuery("SELECT IFNULL(SUM(amount),0.00) AS sum FROM expense\n" +
            "WHERE serviceProvider='"+SPName+"'\n" +
            "AND expType = '"+ExpType+"'");

            while (rs.next()){
                PaidCost = rs.getDouble("sum");
            }
        }catch(SQLException e2){}

    }
    
    private boolean checkIfExceedCost(double amount, String ExpType){
        boolean exceed = false;
        if(ExpType.compareTo("Payment")==0){
            if((amount + PaidCost)> PlannedlbCost){
                remainAmount = PlannedlbCost - PaidCost;
                exceed = true;
            }
        }
        else { // raw materials
            if((amount + PaidCost)> PlannedrmCost){
                remainAmount = PlannedrmCost - PaidCost;
                exceed = true;
            }
        }
        return exceed;
    }

    
    private void CoxcombChart(){
        
        Double budget = 0.0;
        try{
            rs = stmt.executeQuery("SELECT * FROM project WHERE PID = "+cl.getCID());
            while (rs.next()){
                budget = rs.getDouble("budget");
            }
        }catch(SQLException e){}
        
//        String [] expT = new String[4];
//        Double [] expA = new Double[4];
        
        String [] expT = {"Raw Materials","Payment","Miscellaneous","Remaining"};
        Double [] expA = {0.00,0.00,0.00,0.00};
        
        int k=0;
        try {
            rs = stmt.executeQuery("SELECT expType, SUM(amount) AS sum FROM expense GROUP BY expType");
            while (rs.next()){
                String c = rs.getString("sum");
                expA[k] = Double.parseDouble(c);
                expT[k] = rs.getString("expType");
                k++;
            }
        }catch(SQLException e){}
        
        // Remaining
        expA[3] = budget - (expA[0] + expA[1] + expA[2]);
        expT[3] = "Remaining";
        
        //sort list ascending order
        Double temp;
        String stemp;
        
        for (int i = 0; i < 4; i++) 
        {
            for (int j = i + 1; j < 4; j++) 
            {
                if (expA[i] > expA[j]) 
                {
                    temp = expA[i];
                    stemp = expT[i];
                    
                    expA[i] = expA[j];
                    expT[i] = expT[j];
                    
                    expA[j] = temp;
                    expT[j] = stemp;
                }
            }
        
        }
        
         Item[] items = {
            new Item(expA[3], expT[3], Color.web("#96AA3B")),
            new Item(expA[2], expT[2], Color.web("#29A783")),
            new Item(expA[1], expT[1], Color.web("#098AA9")),
            new Item(expA[0], expT[0], Color.web("#EF5780"))
        };
         
        chart = new CoxcombChart(items);
        chart.setTextColor(Color.WHITE);
        chart.setAutoTextColor(false);
        paneFirstChart.getChildren().clear();
        paneFirstChart.getChildren().addAll(chart);
        
        
    }
    
    private void HorizontalBarChart(int SPcount){
        
//        int SPcount = 0;
//        try {
//            rs = stmt.executeQuery("SELECT COUNT(SID) AS count FROM selected_sp WHERE quo_status='ACCEPTED' AND CID = "+cl.getCID() );
//            while (rs.next()){
//                SPcount= rs.getInt("count");
//            }
//        }catch(SQLException e){}
    
        String [] SPName = new String[SPcount];
//        int i=0;
//        try {
//            rs = stmt.executeQuery("SELECT sp_Name FROM serviceprovider WHERE SID IN (SELECT SID FROM selected_sp WHERE quo_status='ACCEPTED' AND CID = "+cl.getCID() +")");
//            while (rs.next()){
//                SPName[i] = rs.getString("sp_Name");
//                i++;
//            }
//        }catch(SQLException e){}
//        
//        Double [] SPM = new Double[SPcount];
        Double [] SPLC = new Double[SPcount];
        Double [] SPRM = new Double[SPcount];
        Double [] SPTotal = new Double[SPcount];
        
        for(int b=0; b<SPcount; b++){
            SPLC[b] = 0.00;
            SPRM[b] = 0.00;
            SPTotal[b] = 0.00;
        }

        
        int i=0;
        try {
            rs = stmt.executeQuery("SELECT sp_Name,q.QID, LabourCost, SUM(tCost) AS sum\n" +
            "FROM quotation q, rawmaterial r, selected_sp s, serviceprovider sp\n" +
            "WHERE s.QID=q.QID AND r.QID = q.QID AND sp.SID = s.SID\n" +
            "AND CID="+cl.getCID()+" AND quo_status='ACCEPTED'\n" +
            "GROUP BY QID");

            while (rs.next()){
                SPName[i] = rs.getString("sp_Name");
                SPLC[i] = rs.getDouble("LabourCost");
                SPRM[i] = rs.getDouble("sum");
                SPTotal[i] = SPLC[i] + SPRM[i];
                i++;
            }
        }catch(SQLException e){}


        final NumberAxis xAxis = new NumberAxis();
        final CategoryAxis yAxis = new CategoryAxis();
        final BarChart<Number,String> bc =  new BarChart<>(xAxis,yAxis);
        //bc.setTitle("Service Provider Summary");
        xAxis.setLabel("Total Cost");       
        yAxis.setLabel("Service Provider");
       
        XYChart.Series series = new XYChart.Series();
         
        for(int z=0;z<SPcount;z++){
            series.getData().add(new XYChart.Data(SPTotal[z], SPName[z]));
        }
         
        bc.getData().addAll(series);
        bc.setMinSize(600, 350);
        paneBarChart.getChildren().addAll(bc);
         
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    @FXML
    private void changeScreen(ActionEvent event) {
         Button btn = (Button) event.getSource();
         switch(btn.getText()){
            case "Dashboard" : myController.reloadScreen(ScreensFramework.screen1ID, ScreensFramework.screen1File);
            break;
            case "Plan" : myController.reloadScreen(ScreensFramework.screen2ID, ScreensFramework.screen2File);
            break;
            case "Task" : myController.reloadScreen(ScreensFramework.screen3ID, ScreensFramework.screen3File);
            break;
            case "Service Provider" : myController.reloadScreen(ScreensFramework.screen4ID, ScreensFramework.screen4File);
            break;
            case "Expense" : myController.reloadScreen(ScreensFramework.screen5ID, ScreensFramework.screen5File);
            break;
            case "Photos" : myController.reloadScreen(ScreensFramework.screen6ID, ScreensFramework.screen6File);
            break;
            case "Calculator" : myController.reloadScreen(ScreensFramework.screen7ID, ScreensFramework.screen7File);
            break;
            case "Log Out" : myController.reloadScreen(ScreensFramework.loginID, ScreensFramework.loginFile);
            break;
         }
    }

    public void setScreenParent(ScreensController screenParent){
        myController = screenParent;
    }
}