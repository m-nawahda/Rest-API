package services;

import serverInfo.ServerInformation;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class connectionHelper {
    public static int getStatusCode() throws IOException {
        ServerInformation info=new ServerInformation("mohammed");
        String urlStr=info.getServerFullURL();
        URL url = new URL(urlStr);
        HttpURLConnection http = (HttpURLConnection)url.openConnection();
        int statusCode=http.getResponseCode();
        return statusCode;
    }
}
