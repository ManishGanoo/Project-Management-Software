import javafx.beans.property.SimpleStringProperty;

public class RMaterial {
    private final SimpleStringProperty  itemName;
    private final SimpleStringProperty  itemPrice;
    private final SimpleStringProperty itemQuantity;
    private final SimpleStringProperty totalCost;
    
    public RMaterial(String iname, String iprice, String iqty, String tCost) {
        itemName = new SimpleStringProperty(iname);
        itemPrice = new SimpleStringProperty(iprice);
        itemQuantity =  new SimpleStringProperty(iqty);
        totalCost = new SimpleStringProperty(tCost);
    }

    public String getItemName() {return itemName.get();}
    public String getItemPrice() {return itemPrice.get();}
    public String getItemQuantity() {return itemQuantity.get();}
    public String getTotalCost() {return totalCost.get();}

    public void setItemName(String iName){itemName.set(iName);}
    public void setItemPrice(String price) {itemPrice.set(price);}    
    public void setItemQuantity(String qty){itemQuantity.set(qty);}
    public void setTotalCost(String tcost){totalCost.set(tcost);}  
    
    public String toString(){
        return new StringBuffer("Item name: ").append(getItemName()).append("\t Item price: ").append(getItemPrice()).append("\t Item Quantity: ").append(getItemQuantity()).append("\t Total Cost:").append(getTotalCost()).toString();
    }
   
}