
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;


public class expense {

    private final SimpleStringProperty expID;
    private final SimpleStringProperty expCID;
    private final SimpleStringProperty expDate;
    private final SimpleStringProperty expSName;
    private final SimpleStringProperty expType;
    private final SimpleStringProperty expDetails;
    private final SimpleStringProperty expAmount;
    
    public expense(String eID, String cid, String date, String sname, String type, String details, String amount) {
        expID = new SimpleStringProperty(eID);
        expCID = new SimpleStringProperty(cid);
        expDate = new SimpleStringProperty(date);
        expSName = new SimpleStringProperty(sname);
        expType = new SimpleStringProperty(type);
        expDetails = new SimpleStringProperty(details);
        expAmount = new SimpleStringProperty(amount);
    }

    public String getExpID() {
        return expID.get();
    }

    public String getExpCID() {
        return expCID.get();
    }

    public String getExpDate() {
        return expDate.get();
    }

    public String getExpSName() {
        return expSName.get();
    }

    public String getExpType() {
        return expType.get();
    }

    public String getExpDetails() {
        return expDetails.get();
    }
    
    public String getExpAmount() {
        return expAmount.get();
    }
    
    
    
    public void setExpID(String eID) {
        expID.set(eID);
    }
    
     public void setExpCID(String cid) {
        expCID.set(cid);
    }
    public void setExpDate(String date) {
        expDate.set(date);
    }

    public void setExpSName(String sname) {
        expSName.set(sname);
    }  
    
    public void setExpType(String type) {
        expType.set(type);
    }

    public void setExpDetails(String details) {
        expDetails.set(details);
    }
    
    public void setExpAmount(String amount) {
        expAmount.set(amount);
    }
    
    
    public String toString(){
        return new StringBuffer("ExpID: ").append(getExpID()).append("\t CID: ").append(getExpCID()).append("\t Date: ").append(getExpDate()).append("\t SName: ").append(getExpSName()).append("\t Type: ").append(getExpType()).append("\t Details: ").append(getExpDetails()).append("\t Amount: ").append(getExpAmount()).toString();
  
    }
}
