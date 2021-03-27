import javafx.beans.property.SimpleStringProperty;

public class Openings {
    
    private final SimpleStringProperty OPID;
    private final SimpleStringProperty ORID;
    private final SimpleStringProperty op_type;
    private final SimpleStringProperty op_height;
    private final SimpleStringProperty op_width;

    public Openings(String oID, String oRID, String oType, String oHeight, String oWidth) {
        
        OPID = new SimpleStringProperty(oID);
        ORID = new SimpleStringProperty(oRID);
        op_type = new SimpleStringProperty(oType);
        op_height = new SimpleStringProperty(oHeight);
        op_width = new SimpleStringProperty(oWidth);   
    }
    
    public String getOPID() {
        return OPID.get();
    }

    public String getORID() {
        return ORID.get();
    }

    public String getOp_type() {
        return op_type.get();
    }

    public String getOp_height() {
        return op_height.get();
    }

    public String getOp_width() {
        return op_width.get();
    }
    
    
    public void setOPID(String oID) {
       OPID.set(oID);
    }
    
    public void setORID(String oRID) {
        ORID.set(oRID);
    }
    
    public void setOp_type(String oType) {
        op_type.set(oType);
    }
    
    public void setOp_height(String oHeight) {
        op_height.set(oHeight);
    }
    
    public void setOp_width(String oWidth) {
        op_width.set(oWidth);
    }
    
    
    public String toString(){
        return new StringBuffer("OPID: ").append(getOPID()).append("\t RID: ").append(getORID()).append("\t Type: ").append(getOp_type()).append("\t Height: ").append(getOp_height()).append("\t Width: ").append(getOp_width()).toString();
  
    }
    
}
