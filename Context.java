
public class Context {
    private final static Context instance = new Context();
    
    public static Context getInstance(){
        return instance;
    }
    
    private Client cl = new Client();
    
    public Client currentClient(){
        return cl;
    }
    
    private CurrentSPDetails csp = new CurrentSPDetails();
    
    public CurrentSPDetails currentSPDetails(){
        return csp;
    }
}
