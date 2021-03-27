import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.controlsfx.control.Notifications;
import org.icepdf.ri.common.SwingController;
import org.icepdf.ri.common.SwingViewBuilder;


public class PlanController extends dbManipulation implements Initializable , ControlledScreen{

    ScreensController myController;
    
    @FXML Button btnSelFile, btnUpload;
    @FXML Label lblSelPlan;
    @FXML ComboBox cmbChoosePlan;
    @FXML private SwingNode swingNode;
    
    private SwingController swingController;
    private JComponent viewerPanel;
    
    private static final int BUFFER_SIZE = 4096;
    
    File file;  
    ObservableList<Object> dataPlan = FXCollections.observableArrayList();
    
    Client cl;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cl = Context.getInstance().currentClient();

        
        createViewer();
        setUpUploadPlan();
        setUpComboBoxPlan();
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
                String outputfilePath = "C:\\Users\\user\\Documents\\NetBeansProjects\\MergedClientApp_1\\Plan\\"+cl.getCID()+"\\"+pdfName+".pdf" ;
                
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
    
    private void setUpUploadPlan(){
        
        btnSelFile.setOnMouseClicked(e -> {
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter extFilterPDF = new FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.PDF");
            fileChooser.getExtensionFilters().addAll(extFilterPDF);
            
            file = fileChooser.showOpenDialog(null);
            if(file != null){ 
                try {
                    File source = new File(file.getAbsolutePath());
                    File dest = new File ("C:\\Users\\user\\Documents\\NetBeansProjects\\MergedClientApp_1\\Plan\\"+cl.getCID()+"\\"+file.getName());
                    
                    if (!file.exists()) {
                        if (file.mkdir()) {
                            System.out.println("Directory is created!");
                        } else {
                            System.out.println("Failed to create directory!");
                        }
                    }
                    
                    FileUtils.copyFile(source, dest);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                lblSelPlan.setText(FilenameUtils.getBaseName(file.getName()));
            }
        });           
//        if(file != null){     
            btnUpload.setOnMouseClicked(e -> {
                uploadFile(file);
//                System.out.println(file.getAbsolutePath());
                setUpComboBoxPlan();
            });    
//        }

           
    }
    
    private void uploadFile(File file){
        if(file != null){
            try {
                String sql = "INSERT INTO pdfplan(PID, planName, plan_type, pdf) VALUES(?,?,?,?)";

                PreparedStatement statement = connection.prepareStatement(sql);
                statement.setInt(1, cl.getCID());
                statement.setString(2, FilenameUtils.getBaseName(file.getName()));
                InputStream inputStream = new FileInputStream(new File(file.getAbsolutePath()));

                statement.setString(3, "General");
                statement.setBlob(4, inputStream);

                int row = statement.executeUpdate();
                if (row > 0) {
                    Notifications notificationBuilder = Notifications.create()
                    .title("Upload Succesful")
                    .text("Plan was uploaded successfully.")
                    .graphic(null)
                    .hideAfter(Duration.seconds(5))
                    .position(Pos.TOP_RIGHT);
                    //notificationBuilder.darkStyle();
                    notificationBuilder.showInformation(); 
                }
                else{
                    Notifications notificationBuilder = Notifications.create()
                    .title("Upload Error")
                    .text("Plan was not uploaded successfully.")
                    .graphic(null)
                    .hideAfter(Duration.seconds(5))
                    .position(Pos.TOP_RIGHT);
                    //notificationBuilder.darkStyle();
                    notificationBuilder.showError(); 
                }

            } catch (SQLException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
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
