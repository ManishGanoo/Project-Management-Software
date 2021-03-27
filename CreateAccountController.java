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
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;


public class CreateAccountController extends dbManipulation implements Initializable, ControlledScreen {

    ScreensController myController;
    
    @FXML TextField txtCName, txtCPhone, txtCEmail, txtCAddress, txtCUName; 
    @FXML PasswordField txtCPass, txtCPass1;
    @FXML Button btnCreateCAcc;
    
    @FXML TextField txtSName, txtSPhone, txtSEmail, txtSAddress, txtSUName; 
    @FXML PasswordField txtSPass, txtSPass1;
    @FXML ComboBox cmbSPType;
    @FXML Button btnCreateSAcc;
    
    ObservableList<Object> data = FXCollections.observableArrayList();
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setUpNewClient();
        setUpNewSP();
    }    
    
    private void setUpNewClient(){
        btnCreateCAcc.setOnMouseClicked(e ->{
            boolean check = Validation.validateClientTextField(txtCName.getText(), txtCPhone.getText(), txtCEmail.getText(), txtCAddress.getText(), txtCUName.getText(), txtCPass.getText(), txtCPass.getText());
            if(check == true){ //all textfield are filled and password match and username unique
                try {
                    String sql = "INSERT INTO userclient(cName, telephone, email, address) VALUES ('"+txtCName.getText()+"','"+txtCPhone.getText()+"','"+txtCEmail.getText()+"','"+txtCAddress.getText()+"' )" ;
                    
                    //run sql below, and get generated CID
                    PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                    int rows = statement.executeUpdate();
                    rs = statement.getGeneratedKeys();     
                    int userID = 0;
                    if(rs.next()){
                        ResultSetMetaData rsmd = rs.getMetaData();
                        int colCount = rsmd.getColumnCount();
                        do {
                            for (int i = 1; i <= colCount; i++) {
                                String key = rs.getString(i);
                                userID = Integer.valueOf(key);
                            }
                        }while (rs.next());
                    }                           
                    System.out.println(userID);
                    //use GENERATED CID to insert into table users
                    Insert("INSERT INTO users(username, passw, userType, userID) VALUES ('"+txtCUName.getText()+"','"+txtCPass.getText()+"', 'client', "+userID+")");                  
                    
                    myController.reloadScreen(ScreensFramework.loginID, ScreensFramework.loginFile);
                    
                    Notifications notificationBuilder = Notifications.create()
                    .title("Account Created")
                    .text("You can now login with your username and password")
                    .graphic(null)
                    .hideAfter(Duration.seconds(5))
                    .position(Pos.TOP_RIGHT);
                     notificationBuilder.showInformation(); 
                } catch (SQLException ex) {
                    Logger.getLogger(CreateAccountController.class.getName()).log(Level.SEVERE, null, ex);
                }
                
            }
            
        });
    }
        
    
    private void setUpNewSP(){
        setUpSPComboBox();
        btnCreateSAcc.setOnMouseClicked(e -> {
            boolean check = Validation.validateSPTextField(txtSName.getText(), txtSPhone.getText(), txtSEmail.getText(), txtSAddress.getText(),cmbSPType.getSelectionModel().getSelectedItem().toString(), txtSUName.getText(), txtSPass.getText(), txtSPass.getText());
            if(check == true){ //all textfield are filled and password match and username unique               
                try {  
                     String sql = "INSERT INTO serviceprovider(sp_Name, telephone, email,sp_Type, address, usesApp) VALUES ('"+txtSName.getText()+"','"+txtSPhone.getText()+"','"+txtSEmail.getText()+"','"+cmbSPType.getSelectionModel().getSelectedItem()+"','"+txtSAddress.getText()+"', 1 )" ;

                    //run sql below, and get generated CID
                    PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                    int rows = statement.executeUpdate();
                    rs = statement.getGeneratedKeys();     
                    int userID = 0;
                    if(rs.next()){
                        ResultSetMetaData rsmd = rs.getMetaData();
                        int colCount = rsmd.getColumnCount();
                        do {
                            for (int i = 1; i <= colCount; i++) {
                                String key = rs.getString(i);
                                userID = Integer.valueOf(key);
                            }
                        }while (rs.next());
                    }                           
                    System.out.println(userID);
                    //use GENERATED CID to insert into table users
                    Insert("INSERT INTO users(username, passw, userType, userID) VALUES ('"+txtSUName.getText()+"','"+txtSPass.getText()+"', 'service provider', "+userID+")");           

                    myController.reloadScreen(ScreensFramework.loginID, ScreensFramework.loginFile);
                    
                    Notifications notificationBuilder = Notifications.create()
                    .title("Account Created")
                    .text("You can now login with your username and password")
                    .graphic(null)
                    .hideAfter(Duration.seconds(5))
                    .position(Pos.TOP_RIGHT);
                     notificationBuilder.showInformation(); 
                } catch (SQLException ex) {
                    Logger.getLogger(CreateAccountController.class.getName()).log(Level.SEVERE, null, ex);
                }
                
            }
        });
    }
    
    
    
    private void setUpSPComboBox(){
        data.clear();

        data.add("Masonry");
        data.add("Roof-Formwork");
        data.add("Roof-Concrete");
        data.add("Openings");
        data.add("Electrical");
        data.add("Plumbing");
        data.add("Flooring");
        data.add("Painting");
        
        cmbSPType.setPromptText("Select Type");
        cmbSPType.setItems(data);
        
    }
    
    
    @FXML
    private void changeScreen(ActionEvent event) {
         Button btn = (Button) event.getSource();
         switch(btn.getId()){
            case "btnBackToLog" : myController.reloadScreen(ScreensFramework.loginID, ScreensFramework.loginFile);
            break;     
         }
    }
    
    
    public void setScreenParent(ScreensController screenParent){
        myController = screenParent;
    }
}
