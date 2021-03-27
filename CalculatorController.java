import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;


public class CalculatorController extends dbManipulation implements Initializable, ControlledScreen{
    ScreensController myController;
    
    //Paint
    @FXML TableView tblPaintDetails, tblPaintRoom;
    @FXML TextField txtPaintLabel, txtNumCoats, txtSpreadRate, txtPaintPriceLt, txtPaintTSA, txtTotNumLt, txtPaintTotPrice;
    @FXML Button btnAddPaintLabel, btnRemovePaintLabel;
    
    ObservableList<PaintLabel> paintObsList = FXCollections.observableArrayList();
    ObservableList<PaintRoom> proomObsList = FXCollections.observableArrayList();
    
            
    //Tile
    @FXML TableView tblTileDetails, tblTileRoom;
    @FXML TextField txtTileLabel, txtTileLength, txtTileWidth, txtPriceTile, txtTileTSA, txtTotNumTile, txtTileTotPrice;
    @FXML Button btnAddTileLabel, btnRemoveTileLabel;
    
    ObservableList<TileLabel> tileObsList = FXCollections.observableArrayList();
    ObservableList<TileRoom> troomObsList = FXCollections.observableArrayList();
    
    ObservableList<Object> paintdata = FXCollections.observableArrayList();
    ObservableList<Object> tiledata = FXCollections.observableArrayList();   
    
    int numExtraTile = 0;
    
    Client cl;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cl = Context.getInstance().currentClient();
        //cl.setCID(1);
        
        paintdata = setUpPaintComboBox();
        tiledata = setUpTileComboBox();
        
        setUpPaintCalc();    
        setUpTileCalc();
        
