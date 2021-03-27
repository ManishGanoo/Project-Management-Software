public class CurrentSPDetails {

    private int SID;
    private String QID;
    private String spType;
    
    public static CurrentSPDetails getInstance(){
        return instance;
    }
    
    private static CurrentSPDetails instance = new CurrentSPDetails();
    
    public void setSID(int sid){this.SID = sid;}
    public int getSID(){return SID;}
    
    public void setQID(String qid){this.QID = qid;}
    public String getQID(){return QID;}    
    
    public void setSpType(String type){this.spType = type;}
    public String getSpType(){return spType;}
}
