import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import java.awt.Color;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javax.swing.*;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.ResourceBundle;
import javafx.scene.control.TableView;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.IntervalCategoryDataset;
import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;
import org.jfree.data.time.SimpleTimePeriod;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

public class TaskController extends dbManipulation implements Initializable , ControlledScreen{

    ScreensController myController;
    
    private final String search = "SELECT * FROM task ";
    private fillTable fill;
    
    @FXML   private TableView tblTasks;
    @FXML   private SwingNode swingNode;
    
    @FXML   private TableView tblVerification;
    @FXML   private TextField txtVDetail;
    @FXML   private DatePicker dtpVDate;
    @FXML   private Button btnAddV;
    
    ObservableList<Tasks> taskObsList = FXCollections.observableArrayList();
    
    Client cl;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cl = Context.getInstance().currentClient();
        
        createAndSetSwingDrawingPanel(swingNode);
        createTaskTable();
        setUpVerificationDates();
    }    
    
    private void setUpVerificationDates(){
        //Architect Verification dates
        btnAddV.setOnMouseClicked(e -> {
            try {
                if(Validation.checkVerificationDate(txtVDetail.getText(), dtpVDate.getValue())){
                    try {
                        //check that txtVDetail.getText() is not found in db as taskName needs unique to be displayed on gantt chart
                        rs = stmt.executeQuery("SELECT * FROM task WHERE (QID IN (SELECT QID FROM selected_sp WHERE quo_status = 'ACCEPTED' AND CID = "+cl.getCID()+") OR QID = "+cl.getCID()+") AND task_Name = '"+txtVDetail.getText()+"'");
                        if(!rs.isBeforeFirst() ){ //if rs is empty
                            Insert("INSERT INTO task(QID, task_Name, task_details, startDate, duration, task_status) VALUES ("+cl.getCID()+",'"+txtVDetail.getText()+"','Architect Verification','"+String.valueOf(dtpVDate.getValue())+"', 1 , 'Pending' )");
                            myController.reloadScreen(ScreensFramework.screen3ID, ScreensFramework.screen3File);
                            
                            Notifications notificationBuilder = Notifications.create()
                                    .title("Success")
                                    .text("Verification date added")
                                    .graphic(null)
                                    .hideAfter(Duration.seconds(5))
                                    .position(Pos.TOP_RIGHT);
                            //notificationBuilder.darkStyle();
                            notificationBuilder.showInformation();
                        }
                        else{
                            Notifications notificationBuilder = Notifications.create()
                                    .title("Already Exist")
                                    .text("Please change the label of the verification date ")
                                    .graphic(null)
                                    .hideAfter(Duration.seconds(5))
                                    .position(Pos.TOP_RIGHT);
                            //notificationBuilder.darkStyle();
                            notificationBuilder.showError();
                        }
                    } catch (SQLException ex) {
                        Logger.getLogger(TaskController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } catch (SQLException ex) {
                Logger.getLogger(TaskController.class.getName()).log(Level.SEVERE, null, ex);   
            }

        });
    }
    
        
    private void createTaskTable(){
        tblTasks.setEditable(true);
                       
        TableColumn tnameCol = new TableColumn("Task Name");
        TableColumn tdetailCol = new TableColumn("Task Details");
        TableColumn tdurationCol = new TableColumn("Duration");
        TableColumn tsdateCol = new TableColumn("Start Date");
        TableColumn tstatus = new TableColumn("Status");

                
        tnameCol.setCellValueFactory(new PropertyValueFactory<Tasks,String>("taskName"));
        tdetailCol.setCellValueFactory(new PropertyValueFactory<Tasks,String>("taskDetails"));
        tdurationCol.setCellValueFactory(new PropertyValueFactory<Tasks,String>("taskDuration"));
        tsdateCol.setCellValueFactory(new PropertyValueFactory<Tasks,String>("taskDate"));
        tstatus.setCellValueFactory(new PropertyValueFactory<Tasks,String>("taskStatus"));
        
        tstatus.setCellFactory(ComboBoxTableCell.forTableColumn(setUpComboBox()));
        tstatus.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<Tasks, String>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<Tasks, String> t) {
                    if(Validation.validateUserChangeTStatus(t.getRowValue().getTaskDetails(),t.getOldValue(), t.getNewValue())){
                        ((Tasks) t.getTableView().getItems().get(
                                t.getTablePosition().getRow())
                                ).setTaskStatus(t.getNewValue());

                        String id = ((Tasks) t.getTableView().getItems().get(
                        t.getTablePosition().getRow())
                        ).getTaskID();
                    
                    
                        String sql = "UPDATE task SET task_status = '"+t.getNewValue()+"' WHERE TID = "+id+"";
                        Update(sql);
//                        myController.reloadScreen(ScreensFramework.screen4ID, ScreensFramework.screen4File);
                    }
                    tblTasks.refresh();
                }
            }
        );
                
                
        tnameCol.setMinWidth(100);
        tdetailCol.setMinWidth(100);
        tdurationCol.setMinWidth(100);
        tsdateCol.setMinWidth(150);       
        tstatus.setMinWidth(150); 
           
        fillTask(); //fill obs list 
        
        tblTasks.setItems(taskObsList);
        tblTasks.getColumns().addAll(tnameCol,tdetailCol,tsdateCol,tdurationCol,tstatus);
    }
    
    private void fillTask(){
        taskObsList.clear();
        try{
            rs = stmt.executeQuery("SELECT * FROM task WHERE QID IN (SELECT QID FROM selected_sp WHERE  quo_status = 'ACCEPTED' AND CID = "+cl.getCID()+") OR QID = "+cl.getCID()+" ORDER BY startDate ASC");
            while(rs.next()){
                taskObsList.add(new Tasks(String.valueOf(rs.getInt("TID")),rs.getString("task_Name"), rs.getString("task_details"), String.valueOf(rs.getDate("startDate")), String.valueOf(rs.getInt("duration")), rs.getString("task_status") ));
            }
        }   catch(SQLException e){}
        
    }
      
    private  ObservableList<Object>  setUpComboBox(){
        ObservableList<Object> data = FXCollections.observableArrayList();
//        data.add("Pending");
//        data.add("Ongoing");
//        data.add("Completed");
        data.add("Verified");
                
        return data;
    }
    
    public void createAndSetSwingDrawingPanel(final SwingNode swingNode) {
        final IntervalCategoryDataset dataset = createDataset();
        final JFreeChart chart = createChart(dataset);
        
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setMaximumDrawWidth(4000);
        chartPanel.setPreferredSize(new java.awt.Dimension(1000, 1000));

        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                swingNode.setContent(chartPanel);
            }
        });

    }
    
    public  IntervalCategoryDataset createDataset() {
        
        String taskG;
        Date startDate;
        int duration;
        
        final TaskSeries s1 = new TaskSeries("Before");
        final TaskSeries s2 = new TaskSeries("During");
        final TaskSeries s3 = new TaskSeries("After");
        
        try{
            rs = stmt.executeQuery( "SELECT t.task_Name, t.startDate, t.duration,s.sp_Name\n" +
            "FROM task t, serviceprovider s, selected_sp sp\n" +
            "WHERE t.QID = sp.QID AND sp.SID = s.SID AND (sp.quo_status = 'ACCEPTED' OR (sp.quo_status = 'DECLINED' AND task_status IN ('Completed','Verified'))) AND sp.CID = "+cl.getCID()+"\n" +
            "UNION\n" +
            "SELECT t.task_Name, t.startDate, t.duration,ifnull(NULL, 'Architect') AS sp_Name\n" +
            "FROM task t, serviceprovider\n" +
            "WHERE QID = "+cl.getCID()+"\n" +
            "ORDER BY startDate ASC;");

            while (rs.next()){

                //Start Date
                taskG = rs.getString("task_Name") +", "+ rs.getString("sp_Name");
                startDate = rs.getDate("startDate");
                duration = rs.getInt("duration");

                if(duration == 1){
                    duration=2;
                }

                Date date = startDate; 
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

                LocalDate today = LocalDate.now();
                
                if(parsedDate.isBefore(today) && !addedDate.isBefore(today)){ //ongoing task sdate before today and end date after today
                    s2.add(new org.jfree.data.gantt.Task(taskG, new SimpleTimePeriod(date(sday,sm,syear), date(eday,em,eyear))));
                }
                if(addedDate.isBefore(today)){ //Before today
                    s1.add(new org.jfree.data.gantt.Task(taskG, new SimpleTimePeriod(date(sday,sm,syear), date(eday,em,eyear))));
                }
                else if(!parsedDate.isBefore(today)){ //After today
                    s3.add(new org.jfree.data.gantt.Task(taskG, new SimpleTimePeriod(date(sday,sm,syear), date(eday,em,eyear))));
                }
                else{ //today
                    s2.add(new org.jfree.data.gantt.Task(taskG, new SimpleTimePeriod(date(sday,sm,syear), date(eday,em,eyear))));
                }
            }
        }catch(SQLException e){}
           
        final TaskSeriesCollection collection = new TaskSeriesCollection();
        collection.add(s1);
        collection.add(s2);
        collection.add(s3);
        
        return collection;
    }
    
    private JFreeChart createChart(final IntervalCategoryDataset dataset) {
        JFreeChart chart = ChartFactory.createGanttChart(
            "",  // chart title
            "Tasks",              // domain axis label
            "Date",              // range axis label
            dataset,             // data
            true,                // include legend
            true,                // tooltips
            false                // urls
        );  
        
//       chart.getCategoryPlot().getDomainAxis().setMaxCategoryLabelWidthRatio(10.0f);
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.white);
        CategoryItemRenderer renderer = plot.getRenderer();
        renderer.setSeriesPaint(0, Color.green);
        renderer.setSeriesPaint(1, Color.orange);
        renderer.setSeriesPaint(2, Color.blue);
        
        
        
        ChartPanel chartPanel = new ChartPanel(chart);
        chart = chartPanel.getChart();
        chart.setBackgroundPaint(Color.white);
        
        //ChartUtilities.saveChartAsJPEG(new File("C:\\chartA.jpg"), chart, 800, 30+activities.size()*30);
        return chart;    
    }
    
    private static Date date(final int day, final int month, final int year) {
        final Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        final Date result = calendar.getTime();
        return result;
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
