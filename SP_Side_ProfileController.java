import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;


public class SP_Side_ProfileController extends dbManipulation implements Initializable, ControlledScreen{

    ScreensController myController;
    
    private fillTable fill;
        
    @FXML TextField txtSPName, txtPhone, txtEmail, txtAddress, txtType;
    @FXML TableView tblReview,tblNotification;
    @FXML Button btnUpdate;
    
    CurrentSPDetails csp;
        
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        csp = Context.getInstance().currentSPDetails();
        setSPDetails();
        setReview();
        
        //notification
        txtType.setDisable(true);
        
//        fill = new fillTable(tblNotification,"SELECT NID, notificationType, receiverType, senderID, receiverID, details, dateSent,cName\n" +
//"FROM notification n, userclient c\n" +
//"WHERE receiverType = 'serviceprovider' AND senderID = c.CID AND receiverID = "+csp.getSID()+" ORDER BY dateSent DESC ");
//      
        fill = new fillTable(tblNotification,"SELECT * FROM notification WHERE receiverType = 'serviceprovider' AND receiverID = 8 ORDER BY dateSent DESC");
        
        btnUpdate.setOnMouseClicked(e -> {
            if(Validation.checkSPProfileEdit(txtSPName.getText(),txtPhone.getText(),txtEmail.getText(), txtAddress.getText()) == true){
                Update("UPDATE serviceprovider SET sp_Name = '"+txtSPName.getText()+"', telephone ="+txtPhone.getText()+", email = '"+txtEmail.getText()+"' , address = '"+txtAddress.getText()+"' WHERE SID ="+csp.getSID()); 
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
            rs = stmt.executeQuery("SELECT * FROM serviceprovider WHERE SID = "+csp.getSID());
            while (rs.next()){
                txtSPName.setText(rs.getString("sp_Name")); 
                txtPhone.setText(String.valueOf(rs.getInt("telephone")));  
                txtEmail.setText(rs.getString("email"));
                txtType.setText(rs.getString("sp_Type"));
                txtAddress.setText(rs.getString("address"));
            }
        }   catch(SQLException e){}
    }
    
    private void setReview(){
        fill = new fillTable(tblReview, "SELECT review FROM review WHERE SID = "+csp.getSID());
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
           case "Log Out" : myController.reloadScreen(ScreensFramework.loginID,ScreensFramework.loginFile);
           break;
        }
    }
    
    public void setScreenParent(ScreensController screenParent){
        myController = screenParent;
    }
}
