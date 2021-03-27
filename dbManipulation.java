import java.sql.*;
import java.util.ArrayList;

public abstract class dbManipulation {
    protected static Connection connection;
    protected static Statement stmt;
    protected static ResultSet rs;
    protected static dbConnect dbconnect=dbConnect.getInstance();

    public dbManipulation() {

        try {
            connection = dbconnect.getConnection();
            stmt = connection.createStatement();
        } catch (SQLException e) {
        }
    }

    public static void Insert(String sql){
        try{
            stmt.executeUpdate(sql);
        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    public static void Update(String sql){
        try{
            stmt.executeUpdate(sql);
        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    
    public static void Delete(String sql){
        try{
            stmt.executeUpdate(sql);
        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

//    public static ArrayList getList(int selectedID){
//        ArrayList arrayList=new ArrayList();
//
//        try {
//            String sql="SELECT Invoice_No\n" +
//                    "FROM tblSales\n" +
//                    "WHERE  Cid="+selectedID+" AND Paid='n'";
//            rs = stmt.executeQuery( sql);
//            while (rs.next()){
//                arrayList.add((rs.getInt("Invoice_No")));
//            }
//        }
//        catch (SQLException e){}
//        return arrayList;
//    }
}
