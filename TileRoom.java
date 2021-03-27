import javafx.beans.property.SimpleStringProperty;

public class TileRoom {
    private final SimpleStringProperty RID;
        
    private final SimpleStringProperty rname;
    private final SimpleStringProperty  tsaroom;
    private final SimpleStringProperty tilelabel;
    private final SimpleStringProperty tottile;
    private final SimpleStringProperty totprice;
    
    public TileRoom(String rid, String roomname, String troom, String tlabel, String tile, String price) {
        RID = new SimpleStringProperty(rid);
        rname = new SimpleStringProperty(roomname);
        tsaroom = new SimpleStringProperty(troom);
        tilelabel = new SimpleStringProperty(tlabel);
        tottile = new SimpleStringProperty(tile);
        totprice = new SimpleStringProperty(price);
        
    }

    public String getRID() {return RID.get();}
    public String getRname() {return rname.get();}
    public String getTsaroom() {return tsaroom.get();}
    public String getTilelabel() {return tilelabel.get();}
    public String getTottile() {return tottile.get();}
    public String getTotprice() {return totprice.get();}
  
    public void setRID(String rid) {RID.set(rid);}
    public void setRname(String roomname) {rname.set(roomname);}
    public void setTsaroom(String troom) {tsaroom.set(troom);}
    public void setTilelabel(String tlabel) {tilelabel.set(tlabel);}
    public void setTottile(String tile) {tottile.set(tile);}
    public void setTotprice(String price) {totprice.set(price);}
    
    
    public String toString(){
        return new StringBuffer("RID: ").append(getRID()).append("Rooom name: ").append(getRname()).append("\t TSA Room: ")
                .append(getTsaroom()).append("\t Tile Label:").append(getTilelabel())
                .append("\t Tot tile:").append(getTottile())
                .append("\t Tot price:").append(getTotprice())
                .toString();
    }
   
}