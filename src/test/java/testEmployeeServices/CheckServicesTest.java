package testEmployeeServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.apache.http.HttpStatus;
import resources.Employee;
import services.ConnectionHelper;

import java.io.IOException;

public class CheckServicesTest {

    ConnectionHelper connection;

    public void initConnection() {
        connection = new ConnectionHelper();
    }

    @BeforeClass(groups = {"functional"})
    public void freeResources() {
        initConnection();
        connection.deleteAllEmployees();
    }

    @Test(groups = {"functional"})
    public void checkServerUp() {
        int statusCode = connection.getStatusCode();
        Assert.assertEquals(statusCode, HttpStatus.SC_OK);
    }


    @Test(dependsOnMethods = {"checkServerUp"}, groups = {"functional"})
    public void addEmployeeUsingJson() throws IOException, JSONException {
        String newEmployeesInfoStr = connection.readJsonFile("Ibrahim");//Read Json File
        connection.addEmployee(newEmployeesInfoStr);//Pass json
        Assert.assertTrue(connection.isEmployeeExist(newEmployeesInfoStr), "employee doesn't exist");
    }

    @Test(dataProvider = "", dependsOnMethods = {"checkServerUp"})
    public void addEmployeeUsingObject() throws IOException, JSONException {
        Employee newEmployee = new Employee("Mohammed", "Nawahda", 12, 1500);
        String newEmployeeInfoStr = connection.modelToString(newEmployee);
        connection.addEmployee(newEmployeeInfoStr);
        Assert.assertTrue(connection.isEmployeeExist(newEmployeeInfoStr));
    }


    @Test(dependsOnMethods = {"checkServerUp"})
    public void addListOfEmployee() throws IOException, JSONException {
        String employeesListStr = connection.readJsonFile("employees");
        boolean isAllEmployeesAdded = connection.addListOfEmployees(employeesListStr);
        Assert.assertTrue(isAllEmployeesAdded);
    }

    @Test(dependsOnMethods = {"checkServerUp"})
    public void deleteEmployee() {
        int id=2;
        boolean isEmployeeDeleted = connection.deleteSpecificEmployee(id);
        Assert.assertTrue(isEmployeeDeleted);
        System.out.println("Employee with id "+id+" is deleted successfully.");
    }

    @Test(dependsOnMethods = {"checkServerUp"})
    public void getEmployeeBetweenRange() {
        boolean isAllEmployeeExist = connection.getEmployeeInSpecificRange(1000, 2500);
        Assert.assertTrue(isAllEmployeeExist);
    }

    @Test(dependsOnMethods = {"checkServerUp"})
    public void updateEmployeeInfo() throws IOException, JSONException {
        String newEmployeeInfoStr = connection.readJsonFile("newInfo");
        JSONArray newListInfo = new JSONArray(newEmployeeInfoStr);
        connection.updateEmployeesInfo(newListInfo);
        connection.verifyInfoUpdating(newListInfo);
    }
}
/*
naming
failure messages for asserstion
print statments
commit
share defects
*/