        txtPaintTSA.setEditable(false); 
        txtTotNumLt.setEditable(false);
        txtPaintTotPrice.setEditable(false);
        txtTileTSA.setEditable(false);
        txtTotNumTile.setEditable(false);
        txtTileTotPrice.setEditable(false); 
        
    }    
    
    
    private void setUpPaintCalc() {
        setPaintLabel();
        setPaintRoomDetails();
    }

    private void setUpTileCalc(){
        setTileLabel();
        setTileRoomDetails();
    }
    
    private void setPaintLabel(){
        createTablePaintDetails();
        
        btnAddPaintLabel.setOnMouseClicked(e1 ->{
            if(Validation.checkPaintLabel(txtPaintLabel.getText(),txtNumCoats.getText(),txtSpreadRate.getText(),txtPaintPriceLt.getText())){
                Insert("INSERT INTO paintlabel(paintLabel, PID, numCoats,spreadRate,price) VALUES('"+txtPaintLabel.getText()+"',"+cl.getCID()+","+txtNumCoats.getText()+","+txtSpreadRate.getText()+","+txtPaintPriceLt.getText()+")");
                createTablePaintRoomDetails();
                fetchDbPaintToObsList();
                clearPaintTextBoxes();

                proomObsList.clear();
                tblPaintRoom.setItems(proomObsList);
                fillPRoomDetails();
            }
        });
       
             
        btnRemovePaintLabel.setOnMouseClicked(e1 -> {
            int selectedIndex = tblPaintDetails.getSelectionModel().getSelectedIndex();
            if (selectedIndex >= 0) {
                System.out.println(selectedIndex);
                //tblPaintDetails.getItems().remove(selectedIndex); //delete from table  //no need
                Delete("DELETE FROM paintlabel WHERE paintID ="+paintObsList.get(selectedIndex).getPaintID());
                fetchDbPaintToObsList();
                createTablePaintRoomDetails();

            } else {
                Notifications notificationBuilder = Notifications.create()
                    .title("No Task Selected")
                    .text("Please select desired task to be remove.")
                    .graphic(null)
                    .hideAfter(Duration.seconds(5))
                    .position(Pos.TOP_RIGHT);
                                //notificationBuilder.darkStyle();
                    notificationBuilder.showError(); 
            }
            updatePaintLabelTable();
        });
            
    }
    
    private void createTablePaintDetails(){
        tblPaintDetails.setEditable(true);
        
        TableColumn paintLabelCol = new TableColumn("Paint Label");
        TableColumn numCoatsCol = new TableColumn("Number of Coats");
        TableColumn spreadRateCol = new TableColumn("Spreading Rate");
        TableColumn priceCol = new TableColumn("Price /lt");
        TableColumn extraCol = new TableColumn("Extra");
                
        paintLabelCol.setCellValueFactory(new PropertyValueFactory<PaintLabel,String>("paintLabel"));
        numCoatsCol.setCellValueFactory(new PropertyValueFactory<PaintLabel,String>("numCoats"));
        spreadRateCol.setCellValueFactory(new PropertyValueFactory<PaintLabel,String>("spreadRate"));
        priceCol.setCellValueFactory(new PropertyValueFactory<PaintLabel,String>("price"));
        extraCol.setCellValueFactory(new PropertyValueFactory<PaintLabel,String>("extra"));
             
        paintLabelCol.setCellFactory(TextFieldTableCell.forTableColumn());
        paintLabelCol.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<PaintLabel, String>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<PaintLabel, String> t) {
                    if(Validation.checkEmptyString(t.getNewValue())){
                        ((PaintLabel) t.getTableView().getItems().get(
                                t.getTablePosition().getRow())
                                ).setPaintLabel(t.getNewValue());
                        String id = ((PaintLabel) t.getTableView().getItems().get(
                        t.getTablePosition().getRow())
                        ).getPaintID();
                    
                        String sql = "UPDATE paintlabel SET paintLabel = '"+t.getNewValue()+"' WHERE paintID = "+id+"";
                        Update(sql);
                        fillPRoomDetails();
                    }
                    tblPaintDetails.refresh();
                }
            }
        );
        
        numCoatsCol.setCellFactory(TextFieldTableCell.forTableColumn());
        numCoatsCol.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<PaintLabel, String>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<PaintLabel, String> t) {
                    if(Validation.checkInteger(t.getNewValue())){
                        ((PaintLabel) t.getTableView().getItems().get(
                                t.getTablePosition().getRow())
                                ).setNumCoats(t.getNewValue());
                        String id = ((PaintLabel) t.getTableView().getItems().get(
                        t.getTablePosition().getRow())
                        ).getPaintID();
                    
                    
                        String sql = "UPDATE paintlabel SET numCoats = '"+t.getNewValue()+"' WHERE paintID = "+id+"";
                        Update(sql);
                        fillPRoomDetails();
                    }
                    tblPaintDetails.refresh();
                }
            }
        );
        
        spreadRateCol.setCellFactory(TextFieldTableCell.forTableColumn());
        spreadRateCol.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<PaintLabel, String>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<PaintLabel, String> t) {
                    if(Validation.checkInteger(t.getNewValue())){ 
                        ((PaintLabel) t.getTableView().getItems().get(
                                t.getTablePosition().getRow())
                                ).setSpreadRate(t.getNewValue());
                        String id = ((PaintLabel) t.getTableView().getItems().get(
                        t.getTablePosition().getRow())
                        ).getPaintID();
                    
                    
                        String sql = "UPDATE paintlabel SET spreadRate = '"+t.getNewValue()+"' WHERE paintID = "+id+"";
                        Update(sql);
                        fillPRoomDetails();
                    }
                    tblPaintDetails.refresh();
                }
            }
        );
        
        priceCol.setCellFactory(TextFieldTableCell.forTableColumn());
        priceCol.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<PaintLabel, String>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<PaintLabel, String> t) {
                    if(Validation.checkInteger(t.getNewValue())){
                        ((PaintLabel) t.getTableView().getItems().get(
                                t.getTablePosition().getRow())
                                ).setPrice(t.getNewValue());

                        String id = ((PaintLabel) t.getTableView().getItems().get(
                        t.getTablePosition().getRow())
                        ).getPaintID();
                    
                        String sql = "UPDATE paintlabel SET price = '"+t.getNewValue()+"' WHERE paintID = "+id+"";
                        Update(sql);
                        fillPRoomDetails();
                    }
                    
                }
            }
        );
        
        paintLabelCol.setMinWidth(135);
        numCoatsCol.setMinWidth(135);
        spreadRateCol.setMinWidth(135);
        priceCol.setMinWidth(134);
        fetchDbPaintToObsList();
        tblPaintDetails.setItems(paintObsList);
        
        tblPaintDetails.getColumns().addAll(paintLabelCol,numCoatsCol,spreadRateCol,priceCol);
    }

    private void fetchDbPaintToObsList(){
        try{
            rs = stmt.executeQuery("SELECT * FROM paintLabel WHERE PID = "+cl.getCID());
            paintObsList.clear(); //else when clicking on different sp, it will append rm details to the vector
            while(rs.next()){
                paintObsList.add(new PaintLabel(String.valueOf(rs.getInt("paintID")),rs.getString("paintLabel"),String.valueOf(rs.getInt("numCoats")), String.valueOf(rs.getInt("spreadRate")), String.valueOf(rs.getInt("price"))));      
            }
            updatePaintLabelTable();
        }   catch(SQLException e){}
    }
    
    private void updatePaintLabelTable(){
        tblPaintDetails.setItems(paintObsList);
    }
    
    private void clearPaintTextBoxes() {
        txtPaintLabel.setText("");
        txtNumCoats.setText("");
        txtSpreadRate.setText("");
        txtPaintPriceLt.setText("");
    }
    
       
    
    private void setPaintRoomDetails(){
        createTablePaintRoomDetails();
    }
    
    private void createTablePaintRoomDetails(){
        tblPaintRoom.getItems().clear();
        tblPaintRoom.getColumns().clear();
        tblPaintRoom.setEditable(true);
        proomObsList.clear();
        
        paintdata.clear();
        setUpPaintComboBox();
        
        TableColumn rnameCol = new TableColumn("Room Name");
        TableColumn tsaroomCol = new TableColumn("TSA Room");
        TableColumn tsaopenCol = new TableColumn("TSA Openings");
        TableColumn paintlabelCol = new TableColumn("Paint Label");
        TableColumn totltCol = new TableColumn("Total Litres");
        TableColumn totpriceCol = new TableColumn("Total Price");
                
        rnameCol.setCellValueFactory(new PropertyValueFactory<PaintRoom,String>("rname"));
        tsaroomCol.setCellValueFactory(new PropertyValueFactory<PaintRoom,String>("tsaroom"));
        tsaopenCol.setCellValueFactory(new PropertyValueFactory<PaintRoom,String>("tsaopen"));
        paintlabelCol.setCellValueFactory(new PropertyValueFactory<PaintRoom,String>("paintlabel"));
        totltCol.setCellValueFactory(new PropertyValueFactory<PaintRoom,String>("totlt"));
        totpriceCol.setCellValueFactory(new PropertyValueFactory<PaintRoom,String>("totprice"));
        
        paintlabelCol.setCellFactory(ComboBoxTableCell.forTableColumn(paintdata));
        paintlabelCol.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<PaintRoom, String>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<PaintRoom, String> t) {
                ((PaintRoom) t.getTableView().getItems().get(
                        t.getTablePosition().getRow())
                        ).setPaintlabel(t.getNewValue());
                
                String id = ((PaintRoom) t.getTableView().getItems().get(
                t.getTablePosition().getRow())
                ).getRID();
                
                try{
                    rs = stmt.executeQuery("SELECT  paintID from paintlabel WHERE paintLabel = '"+t.getNewValue()+"'");
                    while(rs.next()){
                        String sql = "UPDATE roomdetails SET paintID = "+rs.getInt("paintID")+" WHERE RID = "+id+"";
                        Update(sql);
                        fillPRoomDetails();  
                    }
                }catch(SQLException e){}
            }
        });
        
        
        rnameCol.setMinWidth(130);
        tsaroomCol.setMinWidth(130);
        tsaopenCol.setMinWidth(130);
        paintlabelCol.setMinWidth(130);
        totltCol.setMinWidth(130);
        totpriceCol.setMinWidth(130);
                
        fillPRoomDetails();
        tblPaintRoom.setItems(proomObsList);
        
        tblPaintRoom.getColumns().addAll(rnameCol,tsaroomCol,tsaopenCol,paintlabelCol,totltCol,totpriceCol);
    }
    
    private void fillPRoomDetails(){
        double paintTSA = 0.0;
        double numLt = 0.0;
        int ptotprice = 0;

        try{
            proomObsList.clear(); //else when clicking on different sp, it will append rm details to the vector
            rs = stmt.executeQuery("SELECT r.RID,  r.PID,r.room_name, r.height, r.length, r.width, r.openingTSA, r.paintID, p.paintLabel, p.numCoats, p.spreadRate, p.price\n" +
            "FROM roomdetails r LEFT JOIN paintlabel p  \n" +
            "ON  r.paintID = p.paintID\n" +
            "WHERE r.PID = "+cl.getCID()+"\n" +
            "GROUP BY r.RID");
            
            while(rs.next()){
                Double tsaopen = 0.0;
                
                int rid = rs.getInt("RID");
                String rname = rs.getString("room_name");
                Double rheight = rs.getDouble("height");
                Double rlength =  rs.getDouble("length");
                Double rwidth = rs.getDouble("width");
                String paintid = String.valueOf(rs.getInt("paintID"));
                int pid = rs.getInt("PID");
                
                String paintLabel = rs.getString("paintLabel");
                int numCoats = rs.getInt("numCoats");
                int spread = rs.getInt("spreadRate");
                int price = rs.getInt("price");

                Double tsaroom = rlength*rwidth + (2*rlength*rheight) + (2*rwidth*rheight);        
                
                tsaopen = rs.getDouble("openingTSA");
    
                if(paintLabel != null){      
//                    int lt = Math.ceil((int)(tsaroom - tsaopen)/spread * numCoats);
                    double dlt = (tsaroom - tsaopen)/spread * numCoats ;
                    int lt = (int)(dlt)+1;
                    
                    numLt += lt; //summary
                    ptotprice += lt * price;
                    proomObsList.add(new PaintRoom(String.valueOf(rid), rname, String.valueOf(tsaroom), String.valueOf(tsaopen),paintLabel,String.valueOf(lt),String.valueOf(lt*price)));      
                }
                else{
                    proomObsList.add(new PaintRoom(String.valueOf(rid), rname, String.valueOf(tsaroom), String.valueOf(tsaopen),"","",""));      
                }
                paintTSA += (tsaroom - tsaopen);

            }
            
            txtPaintTSA.setText(String.valueOf(paintTSA));
            txtTotNumLt.setText(String.valueOf(numLt));
            txtPaintTotPrice.setText(String.valueOf(ptotprice));
            
            tblPaintRoom.setItems(proomObsList);
          
        }   catch(SQLException e){}
    }
        
    private ObservableList<Object> setUpPaintComboBox(){
        try{
        rs = stmt.executeQuery("SELECT DISTINCT paintLabel FROM paintlabel WHERE PID = "+cl.getCID());
            while (rs.next()){
                paintdata.add(rs.getString("paintLabel"));
            }
        }catch(SQLException e){}
        return paintdata;
    }
    
    
    
    
    //////tile room 
    private void setTileLabel(){
        createTableTileDetails();

        btnAddTileLabel.setOnMouseClicked(e1 ->{
            if(Validation.checkPaintLabel(txtTileLabel.getText(),txtTileLength.getText(),txtTileWidth.getText(),txtPriceTile.getText())){
                Insert("INSERT INTO tilelabel(tileLabel,PID,length,width,price) VALUES('"+txtTileLabel.getText()+"',"+cl.getCID()+","+txtTileLength.getText()+","+txtTileWidth.getText()+","+txtPriceTile.getText()+")");
                createTableTileRoomDetails();
                fetchDbTileToObsList();
                clearTileTextBoxes();

                troomObsList.clear();
                tblTileRoom.setItems(troomObsList);
                fillTRoomDetails();
            }
        });
       
             
        btnRemoveTileLabel.setOnMouseClicked(e1 -> {
            int selectedIndex = tblTileDetails.getSelectionModel().getSelectedIndex();
            if (selectedIndex >= 0) {
                System.out.println(selectedIndex);
                //tblPaintDetails.getItems().remove(selectedIndex); //delete from table  //no need
                Delete("DELETE FROM tilelabel WHERE tileID ="+tileObsList.get(selectedIndex).getTileID());
                fetchDbTileToObsList();
                createTableTileRoomDetails();

            } else {
                Notifications notificationBuilder = Notifications.create()
                    .title("No Tile Label Selected")
                    .text("Please select desired tile label to be remove.")
                    .graphic(null)
                    .hideAfter(Duration.seconds(5))
                    .position(Pos.TOP_RIGHT);
                                //notificationBuilder.darkStyle();
                    notificationBuilder.showError(); 
            }
            updateTileLabelTable();
        });
    }
    
    private void createTableTileDetails(){
        tblTileDetails.setEditable(true);
        
        TableColumn tileLabelCol = new TableColumn("Tile Label");
        TableColumn lengthCol = new TableColumn("Length");
        TableColumn widthCol = new TableColumn("Width");
        TableColumn priceCol = new TableColumn("Price /Unit");
        TableColumn extraCol = new TableColumn("Extra");      
        TableColumn totCol = new TableColumn("Total");  
                
        tileLabelCol.setCellValueFactory(new PropertyValueFactory<TileLabel,String>("tileLabel"));
        lengthCol.setCellValueFactory(new PropertyValueFactory<TileLabel,String>("length"));
        widthCol.setCellValueFactory(new PropertyValueFactory<TileLabel,String>("width"));
        priceCol.setCellValueFactory(new PropertyValueFactory<TileLabel,String>("price"));
        extraCol.setCellValueFactory(new PropertyValueFactory<TileLabel,String>("extra"));
        totCol.setCellValueFactory(new PropertyValueFactory<TileLabel,String>("total"));
           
        tileLabelCol.setCellFactory(TextFieldTableCell.forTableColumn());
        tileLabelCol.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<TileLabel, String>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<TileLabel, String> t) {
                    if(Validation.checkEmptyString(t.getNewValue())){
                        ((TileLabel) t.getTableView().getItems().get(
                                t.getTablePosition().getRow())
                                ).setTileLabel(t.getNewValue());
                        String id = ((TileLabel) t.getTableView().getItems().get(
                        t.getTablePosition().getRow())
                        ).getTileID();
                    
                    
                        String sql = "UPDATE tilelabel SET tileLabel = '"+t.getNewValue()+"' WHERE tileID = "+id+"";
                        Update(sql);
                        fillTRoomDetails();
                    }
                    tblTileDetails.refresh();
                }
            }
        );
        
        lengthCol.setCellFactory(TextFieldTableCell.forTableColumn());
        lengthCol.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<TileLabel, String>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<TileLabel, String> t) {
                    if(Validation.checkInteger(t.getNewValue())){
                        ((TileLabel) t.getTableView().getItems().get(
                                t.getTablePosition().getRow())
                                ).setLength(t.getNewValue());
                        String id = ((TileLabel) t.getTableView().getItems().get(
                        t.getTablePosition().getRow())
                        ).getTileID();
                    
                    
                        String sql = "UPDATE tilelabel SET length = '"+t.getNewValue()+"' WHERE tileID = "+id+"";
                        Update(sql);
                        fillTRoomDetails();
                    }
                    tblTileDetails.refresh();
                }
            }
        );
        
        widthCol.setCellFactory(TextFieldTableCell.forTableColumn());
        widthCol.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<TileLabel, String>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<TileLabel, String> t) {
                    if(Validation.checkInteger(t.getNewValue())){
                        ((TileLabel) t.getTableView().getItems().get(
                                t.getTablePosition().getRow())
                                ).setWidth(t.getNewValue());
                        String id = ((TileLabel) t.getTableView().getItems().get(
                        t.getTablePosition().getRow())
                        ).getTileID();
                    
                    
                        String sql = "UPDATE tilelabel SET width = '"+t.getNewValue()+"' WHERE tileID = "+id+"";
                        Update(sql);
                        fillTRoomDetails();
                    }
                    tblTileDetails.refresh();
                }
            }
        );
        
        priceCol.setCellFactory(TextFieldTableCell.forTableColumn());
        priceCol.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<TileLabel, String>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<TileLabel, String> t) {
                    if(Validation.checkInteger(t.getNewValue())){
                        ((TileLabel) t.getTableView().getItems().get(
                                t.getTablePosition().getRow())
                                ).setPrice(t.getNewValue());

                        String id = ((TileLabel) t.getTableView().getItems().get(
                        t.getTablePosition().getRow())
                        ).getTileID();
                    
                    
                        String sql = "UPDATE tilelabel SET price = '"+t.getNewValue()+"' WHERE tileID = "+id+"";
                        Update(sql);
                        fillTRoomDetails();
                    }
                    tblTileDetails.refresh();
                }
            }
        );
        
        extraCol.setCellFactory(TextFieldTableCell.forTableColumn());
        extraCol.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<TileLabel, String>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<TileLabel, String> t) {
                    if(Validation.checkInteger(t.getNewValue())){
                        ((TileLabel) t.getTableView().getItems().get(
                                t.getTablePosition().getRow())
                                ).setExtra(t.getNewValue());

                        String id = ((TileLabel) t.getTableView().getItems().get(
                        t.getTablePosition().getRow())
                        ).getTileID();
                    
                    
                        String sql = "UPDATE tilelabel SET extra = '"+t.getNewValue()+"' WHERE tileID = "+id+"";
                        Update(sql);
                        fillTRoomDetails();
                    }
                    tblTileDetails.refresh();
                }
            }
        );
        
        tileLabelCol.setMinWidth(135);
        lengthCol.setMinWidth(135);
        widthCol.setMinWidth(135);
        priceCol.setMinWidth(134);
        extraCol.setMinWidth(130);
        totCol.setMinWidth(130);
        
        fetchDbTileToObsList();
        tblTileDetails.setItems(tileObsList);
        
        tblTileDetails.getColumns().addAll(tileLabelCol,lengthCol,widthCol,priceCol, totCol, extraCol);
    }
    
    private void fetchDbTileToObsList(){
        try{
            rs = stmt.executeQuery("SELECT * FROM tileLabel WHERE PID = "+cl.getCID());
            tileObsList.clear(); //else when clicking on different sp, it will append rm details to the vector

            while(rs.next()){
                tileObsList.add(new TileLabel(String.valueOf(rs.getInt("tileID")),rs.getString("tileLabel"),String.valueOf(rs.getInt("length")), String.valueOf(rs.getInt("width")), String.valueOf(rs.getInt("price")), String.valueOf(rs.getInt("total")), String.valueOf(rs.getInt("extra"))));      
            }
            
            updateTileLabelTable();
        }   catch(SQLException e){}
    }
               
    private void updateTileLabelTable(){
        tblTileDetails.setItems(tileObsList);
    }
    
    private void clearTileTextBoxes(){
        txtTileLabel.setText("");
        txtTileLength.setText("");
        txtTileWidth.setText("");
        txtPriceTile.setText("");
    }
    
    private void setTileRoomDetails(){
        createTableTileRoomDetails();
    }
    
    private void createTableTileRoomDetails(){
        tblTileRoom.getItems().clear();
        tblTileRoom.getColumns().clear();
        tblTileRoom.setEditable(true);
        troomObsList.clear();
        
        tiledata.clear();
        setUpTileComboBox();
        
        TableColumn rnameCol = new TableColumn("Room Name");
        TableColumn tsaroomCol = new TableColumn("TSA Room");
        TableColumn tilelabelCol = new TableColumn("Tile Label");
        TableColumn tottiles = new TableColumn("Number of Tile");
        TableColumn totpriceCol = new TableColumn("Total Price");
                
        rnameCol.setCellValueFactory(new PropertyValueFactory<TileRoom,String>("rname"));
        tsaroomCol.setCellValueFactory(new PropertyValueFactory<TileRoom,String>("tsaroom"));
        tilelabelCol.setCellValueFactory(new PropertyValueFactory<TileRoom,String>("tilelabel"));
        tottiles.setCellValueFactory(new PropertyValueFactory<TileRoom,String>("tottile"));
        totpriceCol.setCellValueFactory(new PropertyValueFactory<TileRoom,String>("totprice"));
        
        tilelabelCol.setCellFactory(ComboBoxTableCell.forTableColumn(tiledata));
        tilelabelCol.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<TileRoom, String>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<TileRoom, String> t) {
                ((TileRoom) t.getTableView().getItems().get(
                        t.getTablePosition().getRow())
                        ).setTilelabel(t.getNewValue());
                
                String id = ((TileRoom) t.getTableView().getItems().get(
                t.getTablePosition().getRow())
                ).getRID();
                
                try{
                    rs = stmt.executeQuery("SELECT  tileID from tilelabel WHERE tileLabel = '"+t.getNewValue()+"'");
                    while(rs.next()){
                        String sql = "UPDATE roomdetails SET tileID = "+rs.getInt("tileID")+" WHERE RID = "+id+"";
                        Update(sql);
                        fillTRoomDetails();  
                    }
                }catch(SQLException e){}
            }
        });
        
        
        rnameCol.setMinWidth(130);
        tsaroomCol.setMinWidth(130);
        tilelabelCol.setMinWidth(130);
        tottiles.setMinWidth(130);
        totpriceCol.setMinWidth(130);
        
                
        fillTRoomDetails();
        tblTileRoom.setItems(troomObsList);
        
        tblTileRoom.getColumns().addAll(rnameCol,tsaroomCol,tilelabelCol,tottiles,totpriceCol);
    }
    
    private void fillTRoomDetails(){
        double tileTSA = 0.0;

        ArrayList<String> arr = new ArrayList<String>();
        arr.clear();
            
        try{
            troomObsList.clear(); //else when clicking on different sp, it will append rm details to the vector
            
            rs = stmt.executeQuery("SELECT r.RID,  r.PID,r.room_name, r.height, r.length, r.width, r.tileID, t.tileLabel, t.length AS tlength, t.width AS twidth, t.price\n" +
            "FROM roomdetails r LEFT JOIN tilelabel t  \n" +
            "ON  r.tileID = t.tileID\n" +
            "WHERE r.PID = "+cl.getCID()+"\n" +
            "GROUP BY r.RID");
            

            
            while(rs.next()){
                int rid = rs.getInt("RID");
                int pid = rs.getInt("PID");
                
                String rname = rs.getString("room_name");
                Double rheight = rs.getDouble("height");
                Double rlength =  rs.getDouble("length");
                Double rwidth = rs.getDouble("width");
                String tileid = String.valueOf(rs.getInt("tileID"));
                
                String tileLabel = rs.getString("tileLabel");
                int tlength = rs.getInt("tlength");
                int twidth = rs.getInt("twidth");
                int price = rs.getInt("price");

                Double tsaroom = rlength*rwidth;  
                
                if(tileLabel != null){ 
                    double tileDimensions =0.0;
                    tileDimensions = (double)(tlength * twidth) / 10000;          
                    double dtotTile = tsaroom /tileDimensions; //cm2 to m2 divide by 10,000
                    
                    int totTile = (int) dtotTile+1;
                    
                    arr.add(String.valueOf(totTile));
                    arr.add(tileid);
                                        
                    troomObsList.add(new TileRoom(String.valueOf(rid), rname, String.valueOf(tsaroom),tileLabel,String.valueOf(totTile),String.valueOf(totTile*price)));      
                }
                else{
                    troomObsList.add(new TileRoom(String.valueOf(rid), rname, String.valueOf(tsaroom), "","",""));      
                }
                tileTSA += tsaroom ;
            }
            txtTileTSA.setText(String.valueOf(tileTSA));
            
            updateTotalTile(arr);
            fetchDbTileToObsList();
            
            updateTileSummary();
                    
            tblTileRoom.setItems(troomObsList);
          
        }   catch(SQLException e){}
    }

    private void updateTotalTile(ArrayList<String> arr){
        Iterator<TileLabel> iter = tileObsList.iterator();
        while(iter.hasNext()){
            TileLabel g = iter.next();
            String sql0 = "UPDATE tileLabel SET total = 0 WHERE tileID = '"+g.getTileID()+"'";
//            System.out.println(sql0);
            Update(sql0);
        }

        for(int i=0; i<arr.size();i=i+2){
            String sql = "UPDATE tileLabel SET total = total + "+Integer.valueOf(arr.get(i))+" WHERE tileID = '"+arr.get(i+1)+"'";
//            System.out.println(sql);
            Update(sql);
        }
        
    }

    private void updateTileSummary(){
        int numtile = 0;
        int ttotprice = 0;
        
        Iterator<TileLabel> iter = tileObsList.iterator();
        while(iter.hasNext()){
            TileLabel g = iter.next();
            if(Integer.valueOf(g.getTotal()) != 0 ){
                numtile += Integer.valueOf(g.getTotal())+Integer.valueOf(g.getExtra());
                ttotprice += Integer.valueOf(g.getPrice())*(Integer.valueOf(g.getTotal())+Integer.valueOf(g.getExtra()));
            }
        }

        
        
        txtTotNumTile.setText(String.valueOf(numtile));
        txtTileTotPrice.setText(String.valueOf(ttotprice));
    }
    
    private ObservableList<Object> setUpTileComboBox(){
        try{
        rs = stmt.executeQuery("SELECT DISTINCT tileLabel FROM tilelabel WHERE PID = "+cl.getCID());
            while (rs.next()){
                tiledata.add(rs.getString("tileLabel"));
                
            }
        }catch(SQLException e){}
        return tiledata;
    }
    
    
    
    double ParseDouble(String strNumber) {
        if (strNumber != null && strNumber.length() > 0) {
            try {
               return Double.parseDouble(strNumber);
            } catch(Exception e) {
               return -1;   // or some value to mark this field is wrong. or make a function validates field first ...
            }
        }
        else return 0;
    }    
    
    
    @FXML
    private void changeScreen(ActionEvent event) {
        Button btn = (Button) event.getSource();
        switch(btn.getText()){
           case "Dashboard" : myController.reloadScreen(ScreensFramework.screen1ID, ScreensFramework.screen1File);
           break;
           case "Plan" : myController.reloadScreen(ScreensFramework.screen2ID, ScreensFramework.screen2File);
           break;
           case "Task" : myController.reloadScreen(ScreensFramework.screen3ID, ScreensFramework.screen3File);
           break;
           case "Service Provider" : myController.reloadScreen(ScreensFramework.screen4ID, ScreensFramework.screen4File);
           break;
           case "Expense" : myController.reloadScreen(ScreensFramework.screen5ID, ScreensFramework.screen5File);
           break;
           case "Photos" : myController.reloadScreen(ScreensFramework.screen6ID, ScreensFramework.screen6File);
           break;
           case "Calculator" : myController.reloadScreen(ScreensFramework.screen7ID, ScreensFramework.screen7File);
           break;
           case "Log Out" : myController.reloadScreen(ScreensFramework.loginID, ScreensFramework.loginFile);
           break;
        }
    }
    
    public void setScreenParent(ScreensController screenParent){
        myController = screenParent;
    }
    
    
}
