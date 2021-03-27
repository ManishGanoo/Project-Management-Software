import java.awt.Color;
import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javax.swing.SwingUtilities;
import org.controlsfx.control.Notifications;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.IntervalCategoryDataset;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;
import org.jfree.data.time.SimpleTimePeriod;



public class HomeController extends dbManipulation implements Initializable, ControlledScreen{

    ScreensController myController;
    Client cl;
    
    @FXML Label lblTime, lblDate;
    private int minute, hour, second,day, month,year;

    @FXML TextField txtProjectName, txtProjectAddress;
    
    @FXML TableView tblNotification;
    @FXML Button btnAcceptExtension, btnRefuseExtension, btnAlterBudget, btnSummary;
    @FXML Label lblNotification;
    
    private fillTable fill;
    
    @FXML Tab tabGanttChart, tabExpenseChart, tabSPChart;
    @FXML SwingNode gswingNode;
    
    private CoxcombChart chart;
    
    @FXML    private Pane paneExpenseChart;
    @FXML    private Pane paneBarChart, Panebudget;
    
    @FXML Label lblBudgetRemaining, lblBudgetTotal;
    
    @FXML TextField txtSDate, txtPlannedEDate;
    
    Item[] items;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cl = Context.getInstance().currentClient();
        setTime();
        
        txtProjectName.setEditable(false);
        txtProjectAddress.setEditable(false);
        txtSDate.setEditable(false);
        txtPlannedEDate.setEditable(false);
        
        try{
            rs = stmt.executeQuery("SELECT * FROM project WHERE PID = "+cl.getCID());
            while (rs.next()){
                txtProjectName.setText(rs.getString("proj_Name"));
                txtProjectAddress.setText(rs.getString("address"));
                txtSDate.setText(String.valueOf(rs.getDate("temp_StartDate")));
            }
        }   catch(SQLException e1){}
        
        rs = null;
        
        String edate = "";

        try{
            rs = stmt.executeQuery("SELECT * FROM tempstartdate WHERE sp_type = 'Flooring' AND PID = "+cl.getCID());
            while (rs.next()){
                edate = String.valueOf(rs.getDate("endDate"));
            }
            if(edate.equals("null")){
                txtPlannedEDate.setText("Unavailable");
            }else{
                txtPlannedEDate.setText(edate);
            }
        }   catch(SQLException e2){}
        
        setSummary();
        setNotification();
        
