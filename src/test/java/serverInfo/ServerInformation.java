package serverInfo;

public class ServerInformation {
    private static String employeeName = "ibraheem";
    private static String serverUrl = "http://192.168.200.91:8080/demo-server/employee-module/";
    private static String serverFullUrl = serverUrl + employeeName + '/';

    public static String getServerFullUrl() {
        return serverFullUrl;
    }
}
