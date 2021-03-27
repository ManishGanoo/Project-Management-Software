import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Plan {
    private final SimpleIntegerProperty planID;		
    private final SimpleStringProperty planName;	


    public Plan(SimpleIntegerProperty sp_ID, SimpleStringProperty sp_Name) {
        this.planID = sp_ID;
        this.planName = sp_Name;

    }



    public void setSp_ID(int id){this.planID.set(id);}
    public void setSp_Name(String name){this.planName.set(name);}

    
    
    public int getSp_ID() { return this.planID.get();}
    public String getSp_Name() {return this.planName.get(); }


    
}
