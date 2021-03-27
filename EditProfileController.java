import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

public class EditProfileController  extends dbManipulation implements Initializable, ControlledScreen{
    ScreensController myController;
    @FXML TextField txtCName,txtCPhone,txtCEmail, txtCAddress;
    @FXML Button btnUpdate;
    
    Client cl;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cl = Context.getInstance().currentClient();
        setSPDetails();
        
        btnUpdate.setOnMouseClicked(e -> {
            if(Validation.checkSPProfileEdit(txtCName.getText(),txtCPhone.getText(),txtCEmail.getText(), txtCAddress.getText()) == true){
                Update("UPDATE userclient SET cName = '"+txtCName.getText()+"', telephone ="+txtCPhone.getText()+", email = '"+txtCEmail.getText()+"' , address = '"+txtCAddress.getText()+"' WHERE CID ="+cl.getCID()); 
                Notifications notificationBuilder = Notifications.create()
                .title("Success")
                .text("Profile was successfully updated")
                .graphic(null)
                .hideAfter(Duration.seconds(5))
                .position(Pos.TOP_RIGHT);
                 notificationBuilder.showConfirm(); 
            }

        });
    }    
    private void setSPDetails(){
        try{
            rs = stmt.executeQuery("SELECT * FROM userclient WHERE CID = "+cl.getCID());
            while (rs.next()){
                txtCName.setText(rs.getString("cName")); 
                txtCPhone.setText(String.valueOf(rs.getInt("telephone")));  
                txtCEmail.setText(rs.getString("email"));
                txtCAddress.setText(rs.getString("address"));
            }
        }   catch(SQLException e){}
    }

    @Override
    public void setScreenParent(ScreensController screenParent) {
        myController = screenParent;    
    }
}
