import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class ScreensFramework extends Application{
    public static String loginID = "LoginForm";
    public static String loginFile = "LoginForm.fxml";
    
    public static String createAccID = "CreateAccount";
    public static String createAccFile = "CreateAccount.fxml";
   
    public static String projectID = "ProjectMenu";
    public static String projectFile = "ProjectMenu.fxml";
    
    public static String inputmenuID = "InitialInputMenu";
    public static String inputmenuFile = "InitialInputMenu.fxml";
    
    public static String screen1ID = "home";
    public static String screen1File = "Home.fxml";
    
    public static String screen2ID = "Plan";
    public static String screen2File = "Plan.fxml";
    
    public static String screen3ID = "Task";
    public static String screen3File = "Task.fxml";
    
    public static String screen4ID = "SP_Screen";
    public static String screen4File = "SP_Screen.fxml";
    
    public static String screen5ID = "Expense";
    public static String screen5File = "Expense.fxml";
    
    public static String screen6ID = "Photos";
    public static String screen6File = "Photos.fxml";
    
    public static String screen7ID = "Calculator";
    public static String screen7File = "Calculator.fxml";
   
   //SP_SIDE
    
    public static String SP_Side_ProfileID = "SP_Side_Profile";
    public static String SP_Side_ProfileFile = "SP_Side_Profile.fxml";
    
    public static String SP_Side_ClientID = "SP_Side_Client";
    public static String SP_Side_ClientFile = "SP_Side_Client.fxml";
    
    public static String SP_Side_ClientPhotoID = "SP_Side_CPhotos";
    public static String SP_Side_ClientPhotoFile = "SP_Side_ClientPhotos.fxml";

    public static String SP_Side_QuotationID = "SP_Side_Quotation";
    public static String SP_Side_QuotationFile = "SP_Side_Quotation.fxml";
    
    public static String SP_Side_SendQuotationID = "SP_Side_SendQuotation";
    public static String SP_Side_SendQuotationFile = "SP_Side_SendQuotation.fxml";
    
    @Override
   
    public void start(Stage primaryStage) throws Exception {
        // AnchorPane root = (AnchorPane) FXMLLoader.load(getClass().getResource("drawPlan.fxml"));
        
        ScreensController mainContainer = new ScreensController();
        mainContainer.loadScreen(ScreensFramework.loginID, ScreensFramework.loginFile);
        mainContainer.loadScreen(ScreensFramework.createAccID, ScreensFramework.createAccFile);
        mainContainer.loadScreen(ScreensFramework.projectID, ScreensFramework.projectFile);
        mainContainer.loadScreen(ScreensFramework.inputmenuID, ScreensFramework.inputmenuFile);


//        //SP_Side
//        mainContainer.loadScreen(ScreensFramework.SP_Side_ProfileID, ScreensFramework.SP_Side_ProfileFile);
//        mainContainer.loadScreen(ScreensFramework.SP_Side_ClientID, ScreensFramework.SP_Side_ClientFile);
//        mainContainer.loadScreen(ScreensFramework.SP_Side_QuotationID, ScreensFramework.SP_Side_QuotationFile);
//        mainContainer.loadScreen(ScreensFramework.SP_Side_SendQuotationID, ScreensFramework.SP_Side_SendQuotationFile);
                
        mainContainer.setScreen(ScreensFramework.loginID);
//        mainContainer.setScreen(ScreensFramework.projectID);
        
        Group root = new Group();
        root.getChildren().addAll(mainContainer);
        
 
        
        Scene scene = new Scene(root);
        
        primaryStage.setScene(scene);

        primaryStage.setTitle("Home Construction Planner");
        primaryStage.setMaximized(true);
        
        primaryStage.show();
        
        //below close child thread create by the application
        primaryStage.setOnCloseRequest((ae) -> {
            Platform.exit();
            System.exit(0);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
