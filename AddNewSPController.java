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
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;


public class AddNewSPController extends dbManipulation implements Initializable, ControlledScreen{
    ScreensController myController;
    
    @FXML TextField txtSPName, txtPhone, txtEmail, txtAddress;
    @FXML TextArea txaComments;
    @FXML ComboBox cmbSPType;
    @FXML Button btnAdd;
    
    Client cl;
    CurrentSPDetails csp;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cl = Context.getInstance().currentClient();
        csp = Context.getInstance().currentSPDetails();
    
        setComboBox();

        btnAdd.setOnMouseClicked(e ->{
            if(Validation.checknewsp(txtSPName.getText(),txtPhone.getText(),txtEmail.getText(),cmbSPType.getSelectionModel().getSelectedItem().toString(),txtAddress.getText())){
                String sql = "INSERT INTO serviceprovider(sp_Name, telephone, email, sp_Type,address, usesApp) VALUES('"+txtSPName.getText()+"',"+txtPhone.getText()+", '"+txtEmail.getText()+"','"+cmbSPType.getSelectionModel().getSelectedItem().toString()+"','"+txtAddress.getText()+"',0)" ;

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
                    String qid = "Q"+cl.getCID()+String.valueOf(ID);
                    Insert("INSERT INTO selected_sp(CID, SID,QID, Comments, quo_status) VALUES("+cl.getCID()+","+ID+",'"+qid+"','"+txaComments.getText()+"','ACCEPTED')");
                    Update("UPDATE tempStartDate SET exist = 1 WHERE PID="+cl.getCID()+" AND sp_type = '"+cmbSPType.getSelectionModel().getSelectedItem()+"'");
                    
                    Notifications notificationBuilder = Notifications.create()
                    .title("Success")
                    .text("New service provider was created, close to refresh previous screen.")
                    .graphic(null)
                    .hideAfter(Duration.seconds(5))
                    .position(Pos.TOP_RIGHT);
                                //notificationBuilder.darkStyle();
                    notificationBuilder.showInformation(); 

                    clearTextBox();

                } catch (SQLException ex) {
                    Logger.getLogger(CreateAccountController.class.getName()).log(Level.SEVERE, null, ex);
                }    
            }
     
        });
        
    }    
    
    private void clearTextBox(){
        txtSPName.setText("");
        txtPhone.setText("");
        txtEmail.setText("");
        txtAddress.setText("");
        txaComments.setText("");
        cmbSPType.getEditor().clear();
    }
    
    private void setComboBox(){
        ObservableList<Object> data = FXCollections.observableArrayList();
        
        
        try{
            rs = stmt.executeQuery("SELECT DISTINCT sp_Type FROM serviceprovider" );
                while (rs.next()){
                    data.add(rs.getString("sp_Type"));
                }
        }catch(SQLException e){}
        cmbSPType.setPromptText("Select Type");
        cmbSPType.setEditable(true);
        cmbSPType.setItems(data);
        cmbSPType.getEditor().textProperty().addListener((obs, oldText, newText) -> {
            cmbSPType.setValue(newText);
        });

    }

    @Override
    public void setScreenParent(ScreensController screenParent) {
       myController = screenParent;
    }
    
}
