import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.util.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.icepdf.ri.common.SwingController;
import org.icepdf.ri.common.SwingViewBuilder;



//public class SP_Side_QuotationController implements Initializable {
public class SP_Side_QuotationController extends dbManipulation implements Initializable, ControlledScreen{

    ScreensController myController;
    
    private fillTable fill;
    
    @FXML TableView tblQuoReq;
    @FXML Button btnAcceptQuo, btnRefuse;
    @FXML TextField txtTempStartDate;
    @FXML TextArea txaNotes;
    @FXML Label lblAvailability;
    
    //pdf
    @FXML ComboBox cmbChoosePlan;
    @FXML private SwingNode swingNode;
    private SwingController swingController;
    private JComponent viewerPanel;   
    private static final int BUFFER_SIZE = 4096;
    ObservableList<Object> dataPlan = FXCollections.observableArrayList();
    
    CurrentSPDetails csp;
    Client cl;
    String selectedClientIndex;
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cl = Context.getInstance().currentClient();
        csp = Context.getInstance().currentSPDetails();  
//        csp.setSID(16);

        txtTempStartDate.setEditable(false);
        
        txaNotes.setEditable(false);
                
        fill = new fillTable(tblQuoReq, "SELECT PID AS ProjectID, proj_Name AS ProjectName, cName AS Client, p.address,telephone, email, no_floors AS NumberOfFloors, temp_StartDate \n" +
        "FROM project p, userclient c\n" +
        "WHERE p.CID=c.CID AND PID IN (SELECT CID from quotationrequest WHERE req_status = 'Pending' AND SID ="+csp.getSID()+")");

        
        createViewer();
                
        tblQuoReq.setOnMouseClicked(e -> {
            lblAvailability.setText("");
            if (tblQuoReq.getSelectionModel().getSelectedItem() != null) {
                selectedClientIndex = tblQuoReq.getSelectionModel().getSelectedItem().toString(); //get SELECTED ROW
                selectedClientIndex = selectedClientIndex.substring(1, selectedClientIndex.indexOf(",")); //GET ID OF SELECTED ROW
                
                cl.setCID(Integer.valueOf(selectedClientIndex));
                //System.out.println("selectedClientIndex: "+selectedClientIndex);
                setUpComboBoxPlan();
                
                setUpQuoDetails();
            
                btnAcceptQuo.setOnMouseClicked(e1 -> {
                    //codes below moved to send quotation controller
//                    System.out.println("DELETE * FROM quotationrequest WHERE SID = "+csp.getSID()+" AND CID = "+selectedClientIndex);
//                    System.out.println("INSERT INTO selected_sp(CID,SID,QID,Comments,quo_status) VALUES ("+selectedClientIndex+","+csp.getSID()+",Q"+selectedClientIndex+csp.getSID()+",'','RECEIVED')");
//                    
                    //set Client id for send quotation screen
                    cl.setCID(Integer.valueOf(selectedClientIndex));
                    myController.reloadScreen(ScreensFramework.SP_Side_SendQuotationID, ScreensFramework.SP_Side_SendQuotationFile);
                });

                btnRefuse.setOnMouseClicked(e2 ->{
                    //System.out.println("DELETE * FROM quotationrequest WHERE SID = "+csp.getSID()+" AND CID = "+selectedClientIndex);
                    Update("UPDATE quotationrequest SET req_status = 'Refused' WHERE CID ="+selectedClientIndex+" AND SID = "+csp.getSID()+" ");
                    //or add column status, and change status to refused, in order to keep deleted records
                });
            }
        });
        
        
    }    
    
    private void setUpQuoDetails(){
        
        String notes;
        String tempDate;
        LocalDate tDate = null;
        
        Date startdate, enddate;
        LocalDate ldstart, ldend;
        
        int duration;
        
        Date day = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(day);
        LocalDate today = LocalDate.of(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1,  cal.get(Calendar.DAY_OF_MONTH));
        //System.out.println("date: "+today);
        
        boolean available = TRUE;
        
        try{
            rs = stmt.executeQuery("SELECT Notes, tempStartDate FROM quotationrequest WHERE req_status = 'Pending' AND CID="+selectedClientIndex);

            while (rs.next()){
                notes = rs.getString("Notes");
                tempDate = rs.getString("tempStartDate");
                tDate = LocalDate.parse(String.valueOf(tempDate));
                if(tDate.isBefore(today)){
                    tDate = today;
                }
                txtTempStartDate.setText(tempDate);
                txaNotes.setText(notes);  

//                   System.out.println("TempDate: "+tDate);
            } 
        }catch(SQLException e1){}
            
         
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

                ldstart = LocalDate.parse(String.valueOf(startdate)); 
                ldend = LocalDate.parse(String.valueOf(enddate));

                //if((tDate.compareTo(startdate)>=0) && (tDate.compareTo(enddate)<0)){
                if(((tDate.isAfter(ldstart)) || (tDate.isEqual(ldstart))) && (tDate.isBefore(ldend))){
                        available = FALSE;
                        lblAvailability.setText("Unavailable Date");
                }
            }
        }catch(SQLException e2){}
    }
    
    
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
            try {
                readpdffromdb(dataPlan.get(selectedIndex).toString());
//            openDocument("C:\\Users\\user\\Documents\\NetBeansProjects\\MergedClientApp_1\\Plan\\"+dataPlan.get(selectedIndex).toString()+".pdf");
            } catch (IOException ex) {
                Logger.getLogger(SP_Side_QuotationController.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }
    
    private void readpdffromdb(String pdfName) throws FileNotFoundException, IOException{
        
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
