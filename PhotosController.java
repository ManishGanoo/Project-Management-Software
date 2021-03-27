import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.controlsfx.control.Notifications;

public class PhotosController extends dbManipulation implements Initializable , ControlledScreen{

    ScreensController myController;
    @FXML ScrollPane slPanePhotos;
   // @FXML TilePane tile;
    
    private static final int BUFFER_SIZE = 4096;
        
    @FXML Label lblSelPlan,lblAddPhoto;
    @FXML Button btnSelFile,btnUpload;
    File file; 
    String path = "C:/Users/user/Documents/NetBeansProjects/MergedClientApp_1/Photos/";

    Client cl;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cl = Context.getInstance().currentClient();
        
        lblAddPhoto.setVisible(false);
        lblSelPlan.setText("");
        btnSelFile.setVisible(false);
        btnUpload.setVisible(false);
//        setUpUploadPhotos();   
        readpdffromdb();
//        setPhotos();

    }   
    
    private void readpdffromdb(){
        
        try {
            String sql = ("SELECT photoName, photo FROM photos WHERE PID = ?");
            PreparedStatement statement = connection.prepareStatement(sql);
//            statement.setString(1, pdfName);
            statement.setInt(1, cl.getCID());
            
            rs = statement.executeQuery();
            if (rs.next()){          
                String outputfilePath = "C:\\Users\\user\\Documents\\NetBeansProjects\\MergedClientApp_1\\Photos\\"+cl.getCID()+"\\"+rs.getString("photoName") ;
                
                Blob blob = rs.getBlob("photo");
                InputStream inputStream = blob.getBinaryStream();
                
                OutputStream outputStream = new FileOutputStream(outputfilePath);
 
                int bytesRead = -1;
                byte[] buffer = new byte[BUFFER_SIZE];
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
//                System.out.println("Done");
                setPhotos();
                
                
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
        
//    private void setUpUploadPhotos(){
//        
//        btnSelFile.setOnMouseClicked(e -> {
//            FileChooser fileChooser = new FileChooser();
//            FileChooser.ExtensionFilter extFilterImg = new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.png");
//            fileChooser.getExtensionFilters().addAll(extFilterImg);
//            
//            file = fileChooser.showOpenDialog(null);
//            
//            if(file != null){
//                try {
//                    File source = new File(file.getAbsolutePath());
//                    File dest = new File ("C:\\Users\\user\\Documents\\NetBeansProjects\\MergedClientApp_1\\Photos\\"+cl.getCID()+"\\"+file.getName());
//                    
//                    if (!file.exists()) {
//                        if (file.mkdir()) {
//                            System.out.println("Directory is created!");
//                        } else {
//                            System.out.println("Failed to create directory!");
//                        }
//                    }
//                    
//                    FileUtils.copyFile(source, dest);
//                } catch (IOException ex) {
//                    ex.printStackTrace();
//                }
//                
//                lblSelPlan.setText(FilenameUtils.getBaseName(file.getName()));
//            }
//            
//        });           
//        
////        if(file != null){     
//            btnUpload.setOnMouseClicked(e -> {
//                if(lblSelPlan.getText().isEmpty() == false){
//                    uploadFile(file);
//                    setPhotos();
//                    lblSelPlan.setText("");
//                }
//                else{
//                    Notifications notificationBuilder = Notifications.create()
//                    .title("Error")
//                    .text("Please choose a file.")
//                    .graphic(null)
//                    .hideAfter(Duration.seconds(5))
//                    .position(Pos.TOP_RIGHT);
//                    //notificationBuilder.darkStyle();
//                    notificationBuilder.showError(); 
//                }
//            });   
////        }
// 
//    }
//    
//    
//    private void uploadFile(File file){
//        
//        if(file != null){
//            try {
//                String sql = "INSERT INTO photos(SID, PID, photoName, photo) VALUES(?,?,?,?)";
//
//                PreparedStatement statement = connection.prepareStatement(sql);
//                statement.setInt(1, 1); //service provider id
//                statement.setInt(2, cl.getCID());
//                statement.setString(3, file.getName());
//
//                InputStream inputStream = new FileInputStream(new File(file.getAbsolutePath()));
//                statement.setBlob(4, inputStream);
//
//                int row = statement.executeUpdate();
//                if (row > 0) {
//                    Notifications notificationBuilder = Notifications.create()
//                    .title("Upload Succesful")
//                    .text("Photos was uploaded successfully.")
//                    .graphic(null)
//                    .hideAfter(Duration.seconds(5))
//                    .position(Pos.TOP_RIGHT);
//                    //notificationBuilder.darkStyle();
//                    notificationBuilder.showInformation(); 
//                }
//                else{
//                    Notifications notificationBuilder = Notifications.create()
//                    .title("Upload Error")
//                    .text("Photos was not uploaded successfully.")
//                    .graphic(null)
//                    .hideAfter(Duration.seconds(5))
//                    .position(Pos.TOP_RIGHT);
//                    //notificationBuilder.darkStyle();
//                    notificationBuilder.showError(); 
//                }
//
//            } catch (SQLException ex) {
//                ex.printStackTrace();
//            } catch (IOException ex) {
//                ex.printStackTrace();
//            }
//        }
//          
//    }
    
    private void setPhotos(){
        TilePane tile = new TilePane();
        tile.setPadding(new Insets(15, 15, 15, 15));
        tile.setHgap(15);
        tile.setVgap(15);
        
        File folder = new File(path+cl.getCID());
        File[] listOfFiles = folder.listFiles();

        for (final File file : listOfFiles) {
                ImageView imageView;
                imageView = createImageView(file);
                tile.getChildren().addAll(imageView);
        }
        
        slPanePhotos.setContent(tile);
        slPanePhotos.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); // Horizontal
        slPanePhotos.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); // Vertical scroll bar
    }
        
    private ImageView createImageView(final File imageFile) {

        ImageView imageView = null;
        try {
            final Image image = new Image(new FileInputStream(imageFile), 600, 0, true,true);
            imageView = new ImageView(image);
            imageView.setFitWidth(600);
            imageView.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    if(mouseEvent.getButton().equals(MouseButton.PRIMARY)){
                        if(mouseEvent.getClickCount() == 2){
                            try {
                                BorderPane borderPane = new BorderPane();
                                ImageView imageView = new ImageView();
                                Image image = new Image(new FileInputStream(imageFile));
                                imageView.setImage(image);
                                imageView.setStyle("-fx-background-color: BLACK");
                                imageView.setFitHeight(915 - 10);
                                imageView.setPreserveRatio(true);
                                imageView.setSmooth(true);
                                imageView.setCache(true);
                                borderPane.setCenter(imageView);
                                borderPane.setStyle("-fx-background-color: BLACK");
                                Stage newStage = new Stage();
                                newStage.setWidth(1920);
                                newStage.setHeight(1000);
                                newStage.setTitle(imageFile.getName());
                                Scene scene = new Scene(borderPane,Color.BLACK);
                                newStage.setScene(scene);
                                newStage.show();
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
        return imageView;
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
