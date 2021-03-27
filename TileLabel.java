import javafx.beans.property.SimpleStringProperty;

public class TileLabel {
    private final SimpleStringProperty tileID;
    private final SimpleStringProperty  tileLabel;
    private final SimpleStringProperty  length;
    private final SimpleStringProperty width;
    private final SimpleStringProperty price;
    private final SimpleStringProperty extra;
    private final SimpleStringProperty total;
    
    public TileLabel(String id, String tlabel, String tlength, String twidth, String tprice,String tot, String extrat) {
        tileID = new SimpleStringProperty(id);
        tileLabel = new SimpleStringProperty(tlabel);
        length = new SimpleStringProperty(tlength);
        width =  new SimpleStringProperty(twidth);
        price = new SimpleStringProperty(tprice);
        total = new SimpleStringProperty(tot);
        extra = new SimpleStringProperty(extrat);
    }
    public String getTileID() {return tileID.get();}
    public String getTileLabel() {return tileLabel.get();}
    public String getLength() {return length.get();}
    public String getWidth() {return width.get();}
    public String getPrice() {return price.get();}
    public String getExtra() {return extra.get();}
    public String getTotal() {return total.get();}
        
    public void setTileID(String id){tileID.set(id);}
    public void setTileLabel(String tlabel){tileLabel.set(tlabel);}
    public void setLength(String tlength) {width.set(tlength);}    
    public void setWidth(String theight){width.set(theight);}
    public void setPrice(String tprice){price.set(tprice);}  
    public void setExtra(String textra){extra.set(textra);}
    public void setTotal(String tot){extra.set(tot);}
            
    public String toString(){
        return new StringBuffer("Tile ID: ").append(getTileID()).append("Tile Label: ").append(getTileLabel()).append("\t Width: ")
                .append(getLength()).append("\t Width: ").append(getWidth())
                .append("\t Price /lt:").append(getPrice()).toString();
    }
   
}