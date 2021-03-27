import java.sql.Connection;
import java.sql.DriverManager;

public class dbConnect {
    private  Connection connect;
    private static boolean instance_flag=false;

    public static dbConnect getInstance(){
        if (instance_flag){
            //PopUp.DisplayMessage(new JFrame(),"ONLY 1 INSTANCE AT A TIME","ERROR",JOptionPane.ERROR_MESSAGE);
            System.out.println("ONLY 1 INSTANCE OF DB AT A TIME");
            return null;
        }
        else {
            instance_flag=true;
            return new dbConnect();
        }
    }
    private dbConnect(){
        Connect();
    }

    public void Connect(){
        try{
            try{
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            }
            catch(Exception e){}
//            //gearhost
//            String url = "jdbc:mysql://den1.mysql4.gear.host:3306/dbhomeplanner?autoReconnect=true&useSSL=false";
//            connect = DriverManager.getConnection(url,"dbhomeplanner","Ag47~?OL2wq5");
            
////            azure
//            String url = "jdbc:sqlserver://homeprojectserver.database.windows.net:1433;database=homeconstructionplanner;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;";
//            connect = DriverManager.getConnection(url, "serveradmin@homeprojectserver", "Ag47~?OL2wq5");
            
            //sql server
            String url = "jdbc:mysql://localhost:3306/homeconstructionplanner1?autoReconnect=true&useSSL=false";
            connect = DriverManager.getConnection(url,"root","root");
           
            if(connect!=null){
//               System.out.println("Connected");
           }
           else{
               System.out.println("Not Connected");
           }
        }

        catch(Exception e){
            //System.out.println("Not Connected");
            //System.out.println(e.getMessage());
            System.out.println("Error: " + e);
        }
    }
    public  Connection getConnection(){
        Connect();
        return connect;
    }

    public void CloseConnection(){
        try{
            connect.close();
        }
        catch(Exception e){
            System.out.println("Error: " + e);
        }
    }
}
