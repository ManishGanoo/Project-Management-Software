import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.controlsfx.control.Notifications;
import org.icepdf.ri.common.SwingController;
import org.icepdf.ri.common.SwingViewBuilder;


public class InitialInputMenuController extends dbManipulation implements Initializable, ControlledScreen{

    ScreensController myController; 
    
    ObservableList<room> listOfRoom = FXCollections.observableArrayList();
    ObservableList<Openings> listOfOp = FXCollections.observableArrayList();
    
    //private final String search_room = "SELECT room_name,room_type,height,length,width FROM roomDetails";
    //private final String search_op = "SELECT op_type,height,width FROM openings";
    //private fillTable fill;
    
    Client cl;

    int ProjectID;
    
    @FXML    private TableView<room> tblRooms;
    @FXML    private TextField txtRoomName;
    @FXML    private ComboBox cmbRoomType;
    @FXML    private TextField txtRoomHeight;
    @FXML    private TextField txtRoomLength;
    @FXML    private TextField txtRoomWidth;
    @FXML    private Button btnAddRoom, btnDelRoom;
    
    @FXML    private TableView<Openings> tblOpenings;
    @FXML    private ComboBox cmbOPtype;
    @FXML    private TextField txtOPheight, txtOPwidth;
    @FXML    private Button btnAddOP, btnDelOP;
    @FXML    private Button btnContinue;
    
    @FXML   Button  btnSelFile, btnUpload ;
    @FXML   Label lblSelPlan;
    @FXML   ComboBox cmbChoosePlan;
    @FXML   private SwingNode swingNode;
    
    private SwingController swingController;
    private JComponent viewerPanel;
    
    private static final int BUFFER_SIZE = 4096;
    
    File file; 
    ObservableList<Object> dataPlan = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        cl = Context.getInstance().currentClient();
        
        ProjectID = cl.getCID();
        
        createViewer();
        setUpUploadPlan();
        setUpComboBoxPlan();
        
        setRoomList();
        setRoomColumns();
        tblRooms.setEditable(true);
        tblRooms.setItems(listOfRoom);
        setRoomTypeComboBox();
        
        setOpeningsColumns();
        tblOpenings.setEditable(true);
        tblOpenings.setItems(listOfOp);
        
        ManageOperations();
        
        btnContinue.setOnMouseClicked(e -> {
            //validate
            //validate openings and rooms and pdf 
            myController.reloadScreen(ScreensFramework.screen1ID, ScreensFramework.screen1File); //go to Home for this project
            
            myController.refreshScreen(ScreensFramework.screen1ID, ScreensFramework.screen1File);
            myController.refreshScreen(ScreensFramework.screen2ID, ScreensFramework.screen2File);
            myController.refreshScreen(ScreensFramework.screen3ID, ScreensFramework.screen3File);
            myController.refreshScreen(ScreensFramework.screen4ID, ScreensFramework.screen4File);
            myController.refreshScreen(ScreensFramework.screen5ID, ScreensFramework.screen5File);
            myController.refreshScreen(ScreensFramework.screen6ID, ScreensFramework.screen6File);
            myController.refreshScreen(ScreensFramework.screen7ID, ScreensFramework.screen7File);
            
            myController.refreshScreen(ScreensFramework.screen7ID, ScreensFramework.screen7File);
        });
    }
    

