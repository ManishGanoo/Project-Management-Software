import javafx.beans.property.SimpleStringProperty;

public class PaintRoom {
    private final SimpleStringProperty RID;
    
    
    private final SimpleStringProperty rname;
    private final SimpleStringProperty  tsaroom;
    private final SimpleStringProperty  tsaopen;
    private final SimpleStringProperty paintlabel;
    private final SimpleStringProperty totlt;
    private final SimpleStringProperty totprice;
    
    public PaintRoom(String rid, String roomname, String troom, String topen, String plabel, String lt, String price) {
        RID = new SimpleStringProperty(rid);
        rname = new SimpleStringProperty(roomname);
        tsaroom = new SimpleStringProperty(troom);
        tsaopen =  new SimpleStringProperty(topen);
        paintlabel = new SimpleStringProperty(plabel);
        totlt = new SimpleStringProperty(lt);
        totprice = new SimpleStringProperty(price);
    }

    public String getRID() {return RID.get();}
    public String getRname() {return rname.get();}
    public String getTsaroom() {return tsaroom.get();}
    public String getTsaopen() {return tsaopen.get();}
    public String getPaintlabel() {return paintlabel.get();}
    public String getTotlt() {return totlt.get();}
    public String getTotprice() {return totprice.get();}
  
    public void setRID(String rid) {RID.set(rid);}
    public void setRname(String roomname) {rname.set(roomname);}
    public void setTsaroom(String troom) {tsaroom.set(troom);}
    public void setTsaopen(String topen) {tsaopen.set(topen);}
    public void setPaintlabel(String plabel) {paintlabel.set(plabel);}
    public void setTotlt(String lt) {totlt.set(lt);}
    public void setTotprice(String price) {totprice.set(price);}
    
    
    public String toString(){
        return new StringBuffer("RID: ").append(getRID()).append("Rooom name: ").append(getRname()).append("\t TSA Room: ")
                .append(getTsaroom()).append("\t TSA Openings: ").append(getTsaopen())
                .append("\t Paint Label:").append(getPaintlabel())
                .append("\t Tot lt:").append(getTotlt())
                .append("\t Tot price:").append(getTotprice())
                .toString();
    }
   
}