import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

///////////////update /////////// refer to rmaterial class ///////////////

public class ServiceProvider {
    private final SimpleIntegerProperty sp_ID;		
    private final SimpleStringProperty sp_Name;	
    private final SimpleIntegerProperty telephone;	
    private final SimpleStringProperty email;		
    private final SimpleStringProperty sp_Type;	
    private final SimpleBooleanProperty usesApp;

    public ServiceProvider(SimpleIntegerProperty sp_ID, SimpleStringProperty sp_Name, SimpleIntegerProperty telephone, SimpleStringProperty email, SimpleStringProperty sp_Type, SimpleBooleanProperty usesApp) {
        this.sp_ID = sp_ID;
        this.sp_Name = sp_Name;
        this.telephone = telephone;
        this.email = email;
        this.sp_Type = sp_Type;
        this.usesApp = usesApp;
    }



    public void setSp_ID(int id){this.sp_ID.set(id);}
    public void setSp_Name(String name){this.sp_Name.set(name);}
    public void setTelephone(int tel){this.telephone.set(tel);}
    public void setEmail(String email){this.email.set(email);}
    public void setSp_Type(String type){this.sp_Type.set(type);}
    public void setUsesApp(Boolean usesApp){this.usesApp.set(usesApp);}
    
    
    public int getSp_ID() { return this.sp_ID.get();}
    public String getSp_Name() {return this.sp_Name.get(); }
    public int getTelephone() { return this.telephone.get(); }
    public String getEmail() { return this.email.get();}
    public String getSp_Type() {return this.sp_Type.get();}
    public boolean getUsesAppCost() {return this.usesApp.get();}

    
    
    
}
