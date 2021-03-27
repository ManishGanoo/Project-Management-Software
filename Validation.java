
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.util.Duration;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.controlsfx.control.Notifications;



public abstract class Validation extends dbManipulation{
    static Client cl;
    
  
    
    static LocalDate today = LocalDate.now();
    
    public static boolean validateClientTextField(String cname, String cphone, String cemail, String cadd, String cuname, String cpass, String cpass1){
        //if textfield empty return false and notify empty 
        //if username already exist, return false and notify to choose another username
        //if pass != pass1 return false and notify password doesnt match
        //else return true
        
        if(cname.equals("") || cphone.equals("") ||cemail.equals("") ||cadd.equals("") || cuname.equals("") || cpass.equals("") || cpass1.equals("") ){
            Notifications notificationBuilder = Notifications.create()
            .title("Missing Values")
            .text("Please fill in all the values")
            .graphic(null)
            .hideAfter(Duration.seconds(5))
            .position(Pos.TOP_RIGHT);
             notificationBuilder.showError(); 
            return false;
        }
        
        if(cname.matches("[a-zA-Z\\s']+") == false ){
            Notifications notificationBuilder = Notifications.create()
            .title("Incorrect Values")
            .text("Name can also consist of alphabets")
            .graphic(null)
            .hideAfter(Duration.seconds(5))
            .position(Pos.TOP_RIGHT);
             notificationBuilder.showError(); 
             return false;
        }
        
        if(cphone.matches("5[0-9]{7}|[0-9]{7}") == false ){
            Notifications notificationBuilder = Notifications.create()
            .title("Incorrect Values")
            .text("Phone number can also consist of 8 digits starting with 5, or 7 digits")
            .graphic(null)
            .hideAfter(Duration.seconds(5))
            .position(Pos.TOP_RIGHT);
             notificationBuilder.showError();
             return false;
        }  
        
        if(checkUserName(cuname) == false){
            Notifications notificationBuilder = Notifications.create()
            .title("Invalid username")
            .text("Username already exist, please choose another one")
            .graphic(null)
            .hideAfter(Duration.seconds(5))
            .position(Pos.TOP_RIGHT);
             notificationBuilder.showError(); 
            return false;
        }
        if(cpass.equals(cpass1)== false){
            Notifications notificationBuilder = Notifications.create()
            .title("Error")
            .text("Both password are not identical")
            .graphic(null)
            .hideAfter(Duration.seconds(5))
            .position(Pos.TOP_RIGHT);
             notificationBuilder.showError(); 
            return false;
        }
        
        return true;
    }
    
