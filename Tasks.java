import javafx.beans.property.SimpleStringProperty;

public class Tasks {
    private final SimpleStringProperty taskID;
    private final SimpleStringProperty taskName;
    private final SimpleStringProperty taskDetails;
    private final SimpleStringProperty taskDate;
    private final SimpleStringProperty taskDuration;
    private final SimpleStringProperty taskStatus;
    
    public Tasks(String taskid, String tname, String tdetails,String date, String tduration, String status) {
        taskID = new SimpleStringProperty(taskid);
        taskName = new SimpleStringProperty(tname);
        taskDetails = new SimpleStringProperty(tdetails);
        taskDate = new SimpleStringProperty(date);
        taskDuration =  new SimpleStringProperty(tduration);
        taskStatus = new SimpleStringProperty(status);
    }
    
    public String getTaskID() {return taskID.get();}
    public String getTaskName() {return taskName.get();}
    public String getTaskDetails() {return taskDetails.get();}
    public String getTaskDuration() {return taskDuration.get();}
    public String getTaskDate() {return taskDate.get();}
    public String getTaskStatus() {return taskStatus.get();}
    
    public void setTaskID(String id){taskID.set(id);}
    public void setTaskName(String tname){taskName.set(tname);}
    public void setTaskDetails(String tdetails) {taskDetails.set(tdetails);}    
    public void setTaskDuration(String tduration){taskDuration.set(tduration);}
    public void setTaskDate(String Date){taskDate.set(Date);} 
    public void setTaskStatus(String status){taskStatus.set(status);} 
    
    public String toString(){
        return new StringBuffer("Task id").append(getTaskID()).append("Task name: ").append(getTaskName()).append("\t Details: ").append(getTaskDetails()).append("\t Duration: ").append(getTaskDuration()).append("\t Date: ").append(getTaskDate()).append("\t Status: ").append(getTaskStatus()).toString();
    }
}