        setUpGanttChart();
        items = setUpExpenseDetails();
        tabGanttChart.setOnSelectionChanged(e -> {setUpGanttChart(); });
        tabExpenseChart.setOnSelectionChanged(e -> {setUpExpenseChart(items); });
        tabSPChart.setOnSelectionChanged(e -> { setUpSPChart();});
    }    
 
    private void setSummary(){
        btnSummary.setOnMouseClicked(e -> {
            try {
                 rs = stmt.executeQuery("SELECT * FROM task WHERE QID IN (SELECT QID FROM selected_sp "
                + "WHERE  quo_status = 'ACCEPTED' AND CID = "+cl.getCID()+") OR QID = "+cl.getCID()+" ORDER BY startDate ASC");

                String summary="";
                while(rs.next()){
//                     sdate = new Date();
                    String taskname = rs.getString("task_Name");
                    java.sql.Date sdate = rs.getDate("startDate");
                    int dur = rs.getInt("duration");
                    String status = rs.getString("task_status");

                    LocalDate sDate = sdate.toLocalDate();

                    LocalDate eDate = sDate.plusDays(dur);
                    
//                    System.out.println("s "+sDate);
//                    System.out.println("d "+dur);
//                    System.out.println("e "+eDate);
                    
                    LocalDate today = LocalDate.now();

                    if(sDate.equals(today) && status.equals("Pending")){
                       summary += "-Pending task "+taskname+"'s start date is today \n";
                    }
                    if(eDate.equals(today) && status.equals("Ongoing")){
                        summary += "-Ongoing task "+taskname+"'s end date is today \n";
                    }

                }
                if(summary.equals("")){
                    summary = "-No summary available today";
                }

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Summary");
                alert.setHeaderText("");
                alert.setContentText(summary);
                alert.showAndWait();

            } catch (SQLException ex) {
                Logger.getLogger(HomeController.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }
    
    private void setNotification(){
        fill = new fillTable(tblNotification,"SELECT NID, notificationType, receiverType, senderID, receiverID, details, dateSent,sp_Name \n" +
        "FROM notification n, serviceprovider s\n" +
        "WHERE receiverType = 'client' AND senderID = s.SID  AND receiverID = "+cl.getCID()+" ORDER BY dateSent DESC ");
        
        btnAcceptExtension.setVisible(false); 
        btnRefuseExtension.setVisible(false); 
                
        tblNotification.setOnMouseClicked(e -> {
            if (tblNotification.getSelectionModel().getSelectedItem() != null) {
                String selectedNotification = tblNotification.getSelectionModel().getSelectedItem().toString(); //get SELECTED ROW
//                System.out.println(selectedNotification);
                String[] selectedNotificationArr = selectedNotification.split(",");
                String nnid = selectedNotificationArr[0];
                String type = selectedNotificationArr[1];
                String id = selectedNotificationArr[3];
                
                String nid = nnid.replaceAll("\\[","");
                String sid = id.replaceAll("\\s","");
                
                String numdaysExtension =  selectedNotificationArr[5];
                
//                System.out.println("nid "+ nid);
//                System.out.println("type "+type);
//                System.out.println("sid "+sid);
                
                if (type.equals("Extension")){
                    System.out.println("extension");
                    btnAcceptExtension.setVisible(false); 
                    btnRefuseExtension.setVisible(false);
                }else{
                    btnAcceptExtension.setVisible(true); 
                    btnRefuseExtension.setVisible(true); 
                }
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                Date dateobj = new Date();
                btnAcceptExtension.setOnMouseClicked(e1 -> {
                            
                    Insert("INSERT INTO notification(notificationType, receiverType, senderID, receiverID, details, dateSent) VALUES('ExtensionResponse','serviceprovider',"+cl.getCID()+","+sid+",'Accepted','"+df.format(dateobj)+"')");

                    Update("UPDATE task SET duration = duration +"+numdaysExtension+" WHERE QID = 'Q"+cl.getCID()+sid+"' AND task_status = 'Ongoing'");
                    Update("UPDATE task SET startDate = ADDDATE(startDate,"+numdaysExtension+") WHERE QID = 'Q"+cl.getCID()+sid+"' AND task_status = 'Pending'");
                    Update("UPDATE quotation SET Duration = Duration + "+numdaysExtension+" WHERE QID = 'Q"+cl.getCID()+sid+"'");
                    Update("UPDATE notification SET notificationType = 'Accepted Request' , receiverType = 'None' WHERE NID = "+nid);
//                    Update("UPDATE task SET duration = duration + "+numdaysExtension+" WHERE QID = 'Q"+cl.getCID()+sid+"' AND task_status = 'Ongoing'");
                    
                    myController.reloadScreen(ScreensFramework.screen1ID,ScreensFramework.screen1File);
                    
                    //-----------------------------------------send mail--------------------------------------------
                    
                    
                    String spName="";
                    String rec ="";
                    try {
                        rs = stmt.executeQuery("SELECT * FROM serviceprovider WHERE SID ="+sid+"");
                        while(rs.next()){
                            spName = rs.getString("sp_Name");
                            rec = rs.getString("email");
                        }
                    } catch (SQLException ex) {
                        Logger.getLogger(SP_Side_ClientController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    

                    if(!rec.equals("")){
                        String[] to ={rec};
                        String subject = "Extensions";
                        String body ="Dear "+spName+", \nRequest for extensions has been accepted and task has been extended by "+numdaysExtension+" days";
                        
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
                    .title("Extensions")
                    .text("Request for extensions has been accepted and task has been extended by "+numdaysExtension+" days")
                    .graphic(null)
                    .hideAfter(Duration.seconds(5))
                    .position(Pos.TOP_RIGHT);
                                //notificationBuilder.darkStyle();
                    notificationBuilder.showInformation(); 
                        
                    String rec1 ="";
                    try {
                        rs = stmt.executeQuery( "SELECT * FROM serviceprovider"
                        + " WHERE email IS NOT NULL AND SID IN(SELECT SID FROM selected_sp WHERE quo_status = 'ACCEPTED' AND CID = "+cl.getCID()+" AND SID <> "+sid+")");
                        while(rs.next()){
                            spName = rs.getString("sp_Name");
                            rec1 = rs.getString("email");
                            if(!rec1.equals("")){
                                String[] to ={rec1};
                                String subject = "Extension of other Service Provider";
                                String body ="Dear "+spName+",\nClient has extended deadline of one of his service provider.\nPlease contact him for the neccessary changes.";
                                
                                ExecutorService emailExecutor = Executors.newSingleThreadExecutor();
                                emailExecutor.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        SendMail.sendFromGMail(to, subject, body);
                                    }
                                });
                                emailExecutor.shutdown();
                            } 
                        }
                    } catch (SQLException ex) {
                        Logger.getLogger(SP_Side_ClientController.class.getName()).log(Level.SEVERE, null, ex);
                    }

                });
                
                btnRefuseExtension.setOnMouseClicked(e1 -> {
                        
                    Insert("INSERT INTO notification(notificationType, receiverType, senderID, receiverID, details, dateSent) VALUES('ExtensionResponse','serviceprovider',"+cl.getCID()+","+sid+",'Refused','"+df.format(dateobj)+"')");
                    //if refused delete request in notification
                    Update("UPDATE notification SET notificationType = 'Refused Request' , receiverType = 'None' WHERE NID = "+nid);
                    
                    myController.reloadScreen(ScreensFramework.screen1ID,ScreensFramework.screen1File);
                    
                    Notifications notificationBuilder = Notifications.create()
                        .title("Extensions")
                        .text("Request for extensions has been refused.")
                        .graphic(null)
                        .hideAfter(Duration.seconds(5))
                        .position(Pos.TOP_RIGHT);
                                    //notificationBuilder.darkStyle();
                        notificationBuilder.showError(); 
                });
            }
        });
        
        
    }
    
    
    
    
    
    
    
    
    
    
    private void setUpGanttChart(){
        createAndSetSwingDrawingPanel(gswingNode);
    }
    
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
    
    
    private Item [] setUpExpenseDetails(){
        
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
            rs = stmt.executeQuery("SELECT expType, SUM(amount) AS sum FROM expense WHERE CID = "+cl.getCID()+" GROUP BY expType");
            while (rs.next()){
                String c = rs.getString("sum");
                expA[k] = Double.parseDouble(c);
                expT[k] = rs.getString("expType");
                k++;

            }
            
        }catch(SQLException e){}
        
        // Remaining
        Double usedBudget = expA[0] + expA[1] + expA[2];
        
        expA[3] = budget - usedBudget;
        expT[3] = "Remaining";
        
        Double expMisc = 0.00;
        for(int i=0;i<3;i++){
            if(expT[i].equals("Miscellaneous")){
                expMisc += expA[i];
            }
        }
        
        setUpBudget(usedBudget, expMisc, budget);
        
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
            new Item(expA[3], expT[3], javafx.scene.paint.Color.web("#96AA3B")),
            new Item(expA[2], expT[2], javafx.scene.paint.Color.web("#29A783")),
            new Item(expA[1], expT[1], javafx.scene.paint.Color.web("#098AA9")),
            new Item(expA[0], expT[0], javafx.scene.paint.Color.web("#EF5780"))
        };
          
          return items;
          
    }

    
    
    
    private void setUpExpenseChart(Item [] items){
        chart = new CoxcombChart(items);
        chart.setTextColor(javafx.scene.paint.Color.WHITE);
        chart.setAutoTextColor(false);
        
        paneExpenseChart.getChildren().clear();
        paneExpenseChart.getChildren().addAll(chart);
    }
    
    
    private void setUpSPChart(){
        int SPcount = 0;
        try {
            rs = stmt.executeQuery("SELECT COUNT(SID) AS count FROM selected_sp WHERE quo_status='ACCEPTED' AND CID = "+cl.getCID() );
            while (rs.next()){
                SPcount= rs.getInt("count");
            }
        }catch(SQLException e){}
        System.out.println("SPcount "+SPcount);
        
        String [] SPName = new String[SPcount];

        
        Double [] SPLC = new Double[SPcount];
        Double [] SPRM = new Double[SPcount];
        Double [] SPTotal = new Double[SPcount];
        
        for(int b=0; b<SPcount; b++){
            SPLC[b] = 0.00;
            SPRM[b] = 0.00;
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
        final BarChart<Number,String> spBarChart = new BarChart<>(xAxis,yAxis);
        //bc.setTitle("Service Provider Summary");
        xAxis.setLabel("Total Cost");       
        yAxis.setLabel("Service Provider");
       
        XYChart.Series series = new XYChart.Series();
        for(int z=0;z<SPcount;z++){
           series.getData().add(new XYChart.Data(SPTotal[z], SPName[z]));
        }
         
        spBarChart.getData().addAll(series);
         
        spBarChart.setMinSize(600, 350);
        paneBarChart.getChildren().addAll(spBarChart);
    }
    
    
    
    private void setUpBudget(Double usedBudget, Double expMisc, Double budgetTotal){

        double plannedExp = 0.00;

        try {
            rs = stmt.executeQuery("SELECT sp_Name,q.QID, LabourCost, SUM(tCost) AS sum\n" +
               "FROM quotation q, rawmaterial r, selected_sp s, serviceprovider sp\n" +
               "WHERE s.QID=q.QID AND r.QID = q.QID AND sp.SID = s.SID\n" +
               "AND CID="+cl.getCID()+" AND quo_status='ACCEPTED'\n" +
               "GROUP BY QID");

            while (rs.next()){
                double lc = rs.getDouble("LabourCost");
                double rm = rs.getDouble("sum");
                plannedExp += (lc + rm);
            }
        }catch(SQLException e){}

        lblBudgetTotal.setText(String.valueOf(budgetTotal));
        lblBudgetRemaining.setText(String.valueOf(budgetTotal - (plannedExp + expMisc)));


        btnAlterBudget.setOnMouseClicked(e -> {
            TextInputDialog dialog = new TextInputDialog();
               dialog.setTitle("Alter Budget");
               dialog.setHeaderText("Note! The budget is going to be altered");
               dialog.setContentText("Please enter new budget :");

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()){
                Double newB = Double.parseDouble(result.get());

                Update("Update project SET budget = "+newB+" WHERE PID="+cl.getCID()+"");
                items = setUpExpenseDetails();
                setUpExpenseChart(items);
            }

        });


         NumberAxis xAxis = new NumberAxis();
         CategoryAxis yAxis = new CategoryAxis();
         yAxis.setCategories(FXCollections.<String>observableArrayList(Arrays.asList
            ("Budget"))); 

         StackedBarChart <Number, String> sbc = new StackedBarChart<>(xAxis, yAxis);
         XYChart.Series<Number, String> series = new XYChart.Series<>();
         series.setName("Paid");
         series.getData().add(new XYChart.Data<>(usedBudget, "Budget"));

         XYChart.Series<Number, String> series1 = new XYChart.Series<>();
         series1.setName("Planned");
         series1.getData().add(new XYChart.Data<>((plannedExp - (usedBudget+expMisc)), "Budget"));

         XYChart.Series<Number, String> series2 = new XYChart.Series<>();
         series2.setName("Remaining");
         series2.getData().add(new XYChart.Data<>((budgetTotal - (plannedExp + expMisc)), "Budget"));

         sbc.getData().addAll(series,series1,series2);
         sbc.setMaxHeight(200.0);
         sbc.setMaxWidth(700.0);
         Panebudget.getChildren().addAll(sbc);

//        System.out.println(String.valueOf(usedBudget));
//        System.out.println(String.valueOf(plannedExp));
//        System.out.println(String.valueOf(budgetTotal));
//        System.out.println(String.valueOf(budgetTotal - (plannedExp + expMisc)));

    }   

    
    
    
    
    
    
    
    
    private void setTime(){
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {            
            Calendar cal = Calendar.getInstance();
            second = cal.get(Calendar.SECOND);
            minute = cal.get(Calendar.MINUTE);
            hour = cal.get(Calendar.HOUR);
            day = cal.get(Calendar.DAY_OF_MONTH);
            month = cal.get(Calendar.MONTH)+1;
            year = cal.get(Calendar.YEAR);
            
            lblTime.setText(hour + ":" + (minute) + ":" + second);
            lblDate.setText(day+"/"+month+"/"+year);
        }),
             new KeyFrame(Duration.seconds(1))
        );
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
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
