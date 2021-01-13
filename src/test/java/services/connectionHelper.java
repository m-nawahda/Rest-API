package services;

import org.apache.http.HttpStatus;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import resources.Employee;
import serverInfo.ServerInformation;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class ConnectionHelper {
    private String relativePath = ".\\src\\test\\java\\resources\\";
    private String fullPath = "";
    private HttpURLConnection http;
    private URL url;
    private String urlStr;

    public ConnectionHelper() {
    }

    public void setFullPath(String path) {
        fullPath = relativePath + path + ".json";
    }

    public void setUpConnection(String extension) throws IOException {
        urlStr = ServerInformation.getServerFullUrl() + extension;
        url = new URL(urlStr);
        http = (HttpURLConnection) url.openConnection();
    }

    public void setUpConnection() throws IOException {
        urlStr = ServerInformation.getServerFullUrl();
        url = new URL(urlStr);
        http = (HttpURLConnection) url.openConnection();
    }

    public void initForUpdate(String methodType) throws IOException {
        http.setRequestMethod(methodType);
        http.setDoOutput(true);
        http.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        http.setRequestProperty("Accept", "application/json");
        http.connect();

    }

    public JSONObject readResponse() throws IOException, JSONException {
        InputStream in = http.getInputStream();
        int b;
        String result = "";
        while ((b = in.read()) != -1) {
            result += (char) b;
        }
        JSONObject jsonObject = new JSONObject(result);
        return jsonObject;
    }

    public String readJsonFile(String path) throws IOException {
        setFullPath(path);
        FileReader fr = new FileReader(fullPath);
        int c;
        String jsonStr = "";
        while ((c = fr.read()) != -1)
            jsonStr += (char) c;
        fr.close();
        return jsonStr;
    }

    public String modelToString(Employee employee) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            // convert user object to json string and return it
            return mapper.writeValueAsString(employee);
        }

        // catch various errors
        catch (JsonGenerationException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getStatusCode() {
        try {
            setUpConnection();
            int statusCode = http.getResponseCode();
            if(statusCode==HttpStatus.SC_OK)
                System.out.println("Server is Up.");
            else
                System.out.println("Server is Down.");
            return statusCode;

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            http.disconnect();
        }

        return 404;
    }

    public ArrayList<Employee> toEmployees(JSONArray jsonArray) throws JSONException {

        ArrayList<Employee> employees = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            Employee employee = new Employee(obj.getString("firstName"),
                    obj.getString("lastName"),
                    obj.getInt("id"),
                    obj.getInt("salary"));
            employees.add(employee);
        }
        return employees;

    }

    public ArrayList<Employee> getExistingEmployees() {
        try {
            setUpConnection();
            JSONObject fullResponse = readResponse();
            JSONArray employeesJson = fullResponse.getJSONArray("items");
            ArrayList<Employee> employees = toEmployees(employeesJson);
            return employees;
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            http.disconnect();
        }
        return null;
    }

    public JSONObject getAllEmployeeInfo() throws IOException, JSONException {
        //Http GET Request to retrieve all employees
        setUpConnection();
        return readResponse();
    }

    public boolean getEmployeeInSpecificRange(int min, int max) {
        try {
            setUpConnection(Integer.toString(min) + '/' + max);//Http GET Request to retrieve all employees inside specific range
            JSONObject allEmployeesInfoJson = readResponse();
            JSONArray employeesInfo = allEmployeesInfoJson.getJSONArray("items");
            for (int i = 0; i < employeesInfo.length(); i++) {
                String employeeInfoStr = employeesInfo.get(i).toString();
                if (!isEmployeeExist(employeeInfoStr)) {
                    http.disconnect();
                    return false;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            http.disconnect();
        }
        return true;

    }

    public Employee getEmployee(int id) {
        ArrayList<Employee> employees = getExistingEmployees();
        for (Employee obj : employees) {
            if (obj.getId() == id) {
                return obj;
            }
        }
        return null;

    }

    public boolean deleteSpecificEmployee(int id) {
        try {
            Assert.assertTrue(isEmployeeExist(id), "Sorry, Employee with ID " + id + " Doesn't Exist To Delete.");
            setUpConnection(Integer.toString(id));
            http.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");
            http.setRequestMethod("DELETE");
            String statusResponse = readResponse().getString("status");
            return statusResponse.equals("SUCCESS");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (http != null)
                http.disconnect();
        }
        return false;
    }

    public void deleteAllEmployees() {
        try {
            setUpConnection();
            System.out.println("Try to delete all employees");
            http.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");
            http.setRequestMethod("DELETE");
            JSONObject response = new JSONObject(readResponse().toString());
            verifyEmployeesDeleted(response);
            System.out.println("all employees is deleted.");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (http != null)
                http.disconnect();
        }

    }

    public void verifyEmployeesDeleted(JSONObject response) throws JSONException, IOException {
        Assert.assertEquals(response.getString("status"), "SUCCESS", "Sorry, can't delete all Employees.");
        int responseCode = http.getResponseCode();
        Assert.assertEquals(responseCode, HttpStatus.SC_OK, "Sorry, can't access to the server " + responseCode);
        JSONObject allInfo = getAllEmployeeInfo();
        Assert.assertTrue(allInfo.getInt("totalSalaries") == 0, "Total Salaries doesn't right.");
        Assert.assertTrue(allInfo.getInt("employeesCount") == 0, "Employee Count in main information fields doesn't right.");
        Assert.assertTrue(allInfo.getJSONArray("items").length() == 0, "items size doesn't right.");
    }

    public JSONObject getMainEmployeeInfo(int empSalary) throws IOException, JSONException {
        JSONObject mainEmployeeInfo = getAllEmployeeInfo();
        int oldTotalSalary = mainEmployeeInfo.getInt("totalSalaries");
        int oldEmployeeNo = mainEmployeeInfo.getInt("employeesCount");
        int oldMaxSalary = !mainEmployeeInfo.has("maxSalary") ? empSalary : mainEmployeeInfo.getInt("maxSalary");
        int oldMinSalary = !mainEmployeeInfo.has("minSalary") ? empSalary : mainEmployeeInfo.getInt("minSalary");
        JSONObject oldMainEmployeeInfo = new JSONObject("{\"maxSalary\": " + oldMaxSalary + ",\"totalSalaries\": " + oldTotalSalary + ",\"employeesCount\": " + oldEmployeeNo + ",\"minSalary\": " + oldMinSalary + "}");
        return oldMainEmployeeInfo;
    }

    public void addEmployee(String employeeInfoStr) throws IOException, JSONException {
        JSONObject newEmployeeInfo = new JSONObject(employeeInfoStr);
        JSONObject oldMainEmployeeInfo = getMainEmployeeInfo(newEmployeeInfo.getInt("salary"));
        setUpConnection(); //
        byte[] out = employeeInfoStr.getBytes(StandardCharsets.UTF_8);
        initForUpdate("POST");
        try (OutputStream os = http.getOutputStream()) {
            os.write(out);
            verifyEmployeeAdded(newEmployeeInfo);
            verifyMainInfo(oldMainEmployeeInfo,newEmployeeInfo);

        } finally {
            http.disconnect();
        }
    }


    public boolean addListOfEmployees(String newEmployeesInfoStr) throws JSONException, IOException {
        JSONArray jsonList = new JSONArray(newEmployeesInfoStr);
        for (int i = 0; i < jsonList.length(); i++) {
            String newEmployeeInfo = jsonList.get(i).toString();
            addEmployee(newEmployeeInfo);
            if (!isEmployeeExist(newEmployeeInfo)) return false;
        }
        return true;
    }
    public void verifyMainInfo(JSONObject oldMainEmployeeInfo,JSONObject employeeInfo) throws JSONException, IOException {
        JSONObject newMainEmployeeInfo = getMainEmployeeInfo(employeeInfo.getInt("salary"));
        int empSalary = employeeInfo.getInt("salary");
        // get Old information
        int oldMinSalary = oldMainEmployeeInfo.getInt("minSalary");
        int oldMaxSalary = oldMainEmployeeInfo.getInt("maxSalary");
        int oldTotalSalary = oldMainEmployeeInfo.getInt("totalSalaries");
        int oldCount = oldMainEmployeeInfo.getInt("employeesCount");
        // determine expected result
        int expectedCount = oldCount + 1;
        int expectedMinSalary = oldMinSalary >= empSalary ? empSalary : oldMinSalary;
        int expectedMaxSalary = oldMaxSalary <= empSalary ? empSalary : oldMaxSalary;
        int expectedTotalSalary = oldTotalSalary + empSalary;
        Assert.assertTrue(newMainEmployeeInfo.getInt("minSalary") == expectedMinSalary, "actual min salary = " + newMainEmployeeInfo.getInt("minSalary") + " , its must be equal = " + expectedMinSalary);
        Assert.assertTrue(newMainEmployeeInfo.getInt("maxSalary") == expectedMaxSalary, "actual max salary = " + newMainEmployeeInfo.getInt("maxSalary") + " , its must be equal = " + expectedMaxSalary);
        Assert.assertTrue(newMainEmployeeInfo.getInt("employeesCount") == expectedCount, "actual Number of employees = " + newMainEmployeeInfo.getInt("employeesCount") + " , its must be equal = " + expectedCount);
        Assert.assertTrue(newMainEmployeeInfo.getInt("totalSalaries") == expectedTotalSalary, "actual total salary = " + newMainEmployeeInfo.getInt("totalSalaries") + " , its must be equal =" + expectedTotalSalary);

    }
    public void verifyEmployeeAdded(JSONObject employeeInfo) throws IOException, JSONException {
        JSONObject allEmployeesInfo = readResponse();
        int responseCode = http.getResponseCode();
        Assert.assertEquals(responseCode, HttpStatus.SC_OK, "Sorry, can't access to the server " + responseCode);// verify statusCode if ok
        Assert.assertEquals(allEmployeesInfo.getString("status"), "SUCCESS", "Sorry, can't Add new Employee with " + employeeInfo.getInt("id") + ".");// verify status string if success
        Assert.assertNotEquals(allEmployeesInfo.getString("message"), "Employee already exists.", "Employee with id " + employeeInfo.getInt("id") + " already exists.");//verify if employee already doesn't exist
        System.out.println("New employee with id "+employeeInfo.getInt("id")+ " is added successfully");
       }


    public boolean isEmployeeExist(String employeeInfoStr) throws JSONException, IOException {
        JSONObject employeeInfoJson = new JSONObject(employeeInfoStr);
        ArrayList<Employee> employees = getExistingEmployees();
        Employee employee = new Employee(employeeInfoJson);
        for (Employee obj : employees) {
            if (obj.equals(employee)) {
                return true;
            }
        }
        return false;

    }

    public boolean isEmployeeExist(int id) {
        ArrayList<Employee> employees = getExistingEmployees();
        for (Employee obj : employees) {
            if (obj.getId() == id) {
                return true;
            }
        }
        return false;

    }

    public void updateEmployeesInfo(JSONArray newEmployeesInfo) throws IOException, JSONException {
        for (int i = 0; i < newEmployeesInfo.length(); i++) {
            JSONObject newInfo = newEmployeesInfo.getJSONObject(i);
            System.out.println("try to update employee with id "+newInfo.getInt("id"));

            JSONObject oldMainEmployeeInfo = getMainEmployeeInfo(0);
            int id = newInfo.getInt("id");
            setUpConnection(Integer.toString(id));
            initForUpdate("PUT");
            byte[] out = newInfo.toString().getBytes(StandardCharsets.UTF_8);
            try {
                OutputStream os = http.getOutputStream();
                os.write(out);
                JSONObject response = readResponse();
                verifyUpdateResponse(response, id);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                http.disconnect();
            }
        }
    }

    public void verifyUpdateResponse(JSONObject response, int id) throws JSONException, IOException {
        boolean isEmployeeExist = isEmployeeExist(id);
        boolean messageExist = response.getString("message").compareTo("Employee doesn't exist.") == 0;
        int responseCode = http.getResponseCode();
        Assert.assertEquals(responseCode, HttpStatus.SC_OK, "Sorry, can't access the server " + responseCode);// verify statusCode if ok
        Assert.assertTrue(isEmployeeExist != messageExist, "actually the employee with id " + id + " is exist.");//verify if employee doesn't exist
        Assert.assertEquals(response.getString("status"), "SUCCESS", "can't update in employee with id " + id + " info");// verify status string if success
        System.out.println("Employee with id "+id+" is updated successfully.");

    }

    public void verifyInfoUpdating(JSONArray newListInfo) throws JSONException {
        ArrayList<Employee> updatesEmployees = toEmployees(newListInfo);
        for (int i = 0; i < updatesEmployees.size(); i++) {
            Employee updateEmployee = updatesEmployees.get(i);
            Employee originalEmployee = getEmployee(updateEmployee.getId());
            Assert.assertTrue(updateEmployee.getFirstName().equals(originalEmployee.getFirstName()), "actual first name = " + updateEmployee.getFirstName() + " , its must be equal = " + originalEmployee.getFirstName());
            Assert.assertTrue(updateEmployee.getLastName().equals(originalEmployee.getLastName()), "actual lat name = " + updateEmployee.getLastName() + " , its must be equal = " + originalEmployee.getLastName());
            Assert.assertTrue(updateEmployee.getSalary() == originalEmployee.getSalary(), "actual salary = " + updateEmployee.getSalary() + " , its must be equal = " + originalEmployee.getSalary());


        }

    }

}
