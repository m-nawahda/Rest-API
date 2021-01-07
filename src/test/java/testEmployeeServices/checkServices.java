package testEmployeeServices;
import org.apache.http.HttpStatus;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import services.connectionHelper;
import java.io.IOException;
    
public class checkServices {
    @Test
    public void checkServerUp() throws IOException {
        int statusCode = connectionHelper.getStatusCode();
        assertEquals(statusCode, HttpStatus.SC_OK);
    }

}