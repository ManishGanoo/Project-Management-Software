import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;

public class ViewReviewController extends dbManipulation implements Initializable {

    @FXML Label lblSPName;
    @FXML TableView tblReview;
    
    CurrentSPDetails csp;
    
    private fillTable fill;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        csp = Context.getInstance().currentSPDetails();

        try{
        rs = stmt.executeQuery("SELECT sp_Name FROM serviceprovider WHERE SID = "+csp.getSID());
            while (rs.next()){
                lblSPName.setText(rs.getString("sp_Name"));
            }
        }catch(SQLException e){}

        fill=new fillTable(tblReview, "SELECT review FROM review WHERE SID = "+csp.getSID());
    }    
    
}