//    private void setProjectID(){
//        try {
//        rs = stmt.executeQuery("SELECT PID FROM project WHERE CID='"+cl.getCID()+"' ORDER BY PID DESC LIMIT 1" );
//        while (rs.next()){
//            ProjectID = Integer.parseInt(rs.getString("PID"));
//            System.out.println("PID : "+ProjectID);
//        }
//        }catch(SQLException e){}
//    }
    
    
    private void setRoomList(){
        
        try{
        rs = stmt.executeQuery( "SELECT * FROM roomDetails WHERE PID = "+ProjectID);
        //rs = stmt.executeQuery( "SELECT * FROM roomDetails");
        
        while (rs.next()){
                   
            String RID = String.valueOf(rs.getInt("RID"));
            String rName = rs.getString("room_name");
            String rType = rs.getString("room_type");
            String rHeight = String.valueOf(rs.getDouble("height"));
            String rLength = String.valueOf(rs.getDouble("length"));
            String rWidth = String.valueOf(rs.getDouble("width"));
            String PID = String.valueOf(rs.getInt("PID"));
            
            listOfRoom.add(new room(RID, rName, rType, rHeight, rLength, rWidth, PID));
        } 
        }catch(SQLException e){}
        
    }
    
    private void setRoomColumns(){
        
        // room_name
        TableColumn rNameCol = new TableColumn("Room Name");

        rNameCol.setCellValueFactory(
            new PropertyValueFactory<room, String>("room_name"));
        rNameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        rNameCol.setOnEditCommit(
            new EventHandler<TableColumn.CellEditEvent<room, String>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<room, String> t) {
                    ((room) t.getTableView().getItems().get(
                            t.getTablePosition().getRow())
                            ).setRoom_name(t.getNewValue());
                    
                    String id = ((room) t.getTableView().getItems().get(
                        t.getTablePosition().getRow())
                        ).getRID();
                    
                    String sql = "UPDATE roomDetails SET room_name = '"+t.getNewValue()+"' WHERE RID = "+id+"";
                    Update(sql);
                }
            }
        );
        
        // room_type
        TableColumn rTypeCol = new TableColumn("Room Type");

        rTypeCol.setCellValueFactory(
            new PropertyValueFactory<room, String>("room_type"));
        rTypeCol.setCellFactory(ComboBoxTableCell.forTableColumn("Bedroom","Bathroom","Living Room","Study Room","TV Room","Dining Room","Kitchen","laundry Room","Garage"));
        rTypeCol.setOnEditCommit(
            new EventHandler<TableColumn.CellEditEvent<room, String>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<room, String> t) {
                    ((room) t.getTableView().getItems().get(
                            t.getTablePosition().getRow())
                            ).setRoom_type(t.getNewValue());
                    
                    String id = ((room) t.getTableView().getItems().get(
                        t.getTablePosition().getRow())
                        ).getRID();
                    
                    String sql = "UPDATE roomDetails SET room_type = '"+t.getNewValue()+"' WHERE RID = "+id+"";
                    Update(sql);
                }
            }
        );
        
        // room_height
        TableColumn rHeightCol = new TableColumn("Height");

        rHeightCol.setCellValueFactory(
            new PropertyValueFactory<room, String>("room_height"));
        rHeightCol.setCellFactory(TextFieldTableCell.forTableColumn());
        rHeightCol.setOnEditCommit(
            new EventHandler<TableColumn.CellEditEvent<room, String>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<room, String> t) {
                    ((room) t.getTableView().getItems().get(
                            t.getTablePosition().getRow())
                            ).setRoom_height(t.getNewValue());
                    
                    String id = ((room) t.getTableView().getItems().get(
                        t.getTablePosition().getRow())
                        ).getRID();
                    
                    String sql = "UPDATE roomDetails SET height = '"+t.getNewValue()+"' WHERE RID = "+id+"";
                    if(Validation.checkInteger(t.getNewValue()) == true){
                        Update(sql);    
                    }
                }
            }
        );
        
        // room_length
        TableColumn rLengthCol = new TableColumn("Length");

        rLengthCol.setCellValueFactory(
            new PropertyValueFactory<room, String>("room_length"));
        rLengthCol.setCellFactory(TextFieldTableCell.forTableColumn());
        rLengthCol.setOnEditCommit(
            new EventHandler<TableColumn.CellEditEvent<room, String>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<room, String> t) {
                    ((room) t.getTableView().getItems().get(
                            t.getTablePosition().getRow())
                            ).setRoom_length(t.getNewValue());
                    
                    String id = ((room) t.getTableView().getItems().get(
                        t.getTablePosition().getRow())
                        ).getRID();
                    
                    String sql = "UPDATE roomDetails SET length = '"+t.getNewValue()+"' WHERE RID = "+id+"";
                    if(Validation.checkInteger(t.getNewValue()) == true){
                        Update(sql);    
                    }
                }
            }
        );
        
        // room_width
        TableColumn rWidthCol = new TableColumn("Width");

        rWidthCol.setCellValueFactory(
            new PropertyValueFactory<room, String>("room_width"));
        rWidthCol.setCellFactory(TextFieldTableCell.forTableColumn());
        rWidthCol.setOnEditCommit(
            new EventHandler<TableColumn.CellEditEvent<room, String>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<room, String> t) {
                    ((room) t.getTableView().getItems().get(
                            t.getTablePosition().getRow())
                            ).setRoom_width(t.getNewValue());
                    
                    String id = ((room) t.getTableView().getItems().get(
                        t.getTablePosition().getRow())
                        ).getRID();
                    
                    String sql = "UPDATE roomDetails SET width = '"+t.getNewValue()+"' WHERE RID = "+id+"";
                    if(Validation.checkInteger(t.getNewValue()) == true){
                        Update(sql);    
                    }
                }
            }
        );
        
        rNameCol.setMinWidth(171);
        rTypeCol.setMinWidth(171);
        rHeightCol.setMinWidth(171);
        rLengthCol.setMinWidth(171);
        rWidthCol.setMinWidth(171);
        
        tblRooms.getColumns().addAll(rNameCol, rTypeCol, rHeightCol, rLengthCol, rWidthCol);
        
        tblRooms.setOnMouseClicked(e -> {
            
            if (tblRooms.getSelectionModel().getSelectedItem() != null) {
                
                int selectedIndex = tblRooms.getSelectionModel().getSelectedIndex(); 
                int RID = Integer.parseInt(listOfRoom.get(selectedIndex).getRID()); 
                
                setOpeningsList(RID);
                
            }     
        });
        
    }
    
    private void setRoomTypeComboBox(){
        
        cmbRoomType.getItems().addAll(
            "Bedroom",
            "Bathroom",
            "Living Room",
            "Study Room",
            "TV Room",
            "Dining Room",
            "Kitchen",
            "laundry Room",
            "Garage"      
        ); 
    }
    

    
    private void setOpeningsList(int RoomID){
        
        listOfOp.clear();
        
        try{
        rs = stmt.executeQuery( "SELECT * FROM openings WHERE RID ="+RoomID);

        while (rs.next()){
                   
            String OPID = String.valueOf(rs.getInt("OPID"));
            String ORID = String.valueOf(rs.getInt("RID"));
            String OPType = rs.getString("op_type");
            String OPHeight = String.valueOf(rs.getDouble("height"));
            String OPWidth = String.valueOf(rs.getDouble("width"));
            
            listOfOp.add(new Openings(OPID, ORID, OPType, OPHeight, OPWidth));
        } 
        }catch(SQLException e){}
        
    }
    
    private void setOpeningsColumns(){
        
        // op_type
        TableColumn oTypeCol = new TableColumn("Openings Type");
       
        oTypeCol.setCellValueFactory(new PropertyValueFactory<Openings, String>("op_type"));
        oTypeCol.setCellFactory(ComboBoxTableCell.forTableColumn("Door", "Window"));
        oTypeCol.setOnEditCommit(
            new EventHandler<TableColumn.CellEditEvent<Openings, String>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<Openings, String> t) {
                    ((Openings) t.getTableView().getItems().get(
                            t.getTablePosition().getRow())
                            ).setOp_type(t.getNewValue());
                    
                    String OP_id = ((Openings) t.getTableView().getItems().get(
                        t.getTablePosition().getRow())
                        ).getOPID();
                    
                    String sql = "UPDATE openings SET op_type = '"+t.getNewValue()+"' WHERE OPID = "+OP_id+"";
                    Update(sql);
                }
            }
        );
        
        // op_height
        TableColumn oHeightCol = new TableColumn("Height");
        oHeightCol.setCellValueFactory(
            new PropertyValueFactory<Openings, String>("op_height"));
        oHeightCol.setCellFactory(TextFieldTableCell.forTableColumn());
        oHeightCol.setOnEditCommit(
            new EventHandler<TableColumn.CellEditEvent<Openings, String>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<Openings, String> t) {
                    ((Openings) t.getTableView().getItems().get(
                            t.getTablePosition().getRow())
                            ).setOp_height(t.getNewValue());
                    
                    String OP_id = ((Openings) t.getTableView().getItems().get(
                        t.getTablePosition().getRow())
                        ).getOPID();
                    
                    String sql = "UPDATE openings SET height = '"+t.getNewValue()+"' WHERE OPID = "+OP_id+"";
                    
                    if(Validation.checkInteger(t.getNewValue()) == true){
                        Update(sql);    
                        Update("UPDATE roomdetails SET openingTSA = openingTSA - ("+Double.parseDouble(t.getTableView().getItems().get(t.getTablePosition().getRow()).getOp_width())*(Double.parseDouble(t.getOldValue()))+") + ("+Double.parseDouble(t.getTableView().getItems().get(t.getTablePosition().getRow()).getOp_width())*(Double.parseDouble(t.getNewValue()))+") WHERE RID="+t.getTableView().getItems().get(t.getTablePosition().getRow()).getORID());
                    }
                }
            }
        );
        
        // op_width
        TableColumn oWidthCol = new TableColumn("Width");

        oWidthCol.setCellValueFactory(
            new PropertyValueFactory<Openings, String>("op_width"));
        oWidthCol.setCellFactory(TextFieldTableCell.forTableColumn());
        oWidthCol.setOnEditCommit(
            new EventHandler<TableColumn.CellEditEvent<Openings, String>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<Openings, String> t) {
                    ((Openings) t.getTableView().getItems().get(
                            t.getTablePosition().getRow())
                            ).setOp_width(t.getNewValue());
                    
                    String OP_id = ((Openings) t.getTableView().getItems().get(
                        t.getTablePosition().getRow())
                        ).getOPID();
                    
                    String sql = "UPDATE openings SET width = '"+t.getNewValue()+"' WHERE OPID = "+OP_id+"";
                    if(Validation.checkInteger(t.getNewValue()) == true){
                        Update(sql);  
                        Update("UPDATE roomdetails SET openingTSA = openingTSA - ("+Double.parseDouble(t.getTableView().getItems().get(t.getTablePosition().getRow()).getOp_height())*(Double.parseDouble(t.getOldValue()))+") + ("+Double.parseDouble(t.getTableView().getItems().get(t.getTablePosition().getRow()).getOp_height())*(Double.parseDouble(t.getNewValue()))+") WHERE RID="+t.getTableView().getItems().get(t.getTablePosition().getRow()).getORID());
                    }
                }
            }
        );
        
        oTypeCol.setMinWidth(200);
        oHeightCol.setMinWidth(200);
        oWidthCol.setMinWidth(200);
        
        tblOpenings.getColumns().addAll(oTypeCol, oHeightCol, oWidthCol);
        
        cmbOPtype.getItems().addAll("Door","Window");
    }

    
    private void ManageOperations(){
    
        btnAddRoom.setOnMouseClicked(e ->{
            
            String RoomName = txtRoomName.getText();
            String RoomType = cmbRoomType.getSelectionModel().getSelectedItem().toString();
            String RoomHeight = txtRoomHeight.getText();
            String RoomLength = txtRoomLength.getText();
            String RoomWidth = txtRoomWidth.getText();

//            setProjectID();
            //String RID =" ";
            if(Validation.checkRoomDimensions(RoomHeight, RoomLength, RoomWidth) == true){
                Insert("INSERT INTO roomDetails(room_name,room_type,height,length,width,openingTSA,PID) VALUES('"+RoomName+"','"+RoomType+"',"+RoomHeight+","+RoomLength+","+RoomWidth+",0.0,"+ProjectID+");"); 
            }
            
            listOfRoom.clear();
            setRoomList();
            //listOfRoom.add(new room(RID,RoomName,RoomType,RoomHeight,RoomLength,RoomWidth,String.valueOf(ProjectID)));
            tblRooms.setItems(listOfRoom);

           txtRoomName.clear();
           cmbRoomType.getEditor().clear();
           cmbRoomType.setValue(null);
           txtRoomHeight.clear();
           txtRoomLength.clear();
           txtRoomWidth.clear();
        });
        
        
        btnDelRoom.setOnMouseClicked(e ->{
            
             int selectedIndex = tblRooms.getSelectionModel().getSelectedIndex(); 
             int RID = Integer.parseInt(listOfRoom.get(selectedIndex).getRID()); 
                
            if (selectedIndex >= 0) {
                tblRooms.getItems().remove(selectedIndex);
                //String id = tblRooms.getSelectionModel().getSelectedItem().getRID();
                //Delete("DELETE FROM roomDetails WHERE RID = "+RID+"");
                Delete("DELETE FROM roomDetails WHERE RID = "+RID+"");
                //fill = new fillTable(tblExpense, search);
            }
        });
        
        
        btnAddOP.setOnMouseClicked(e -> {
            
            String OPtype = cmbOPtype.getSelectionModel().getSelectedItem().toString();
            String OPheight = txtOPheight.getText();
            String OPwidth = txtOPwidth.getText();
            
            int RoomIndex = tblRooms.getSelectionModel().getSelectedIndex();
            int RID = Integer.parseInt(listOfRoom.get(RoomIndex).getRID());
            
            
            if(Validation.checkOpeningDimensions(OPheight, OPwidth) == true){
                Insert("INSERT INTO openings(RID,op_type,height,width) VALUES ("+RID+",'"+OPtype+"',"+OPheight+","+OPwidth+")");
                Update("UPDATE roomdetails SET openingTSA = openingTSA + " +(Double.parseDouble(OPheight))*(Double.parseDouble(OPwidth))+ " WHERE RID="+RID);
            }
                        
         
            setOpeningsList(RID);
            tblOpenings.setItems(listOfOp);
            
            cmbOPtype.getEditor().clear();
            cmbOPtype.setValue(null);
            txtOPheight.clear();
            txtOPwidth.clear();
            
        });
        
        
        btnDelOP.setOnMouseClicked(e -> {
            
            int sIndex = tblOpenings.getSelectionModel().getSelectedIndex(); 
            int OPID = Integer.parseInt(listOfOp.get(sIndex).getOPID());
             
            if (sIndex >= 0) {
                Update("UPDATE roomdetails SET openingTSA = openingTSA - " +(Integer.parseInt(listOfOp.get(sIndex).getOp_height()))*(Integer.parseInt(listOfOp.get(sIndex).getOp_width()))+ " WHERE RID="+listOfOp.get(sIndex).getORID());
                
                tblOpenings.getItems().remove(sIndex);
                //String id = tblOpenings.getSelectionModel().getSelectedItem().getOPID();
                Delete("DELETE FROM openings WHERE OPID = "+OPID+"");
                //System.out.println("DELETE FROM openings WHERE OPID = "+OPID+"");
                
                int RoomIndex = tblRooms.getSelectionModel().getSelectedIndex();
                int RID = Integer.parseInt(listOfRoom.get(RoomIndex).getRID());
                setOpeningsList(RID);
                tblOpenings.setItems(listOfOp);
            }
        });
        
       
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    private void setUpComboBoxPlan(){
        dataPlan.clear();
        try{
            rs = stmt.executeQuery("SELECT planName FROM pdfplan WHERE PID = "+cl.getCID());
            while (rs.next()){
                dataPlan.add(rs.getString("planName"));
            }
        }catch(SQLException e){}
        cmbChoosePlan.setPromptText("Select Plan");
        cmbChoosePlan.setItems(dataPlan);
        
        cmbChoosePlan.getSelectionModel().selectedItemProperty().addListener( (options, oldValue, newValue) -> {   
            int selectedIndex;
            selectedIndex = cmbChoosePlan.getSelectionModel().getSelectedIndex();
            readpdffromdb(dataPlan.get(selectedIndex).toString());
//            openDocument("C:\\Users\\user\\Documents\\NetBeansProjects\\MergedClientApp_1\\Plan\\"+dataPlan.get(selectedIndex).toString()+".pdf");
        });
    }
        
    private void readpdffromdb(String pdfName){
        
        try {
            String sql = ("SELECT pdf FROM pdfplan WHERE planName = ? AND PID = ?");
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, pdfName);
            statement.setInt(2, cl.getCID());
            
            rs = statement.executeQuery();
            if (rs.next()){          
                String outputfilePath = "C:\\Users\\user\\Documents\\NetBeansProjects\\MergedClientApp_1\\Plan\\"+cl.getCID()+"\\"+pdfName+".pdf" ;
                
                Blob blob = rs.getBlob("pdf");
                InputStream inputStream = blob.getBinaryStream();
                
                OutputStream outputStream = new FileOutputStream(outputfilePath);
 
                int bytesRead = -1;
                byte[] buffer = new byte[BUFFER_SIZE];
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead); //create a pdf file from resulset
                }

                openDocument(outputfilePath); //icepdf method to open the pdf file
                
                inputStream.close();
                outputStream.close();
                
                System.out.println("File saved");
            }
        
        } catch (SQLException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
    }    
    
    private void setUpUploadPlan(){
        
        btnSelFile.setOnMouseClicked(e -> {
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter extFilterPDF = new FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.PDF");
            fileChooser.getExtensionFilters().addAll(extFilterPDF);
            
            file = fileChooser.showOpenDialog(null);
            try {
                File source = new File(file.getAbsolutePath());
                File dest = new File ("C:\\Users\\user\\Documents\\NetBeansProjects\\MergedClientApp_1\\Plan\\"+cl.getCID()+"\\"+file.getName());
                FileUtils.copyFile(source, dest);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            
            lblSelPlan.setText(FilenameUtils.getBaseName(file.getName()));

        });           
             
        btnUpload.setOnMouseClicked(e -> {
            uploadFile(file);
            System.out.println(file.getAbsolutePath());
            setUpComboBoxPlan();
        });    

           
    }
    
    private void uploadFile(File file){
        if(file != null){
            try {

                String sql = "INSERT INTO pdfplan(PID, planName, plan_type, pdf) VALUES(?,?,?,?)";
                PreparedStatement statement = connection.prepareStatement(sql);

                statement.setInt(1, cl.getCID());
                statement.setString(2, FilenameUtils.getBaseName(file.getName()));
                statement.setString(3, "General");

                InputStream inputStream = new FileInputStream(new File(file.getAbsolutePath()));
                statement.setBlob(4, inputStream);

                int row = statement.executeUpdate();
                if (row > 0) {
                    Notifications notificationBuilder = Notifications.create()
                    .title("Upload Succesful")
                    .text("Plan was uploaded successfully.")
                    .graphic(null)
                    .hideAfter(Duration.seconds(5))
                    .position(Pos.TOP_RIGHT);
                    //notificationBuilder.darkStyle();
                    notificationBuilder.showInformation(); 
                }
                else{
                    Notifications notificationBuilder = Notifications.create()
                    .title("Upload Error")
                    .text("Plan was not uploaded successfully.")
                    .graphic(null)
                    .hideAfter(Duration.seconds(5))
                    .position(Pos.TOP_RIGHT);
                    //notificationBuilder.darkStyle();
                    notificationBuilder.showError(); 
                }

            } catch (SQLException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }    
    private void createViewer() {
        try {
            SwingUtilities.invokeAndWait(() -> {
                // create the viewer ri components.
                swingController = new SwingController();
                swingController.setIsEmbeddedComponent(true);
                swingController.getDocumentViewController().setAnnotationCallback(
                new org.icepdf.ri.common.MyAnnotationCallback(swingController.getDocumentViewController()));
                
                SwingViewBuilder factory = new SwingViewBuilder(swingController);
                viewerPanel = factory.buildViewerPanel();
                swingNode.setContent(viewerPanel);
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
    private void openDocument(String document) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                swingController.openDocument(document);
                viewerPanel.revalidate();
            }
        });
    }
    
    
    
    
    
    
    
    
    
    
    
    

    
    public void setScreenParent(ScreensController screenParent){
        myController = screenParent;
    }
    
}
