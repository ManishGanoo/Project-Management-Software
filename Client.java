public class Client {
    private int CID;
      

    public static Client getInstance(){
        return instance;
    }
    
    private static Client instance = new Client();
    
    public int getCID(){
        return CID;
    }
    
    public void setCID(int cid){
        this.CID = cid; 
    }

}
