package serverInfo;

public class ServerInformation {
    String employeeName;
    String serverURL = "http://192.168.200.91:8080/demo-server/employee-module/";
    String serverFullURL = serverURL+employeeName;
    public ServerInformation(String employeeName){
        this.employeeName=employeeName;
    }
    public String getServerFullURL() {
        return serverFullURL;
    }
}
