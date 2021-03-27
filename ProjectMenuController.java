import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;
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
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.controlsfx.control.Notifications;


public class ProjectMenuController extends dbManipulation implements Initializable , ControlledScreen{

    ScreensController myController;
    Client cl;
    
    @FXML
    private TextField txtPName, txtPAdd,txtPBud, txtPNoFloors;
    
    @FXML
    private Button btnCreateP, btnEnterProject, btnEditProfile;
    @FXML private DatePicker dtpTempSDate;

    @FXML TableView tblExistingProject, tblViewPlan;

    private fillTable fill; 
    String selectedProjectIndex;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cl = Context.getInstance().currentClient();       
        
        createProject();  
        setUpExistingProject();
        
        btnEnterProject.setOnMouseClicked(e1 ->{
//            System.out.println("cl "+cl.getCID());
            setUpExistingProject();
        });

        btnEditProfile.setOnMouseClicked(e1 ->{
            try {
                AnchorPane root = (AnchorPane) FXMLLoader.load(getClass().getResource("EditProfile.fxml"));
                Stage secondStage = new Stage();
                secondStage.setScene(new Scene(root));
                secondStage.setTitle("Edit Profile");
                secondStage.show();
                

                secondStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                    public void handle(WindowEvent we) {
//                        myController.reloadScreen(ScreensFramework.screen4ID,ScreensFramework.screen4File);
                    }
                }); 


            } catch (IOException ex) {
                Logger.getLogger(SP_ScreenController.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    
    }    
    
    private void setUpExistingProject(){
        fill = new fillTable(tblExistingProject, "SELECT PID, proj_Name, address,budget,no_floors, temp_StartDate  FROM project WHERE CID = "+cl.getCID());

        tblExistingProject.setOnMouseClicked(e -> {
            if (tblExistingProject.getSelectionModel().getSelectedItem() != null) {
                selectedProjectIndex = tblExistingProject.getSelectionModel().getSelectedItem().toString(); //get SELECTED ROW
                selectedProjectIndex = selectedProjectIndex.substring(1, selectedProjectIndex.indexOf(",")); //GET ID OF SELECTED ROW
                
                btnEnterProject.setOnMouseClicked(e1 ->{
                    cl.setCID(Integer.valueOf(selectedProjectIndex)); //set project id for selected project
                    
                    myController.loadScreen(ScreensFramework.screen1ID, ScreensFramework.screen1File);
                    myController.loadScreen(ScreensFramework.screen2ID, ScreensFramework.screen2File);
                    myController.loadScreen(ScreensFramework.screen3ID, ScreensFramework.screen3File);
                    myController.loadScreen(ScreensFramework.screen4ID, ScreensFramework.screen4File);
                    myController.loadScreen(ScreensFramework.screen5ID, ScreensFramework.screen5File);
                    myController.loadScreen(ScreensFramework.screen6ID, ScreensFramework.screen6File);
                    myController.loadScreen(ScreensFramework.screen7ID, ScreensFramework.screen7File);
                    
                    myController.setScreen(ScreensFramework.screen1ID);
                });
            }
        });
                
    }
    
    private void createProject(){
        btnCreateP.setOnMouseClicked(e ->{            
            if(Validation.checkNewProjectDetails(txtPName.getText(),txtPAdd.getText(),txtPBud.getText(),txtPNoFloors.getText(),dtpTempSDate.getValue())){
//                String sql = "INSERT INTO project(proj_Name, address, CID, budget, no_floors, temp_StartDate) VALUES('"+txtPName.getText()+"','"+txtPAdd.getText()+"',"+cl.getCID()+","+txtPBud.getText()+","+txtPNoFloors.getText()+",'"+dtpTempSDate.getValue()+"')" ;
                String sql = "INSERT INTO project(proj_Name, address, CID, budget, no_floors, temp_StartDate, remainingBudget) VALUES('"+txtPName.getText()+"','"+txtPAdd.getText()+"',"+cl.getCID()+","+txtPBud.getText()+","+txtPNoFloors.getText()+",'"+dtpTempSDate.getValue()+"',"+txtPBud.getText()+")" ;                
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
                            }
                        }while (rs.next());
                    }       
                    cl.setCID(ID); //set Project ID in context for next input
                    //System.out.println(ID);
                } catch (SQLException ex) {
                    Logger.getLogger(CreateAccountController.class.getName()).log(Level.SEVERE, null, ex);
                }

                String [] [] sp_type = new String [][]{{"Masonry","null"},
                 {"Roof-Formwork","Masonry"},
                 {"Roof-Concrete","Masonry"},
                 {"Openings","Masonry"},
                 {"Plumbing","Openings"},
                 {"Electrical","Openings"},
                 {"Painting","Electrical"},
                 {"Flooring","Painting"}};

                for(int i =0; i<sp_type.length;i++){
                    Insert("INSERT INTO TempStartDate(PID,sp_type,predecessor,exist) VALUES ("+cl.getCID()+",'"+sp_type[i][0]+"','"+sp_type[i][1]+"',FALSE)");
                }

                Update("UPDATE TempStartDate SET startDate='"+dtpTempSDate.getValue()+"' WHERE sp_type='Masonry'");

                myController.reloadScreen(ScreensFramework.inputmenuID, ScreensFramework.inputmenuFile); 
            }
            
   
        });
        
    }
       
    @FXML
    private void changeScreen(ActionEvent event) {
        Button btn = (Button) event.getSource();
        switch(btn.getText()){
            case "<-" : myController.reloadScreen(ScreensFramework.loginID,ScreensFramework.loginFile);
            break;
        }
    }
    
    public void setScreenParent(ScreensController screenParent){
        myController = screenParent;
    }
    
}