    public static boolean checkUserName(String cuname){
        System.out.println("username");
        try {
            rs = stmt.executeQuery("SELECT username FROM users ");
            while(rs.next()){
                if(cuname.equals(rs.getString("username"))){
                    return false;
                };
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(CreateAccountController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return true;
    }
    
   
    public static boolean validateSPTextField(String sname, String sphone, String semail, String sadd, String stype, String suname, String spass, String spass1){
        if(sname.equals("") || sphone.equals("") ||semail.equals("") ||sadd.equals("") || stype.isEmpty() || suname.equals("") || spass.equals("") || spass1.equals("") ){
            Notifications notificationBuilder = Notifications.create()
            .title("Missing Values")
            .text("Please fill in all the values")
            .graphic(null)
            .hideAfter(Duration.seconds(5))
            .position(Pos.TOP_RIGHT);
             notificationBuilder.showError(); 
            return false;
        }
        
        if(sname.matches("[a-zA-Z\\s']+") == false ){
            Notifications notificationBuilder = Notifications.create()
            .title("Incorrect Values")
            .text("Name can also consist of alphabets")
            .graphic(null)
            .hideAfter(Duration.seconds(5))
            .position(Pos.TOP_RIGHT);
             notificationBuilder.showError(); 
             return false;
        }
        
        if(sphone.matches("5[0-9]{7}|[0-9]{7}") == false ){
            Notifications notificationBuilder = Notifications.create()
            .title("Incorrect Values")
            .text("Phone number can also consist of 8 digits starting with 5, or 7 digits")
            .graphic(null)
            .hideAfter(Duration.seconds(5))
            .position(Pos.TOP_RIGHT);
             notificationBuilder.showError(); 
             return false;
        }  
        
        if(checkSPUserName(suname) == true){
            Notifications notificationBuilder = Notifications.create()
            .title("Invalid username")
            .text("Username already exist, please choose another one")
            .graphic(null)
            .hideAfter(Duration.seconds(5))
            .position(Pos.TOP_RIGHT);
             notificationBuilder.showError(); 
             
            return false;
        }
        if(spass.equals(spass1)== false){
            Notifications notificationBuilder = Notifications.create()
            .title("Error")
            .text("Both password are not identical")
            .graphic(null)
            .hideAfter(Duration.seconds(5))
            .position(Pos.TOP_RIGHT);
             notificationBuilder.showError(); 
             
            return false;
        }
        
        return true;

    }
    
    public static boolean checkSPUserName(String suname){
        try {
            rs = stmt.executeQuery("SELECT username FROM users ");
            while(rs.next()){
                if(suname.equals(rs.getString("username"))){
                    return true;
                };
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(CreateAccountController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return false;
    }
    
    public static boolean checkNewProjectDetails(String txtPName,String txtPAdd,String txtPBud,String txtPNoFloors,LocalDate dtpTempSDate){
        
        if(txtPBud.matches("[0-9]+") == false ){
            Notifications notificationBuilder = Notifications.create()
            .title("Incorrect Values")
            .text("Budget can also consist of digits")
            .graphic(null)
            .hideAfter(Duration.seconds(5))
            .position(Pos.TOP_RIGHT);
             notificationBuilder.showError(); 
             
            return false;
        } 
        
        if(Integer.valueOf(txtPBud) < 0){
            Notifications notificationBuilder = Notifications.create()
            .title("Incorrect Values")
            .text("Budget cannot be negative")
            .graphic(null)
            .hideAfter(Duration.seconds(5))
            .position(Pos.TOP_RIGHT);
             notificationBuilder.showError(); 
             
            return false;
        }
        
        if(txtPNoFloors.matches("[0-9]+") == false ){
            Notifications notificationBuilder = Notifications.create()
            .title("Incorrect Values")
            .text("Number of floors can also consist of digits")
            .graphic(null)
            .hideAfter(Duration.seconds(5))
            .position(Pos.TOP_RIGHT);
             notificationBuilder.showError(); 
            
            return false;
        } 
        
        if(Integer.valueOf(txtPNoFloors) < 0){
            Notifications notificationBuilder = Notifications.create()
            .title("Incorrect Values")
            .text("Number of floors cannot be negative")
            .graphic(null)
            .hideAfter(Duration.seconds(5))
            .position(Pos.TOP_RIGHT);
             notificationBuilder.showError(); 
             
            return false;
        }
        
        
        if(dtpTempSDate.isBefore(today)){
            Notifications notificationBuilder = Notifications.create()
            .title("Incorrect Values")
            .text("Date cannot be before todays date")
            .graphic(null)
            .hideAfter(Duration.seconds(5))
            .position(Pos.TOP_RIGHT);
             notificationBuilder.showError(); 
            return false;
        }
        
        return true;
    }
    
    public static boolean checkTodayDate(LocalDate ldate){
        if(ldate.isBefore(today)){
            Notifications notificationBuilder = Notifications.create()
            .title("Incorrect Values")
            .text("Date cannot be before todays date")
            .graphic(null)
            .hideAfter(Duration.seconds(5))
            .position(Pos.TOP_RIGHT);
             notificationBuilder.showError(); 
            return false;
        }
        return true;
    }
    
    public static boolean checkVerificationDate(String vdetail, LocalDate vdate) throws SQLException {
        
        if(vdetail.equals("") || vdate.equals("")){
            Notifications notificationBuilder = Notifications.create()
            .title("Missing Values")
            .text("Please input the necessary values")
            .graphic(null)
            .hideAfter(Duration.seconds(5))
            .position(Pos.TOP_RIGHT);
             notificationBuilder.showError(); 
            return false;
        }
        
        if(vdetail.matches("[a-zA-Z\\s']+") == false ){
            Notifications notificationBuilder = Notifications.create()
            .title("Incorrect Values")
            .text("Verification Name can also consist of alphabets")
            .graphic(null)
            .hideAfter(Duration.seconds(5))
            .position(Pos.TOP_RIGHT);
             notificationBuilder.showError(); 
        }
        
        if(checkdatewithtempsDate(vdate) == false){
//            Notifications notificationBuilder = Notifications.create()
//            .title("Incorrect Values")
//            .text("Date cannot be before todays date")
//            .graphic(null)
//            .hideAfter(Duration.seconds(5))
//            .position(Pos.TOP_RIGHT);
//             notificationBuilder.showError(); 
            return false;
        }
                
        return true;
    }
    
    
    public static boolean checkPhone(String phone){    
        if(phone.matches("5[0-9]{7}|[0-9]{7}") == false ){
            Notifications notificationBuilder = Notifications.create()
            .title("Incorrect Values")
            .text("Phone number can also consist of 8 digits starting with 5, or 7 digits")
            .graphic(null)
            .hideAfter(Duration.seconds(5))
            .position(Pos.TOP_RIGHT);
             notificationBuilder.showError(); 
            return false;
        } 
        return true;
    }
        
    public static boolean checkAlphabet(String alpha){    
        if(alpha.matches("[a-zA-Z\\s']+") == false ){
            Notifications notificationBuilder = Notifications.create()
            .title("Incorrect Values")
            .text("Can only consist of alphabets")
            .graphic(null)
            .hideAfter(Duration.seconds(5))
            .position(Pos.TOP_RIGHT);
             notificationBuilder.showError(); 
            return false;
        } 
        return true;
    }
    
    public static boolean checkInteger(String num){    
        if(num.matches("[0-9]+|[0-9]+.[0-9]{1}|[0-9]+.[0-9]{2}") == false ){
            Notifications notificationBuilder = Notifications.create()
            .title("Incorrect Values")
            .text("Can only consist of digits")
            .graphic(null)
            .hideAfter(Duration.seconds(5))
            .position(Pos.TOP_RIGHT);
             notificationBuilder.showError(); 
            return false;
        } 
        return true;
    }
    
    public static boolean checkRoomDimensions(String height, String length, String width){
        if(checkInteger(height) && checkInteger(length) && checkInteger(width)){
            
            return true;
        }
        
        return false;
    }
         
    public static boolean checkOpeningDimensions(String height,String width){
        if(checkInteger(height) && checkInteger(width)){
            
            return true;
        }
        return false;
    }
    
    public static boolean checkdatewithtempsDate(LocalDate thisdate) throws SQLException{
        cl = Context.getInstance().currentClient();
        Date sDate = new Date();
        rs = stmt.executeQuery("SELECT * FROM project WHERE PID = "+cl.getCID());
        while (rs.next()){
            sDate = rs.getDate("temp_StartDate");
        }
        
        Calendar cal = Calendar.getInstance();
        cal.setTime(sDate);
        LocalDate lDate = LocalDate.of(cal.get(Calendar.YEAR),
        cal.get(Calendar.MONTH) + 1,
        cal.get(Calendar.DAY_OF_MONTH));
        
        if(thisdate.isBefore(lDate)){
            Notifications notificationBuilder = Notifications.create()
            .title("Incorrect Values")
            .text("Date cannot be before start date of the project")
            .graphic(null)
            .hideAfter(Duration.seconds(5))
            .position(Pos.TOP_RIGHT);
             notificationBuilder.showError(); 
            return false;
        }
        return true;
    }
    
    public static boolean checkQuotation(LocalDate sdate, String dur, String lcost, ObservableList rm) throws SQLException{
        
        if(checkisEmptyObsList(rm)){
            Notifications notificationBuilder = Notifications.create()
            .title("Missing Values: raw material table empty")
            .text("Please fill in raw material table")
            .graphic(null)
            .hideAfter(Duration.seconds(5))
            .position(Pos.TOP_RIGHT);
             notificationBuilder.showError(); 
            return false;
        }
        if(!checkdatewithtempsDate(sdate)){
            return false;
        }
        
        if(!checkInteger(dur)){
            return false;
        }
        
        if (!checkInteger(lcost)){
            return false;
        }
        
        return true;
    }
    
    public static boolean  checkisEmptyObsList(ObservableList obs){
        if(obs.isEmpty()){
            return false;
        }
        return true;
    }

    public static boolean checkrmdetails(String name, String price, String qty) {
        if(name.equals("") || price.equals("") || qty.equals("")){
            Notifications notificationBuilder = Notifications.create()
            .title("Missing Values")
            .text("Please fill in all the required details for raw material")
            .graphic(null)
            .hideAfter(Duration.seconds(5))
            .position(Pos.TOP_RIGHT);
             notificationBuilder.showError(); 
            return false;
        }
        
        if(!checkInteger(price)){
            return false;
        }
        
        if(!checkInteger(qty)){
            return false;
        }
        
        return true;
    }

    public static boolean checkEditSP(String name, String phone, String email, String type) {
        if(!checkAlphabet(name)){
            return false;
        }
        if(!checkPhone(phone)){
            return false;
        }
        
        if(!checkspType(type)){
            return false;
        }
        
        return true;
    }
    
    public static boolean checkspType(String type){
        String[] spType={"Masonry","Roof-Formwork","Roof-Concrete","Openings","Electrical","Plumbing","Flooring","Painting"};
        
        for(int i = 0; i<8;i++){
            if(type.equals(spType[i])){
                return true;
            }
        }
        
        Notifications notificationBuilder = Notifications.create()
        .title("Incorrect Values")
        .text("Type should be among Masonry,Roof-Formwork,Roof-Concrete,Openings,Electrical,Plumbing,Flooring,Painting")
        .graphic(null)
        .hideAfter(Duration.seconds(5))
        .position(Pos.TOP_RIGHT);
         notificationBuilder.showError(); 
        return false;
        
    }

    public static boolean checknewsp(String name, String phone, String email, String type, String add) {
        if(name.equals("") || phone.equals("") || email.equals("") || type.equals("") || add.equals("")){
            Notifications notificationBuilder = Notifications.create()
            .title("Missing Values")
            .text("Please fill in the missing values")
            .graphic(null)
            .hideAfter(Duration.seconds(5))
            .position(Pos.TOP_RIGHT);
             notificationBuilder.showError(); 
            return false;
        }
        
        if(!checkAlphabet(name)){
            return false;
        }
        
        if(!checkPhone(phone)){
            return false;
        }
        
        
        return true;
    }


    public static boolean checkTaskDetails(String name, String detail, LocalDate date, String dura, String tstatus) throws SQLException {
        if(name.equals("") || detail.equals("") || date.equals("") || dura.equals("") || tstatus.equals("")){
            Notifications notificationBuilder = Notifications.create()
            .title("Missing Values")
            .text("Please fill in the missing values")
            .graphic(null)
            .hideAfter(Duration.seconds(5))
            .position(Pos.TOP_RIGHT);
             notificationBuilder.showError(); 
            return false;
        }
        
        if(!checkdatewithtempsDate(date)){
            return false;
        }
        
        if (!checkInteger(dura)){
            return false;
        }
        
        return true;
    }

    static boolean checkTaskStatus(String status) {
        String[] taskStatus={"Pending","Ongoing","Completed","Verified"};
        for(int i = 0; i<4;i++){
            if(status.equals(taskStatus[i])){
                return true;
            }
        }
        Notifications notificationBuilder = Notifications.create()
        .title("Incorrect Values")
        .text("Status should be among Pending,Ongoing,Completed,Verified")
        .graphic(null)
        .hideAfter(Duration.seconds(5))
        .position(Pos.TOP_RIGHT);
         notificationBuilder.showError(); 
        return false;

    }
    
    static boolean checkDateStringFormat(String date){
        if(date.matches("[0-9]{4}-(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])") == false){
            Notifications notificationBuilder = Notifications.create()
            .title("Incorrect date format")
            .text("Format should be YYYY-MM-DD")
            .graphic(null)
            .hideAfter(Duration.seconds(5))
            .position(Pos.TOP_RIGHT);
             notificationBuilder.showError(); 
            return false;
        }
        return true;
    }

    static boolean checkExpenseType(String etype) {
        String[] expType={"Payment","Raw Materials","Miscellaneous"};
        for(int i = 0; i<3;i++){
            if(etype.equals(expType[i])){
                return true;
            }
        }
        Notifications notificationBuilder = Notifications.create()
        .title("Incorrect Values")
        .text("Status should be among Payment,Materials,Miscellaneous")
        .graphic(null)
        .hideAfter(Duration.seconds(5))
        .position(Pos.TOP_RIGHT);
         notificationBuilder.showError(); 
        return false;
    }

    static boolean checkNewExpense(LocalDate date, String sPName, String expType, String ExpDetails, String ExpAmount) throws SQLException {
        
//        String SPName = sPName.toString();
//        String ExpType = expType.toString();
        
        if(date.equals("") || sPName.equals("") || expType.equals("") || ExpDetails.equals("") || ExpAmount.equals("")){
            Notifications notificationBuilder = Notifications.create()
            .title("Missing Values")
            .text("Please fill in the missing values")
            .graphic(null)
            .hideAfter(Duration.seconds(5))
            .position(Pos.TOP_RIGHT);
             notificationBuilder.showError(); 
            return false;
        }
        
        if(!checkdatewithtempsDate(date)){
            return false;
        }
        
        if(!checkInteger(ExpAmount)){
            return false;
        }
        
        return true;
    }

    static boolean checkPaintLabel(String label, String numCoats, String spread, String price) {
        if(label.equals("") || numCoats.equals("") || spread.equals("") || price.equals("")){
            Notifications notificationBuilder = Notifications.create()
            .title("Missing Values")
            .text("Please fill in the missing values")
            .graphic(null)
            .hideAfter(Duration.seconds(5))
            .position(Pos.TOP_RIGHT);
             notificationBuilder.showError(); 
            return false;
        }
        
        if(!checkInteger(numCoats) || !checkInteger(spread) || !checkInteger(price)){
            return false;
        }
        
        return true;
    }
    
    static boolean checkEmptyString(String str){
        if(str.equals("")){
            Notifications notificationBuilder = Notifications.create()
            .title("Missing Values")
            .text("Please fill in the missing values")
            .graphic(null)
            .hideAfter(Duration.seconds(5))
            .position(Pos.TOP_RIGHT);
             notificationBuilder.showError(); 
            return false;
        }
        return true;
    }

    public static boolean checkSPProfileEdit(String name, String phone, String email, String addr) {
        if(name.equals("") || phone.equals("") || email.equals("") || addr.equals("")){
            Notifications notificationBuilder = Notifications.create()
            .title("Missing Values")
            .text("Please fill in the missing values")
            .graphic(null)
            .hideAfter(Duration.seconds(5))
            .position(Pos.TOP_RIGHT);
             notificationBuilder.showError(); 
            return false;
        }
        
        if(!checkPhone(phone)){
            return false;
        }
        
        return true;
    }

    static boolean checkSendQuotation(LocalDate sdate, String dur, String lcost, ObservableList<RMaterial> rmObsList, ObservableList<Task> taskObsList) {
        if(sdate.equals("") || dur.equals("") || lcost.equals("")){
            Notifications notificationBuilder = Notifications.create()
            .title("Missing Values")
            .text("Please fill in the missing values")
            .graphic(null)
            .hideAfter(Duration.seconds(5))
            .position(Pos.TOP_RIGHT);
             notificationBuilder.showError(); 
            return false;
        }
        
        if(!checkisEmptyObsList(rmObsList)){
            Notifications notificationBuilder = Notifications.create()
            .title("Missing Values")
            .text("Please add raw materials to the list")
            .graphic(null)
            .hideAfter(Duration.seconds(5))
            .position(Pos.TOP_RIGHT);
             notificationBuilder.showError(); 
            return false;
        }
        
        if(!checkisEmptyObsList(taskObsList)){
            Notifications notificationBuilder = Notifications.create()
            .title("Missing Values")
            .text("Please add tasks to the list")
            .graphic(null)
            .hideAfter(Duration.seconds(5))
            .position(Pos.TOP_RIGHT);
             notificationBuilder.showError(); 
            return false;
        }
        
        return true;
    }

    public static boolean checkSPQuoEdit(LocalDate date, String dur, String lcost) {
        if(date.equals("") || dur.equals("") || lcost.equals("")){
            Notifications notificationBuilder = Notifications.create()
            .title("Missing Values")
            .text("Please add tasks to the list")
            .graphic(null)
            .hideAfter(Duration.seconds(5))
            .position(Pos.TOP_RIGHT);
             notificationBuilder.showError(); 
            return false;
        }
        
        if(!checkInteger(dur)){
            return false;
        }
        
        if(!checkInteger(lcost)){
            return false;
        }
        
        return true;
    }

    public static boolean validateChangeTStatus(String oldValue, String newValue) {
        if(oldValue.equals("Pending") && newValue.equals("Completed")){
            Notifications notificationBuilder = Notifications.create()
            .title("Incorrect Values")
            .text("Task status cannot change from pending to completed")
            .graphic(null)
            .hideAfter(Duration.seconds(5))
            .position(Pos.TOP_RIGHT);
             notificationBuilder.showError(); 
            return false;
        }
        if(oldValue.equals("Ongoing") && newValue.equals("Pending")){
            Notifications notificationBuilder = Notifications.create()
            .title("Incorrect Values")
            .text("Task status cannot change from ongoing to pending")
            .graphic(null)
            .hideAfter(Duration.seconds(5))
            .position(Pos.TOP_RIGHT);
             notificationBuilder.showError(); 
            return false;
        }
        if(oldValue.equals("Completed") && newValue.equals("Pending")){
            Notifications notificationBuilder = Notifications.create()
            .title("Incorrect Values")
            .text("Task status cannot change from completed to pending")
            .graphic(null)
            .hideAfter(Duration.seconds(5))
            .position(Pos.TOP_RIGHT);
             notificationBuilder.showError(); 
            return false;
        }
        if(oldValue.equals("Verified")){
            Notifications notificationBuilder = Notifications.create()
            .title("Incorrect Values")
            .text("Verified task status cannot be changed")
            .graphic(null)
            .hideAfter(Duration.seconds(5))
            .position(Pos.TOP_RIGHT);
             notificationBuilder.showError(); 
            return false;
        }
        
        return true;
    }

    public static boolean validateUserChangeTStatus(String tdetails, String oldValue, String newValue) {
        if(tdetails.equals("Architect Verification")){
            return true;    
        }
        if(oldValue.equals("Pending") && newValue.equals("Verified")){
            Notifications notificationBuilder = Notifications.create()
            .title("Incorrect Values")
            .text("Task status cannot change from pending to verified")
            .graphic(null)
            .hideAfter(Duration.seconds(5))
            .position(Pos.TOP_RIGHT);
             notificationBuilder.showError(); 
            return false;
        }
        if(oldValue.equals("Ongoing") && newValue.equals("Verified")){
            Notifications notificationBuilder = Notifications.create()
            .title("Incorrect Values")
            .text("Task status cannot change from ongoing to verified")
            .graphic(null)
            .hideAfter(Duration.seconds(5))
            .position(Pos.TOP_RIGHT);
             notificationBuilder.showError(); 
            return false;
        }
        
        return true;
    }

    static boolean checkaddrm(String name, String price, String qty, Vector<RMaterial> rawmVec) {
        if(name.equals("") || price.equals("") || qty.equals("")){
            Notifications notificationBuilder = Notifications.create()
            .title("Missing Values")
            .text("Please fill in all the required details for raw material")
            .graphic(null)
            .hideAfter(Duration.seconds(5))
            .position(Pos.TOP_RIGHT);
             notificationBuilder.showError(); 
            return false;
        }
        
        if(!checkInteger(price)){
            return false;
        }
        
        if(!checkInteger(qty)){
            return false;
        }
        
        Iterator<RMaterial> iter = getRM(rawmVec).iterator();
        while(iter.hasNext()){
            RMaterial g = iter.next();
            if(g.getItemName().equals(name)){
                Notifications notificationBuilder = Notifications.create()
                .title("Incorrect Values")
                .text("Item name already exist in records.")
                .graphic(null)
                .hideAfter(Duration.seconds(5))
                .position(Pos.TOP_RIGHT);
                 notificationBuilder.showError(); 
                return false;
            }
        }
        
        return true;
    }
    
    public static ObservableList<RMaterial> getRM(Vector<RMaterial> rawmVec){
        ObservableList<RMaterial> rmaterial = FXCollections.observableArrayList(rawmVec);  
        return rmaterial;
    }
    
    static boolean checkaddrm(String name, String price, String qty, ObservableList<RMaterial> rawmVec) {
        if(name.equals("") || price.equals("") || qty.equals("")){
            Notifications notificationBuilder = Notifications.create()
            .title("Missing Values")
            .text("Please fill in all the required details for raw material")
            .graphic(null)
            .hideAfter(Duration.seconds(5))
            .position(Pos.TOP_RIGHT);
             notificationBuilder.showError(); 
            return false;
        }
        
        if(!checkInteger(price)){
            return false;
        }
        
        if(!checkInteger(qty)){
            return false;
        }
        
        Iterator<RMaterial> iter = rawmVec.iterator();
        while(iter.hasNext()){
            RMaterial g = iter.next();
            if(g.getItemName().equals(name)){
                Notifications notificationBuilder = Notifications.create()
                .title("Incorrect Values")
                .text("Item name already exist in records.")
                .graphic(null)
                .hideAfter(Duration.seconds(5))
                .position(Pos.TOP_RIGHT);
                 notificationBuilder.showError(); 
                return false;
            }
        }
        
        return true;
    }
}


