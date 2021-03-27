import javafx.beans.property.SimpleStringProperty;

public class room {

    private final SimpleStringProperty RID;
    private final SimpleStringProperty room_name;
    private final SimpleStringProperty room_type;
    private final SimpleStringProperty room_height;
    private final SimpleStringProperty room_length;
    private final SimpleStringProperty room_width;
    private final SimpleStringProperty PID;
    
    public room (String rID, String rName, String rType, String rHeight, String rLength, String rWidth, String rPID) {
        
        RID = new SimpleStringProperty(rID);
        room_name = new SimpleStringProperty(rName);
        room_type = new SimpleStringProperty(rType);
        room_height = new SimpleStringProperty(rHeight);
        room_length = new SimpleStringProperty(rLength);
        room_width = new SimpleStringProperty(rWidth);
        PID = new SimpleStringProperty(rPID);
    }

    public String getRID() {
        return RID.get();
    }

    public String getRoom_name() {
        return room_name.get();
    }
    
    public String getRoom_type() {
        return room_type.get();
    }

    public String getRoom_height() {
        return room_height.get();
    }

    public String getRoom_length() {
        return room_length.get();
    }

    public String getRoom_width() {
        return room_width.get();
    }

    public String getPID() {
        return PID.get();
    }
    
    
    public void setRID(String rID) {
       RID.set(rID);
    }
    
    public void setRoom_name(String rName) {
        room_name.set(rName);
    }
    
    public void setRoom_type(String rType) {
        room_type.set(rType);
    }
    
    public void setRoom_height(String rHeight) {
        room_height.set(rHeight);
    }
    
    public void setRoom_length(String rLength) {
        room_length.set(rLength);
    }
    
    public void setRoom_width(String rWidth) {
        room_width.set(rWidth);
    }
    
    public void setPID(String rPID) {
        PID.set(rPID);
    }
    
    public String toString(){
        return new StringBuffer("RID: ").append(getRID()).append("\t Room Type: ").append(getRoom_type()).append("\t Room Name: ").append(getRoom_name()).append("\t Height: ").append(getRoom_height()).append("\t Length: ").append(getRoom_length()).append("\t Width: ").append(getRoom_width()).append("\t PID: ").append(getPID()).toString();
  
    }
}