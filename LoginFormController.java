import javafx.scene.control.TextField;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

public class LoginFormController extends dbManipulation implements Initializable, ControlledScreen {

    ScreensController myController;
    
    @FXML TextField txtUName;
    @FXML PasswordField txtPass;
    @FXML Button btnLogin, btnCreateAcc;
    
    CurrentSPDetails csp;
    Client cl;
        
    public void initialize(URL url, ResourceBundle rb) {
        cl = Context.getInstance().currentClient();
        csp = Context.getInstance().currentSPDetails();
        
        txtUName.setPromptText("Username");
        txtPass.setPromptText("Password");
        
//        txtUName.setText("tom");
//        txtPass.setText("tom");

        
        txtUName.setText("sam");
        txtPass.setText("sam");
        
//        txtUName.setText("mason");
//        txtPass.setText("mason");
        
        btnLogin.setOnMouseClicked(e ->{

            try{
                   
                rs = stmt.executeQuery("SELECT * FROM users WHERE username = '"+txtUName.getText()+"' AND passw = '"+txtPass.getText()+"'");
                if (rs.next()){
                    Notifications notificationBuilder = Notifications.create()
                    .title("")
                    .text("Welcome")
                    .graphic(null)
                    .hideAfter(Duration.seconds(5))
                    .position(Pos.TOP_RIGHT);
                                //notificationBuilder.darkStyle();
                    notificationBuilder.showInformation(); 
                    
//                    System.out.println(rs.getInt("userID"));
                    if(rs.getString("userType").equals("client")){
                        cl.setCID(rs.getInt("userID"));
                        myController.reloadScreen(ScreensFramework.projectID, ScreensFramework.projectFile);
                        //go to user profile
                    }
                    else if(rs.getString("userType").equals("service provider")){
                       //go to sp side
                       csp.setSID(rs.getInt("userID"));
                       
                        rs = stmt.executeQuery("SELECT * FROM serviceprovider WHERE SID = "+csp.getSID());
                        if (rs.next()){
                            csp.setSpType(rs.getString("sp_Type"));
                        }
                       
                               //SP_Side
                        myController.loadScreen(ScreensFramework.SP_Side_ProfileID, ScreensFramework.SP_Side_ProfileFile);
                        myController.loadScreen(ScreensFramework.SP_Side_ClientID, ScreensFramework.SP_Side_ClientFile);
                        myController.loadScreen(ScreensFramework.SP_Side_QuotationID, ScreensFramework.SP_Side_QuotationFile);
                        myController.loadScreen(ScreensFramework.SP_Side_SendQuotationID, ScreensFramework.SP_Side_SendQuotationFile);
        
                        myController.reloadScreen(ScreensFramework.SP_Side_ProfileID, ScreensFramework.SP_Side_ProfileFile);
                       
                    }
                    
                }
                else{
                    Notifications notificationBuilder = Notifications.create()
                    .title("Error")
                    .text("Username or Password is incorrect, please try again.")
                    .graphic(null)
                    .hideAfter(Duration.seconds(5))
                    .position(Pos.TOP_RIGHT);
                                //notificationBuilder.darkStyle();
                    notificationBuilder.showError(); 
                }
            }catch(SQLException ex){ex.printStackTrace();}
        });
        
        btnCreateAcc.setOnMouseClicked(e ->{
            myController.reloadScreen(ScreensFramework.createAccID,ScreensFramework.createAccFile);
        });
        
    }    
    
    public void setScreenParent(ScreensController screenParent){
        myController = screenParent;
    }
        
}
