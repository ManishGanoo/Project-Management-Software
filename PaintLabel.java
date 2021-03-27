import javafx.beans.property.SimpleStringProperty;

public class PaintLabel {
    private final SimpleStringProperty paintID;
    private final SimpleStringProperty  paintLabel;
    private final SimpleStringProperty  numCoats;
    private final SimpleStringProperty spreadRate;
    private final SimpleStringProperty price;

    
    public PaintLabel(String id, String plabel, String numcoats, String spreadrate, String paintprice) {
        paintID = new SimpleStringProperty(id);
        paintLabel = new SimpleStringProperty(plabel);
        numCoats = new SimpleStringProperty(numcoats);
        spreadRate =  new SimpleStringProperty(spreadrate);
        price = new SimpleStringProperty(paintprice);

    }
    public String getPaintID() {return paintID.get();}
    public String getPaintLabel() {return paintLabel.get();}
    public String getNumCoats() {return numCoats.get();}
    public String getSpreadRate() {return spreadRate.get();}
    public String getPrice() {return price.get();}

    public void setPaintID(String id){paintID.set(id);}
    public void setPaintLabel(String plabel){paintLabel.set(plabel);}
    public void setNumCoats(String numcoats) {numCoats.set(numcoats);}    
    public void setSpreadRate(String spreadrate){spreadRate.set(spreadrate);}
    public void setPrice(String paintprice){price.set(paintprice);}  
    
    public String toString(){
        return new StringBuffer("Paint ID: ").append(getPaintID()).append("Paint Label: ").append(getPaintLabel()).append("\t Number of coats: ").append(getNumCoats()).append("\t ISpread Rate: ").append(getSpreadRate()).append("\t Price /lt:").append(getPrice()).toString();
    
    }
   
}