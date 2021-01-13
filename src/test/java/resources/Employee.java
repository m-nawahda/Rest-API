package resources;

import org.json.JSONException;
import org.json.JSONObject;

public class Employee {
    public String firstName, lastName;
    public int id, salary;

    public String getFirstName() {
        return firstName;
    }

    public int getId() {
        return id;
    }

    public int getSalary() {
        return salary;
    }

    public String getLastName() {
        return lastName;
    }

    public Employee(String firstName, String lastName, int id, int salary) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.id = id;
        this.salary = salary;
    }

    public Employee(JSONObject jsonObject) throws JSONException {
        this.firstName = jsonObject.getString("firstName");
        this.lastName = jsonObject.getString("lastName");
        this.id = jsonObject.getInt("id");
        this.salary = jsonObject.getInt("salary");
    }

    @Override
    public boolean equals(Object o) {

        // If the object is compared with itself then return true
        if (o == this) {
            return true;
        }

        /* Check if o is an instance of Complex or not
          "null instanceof [type]" also returns false */
        if (!(o instanceof Employee)) {
            return false;
        }

        // typecast o to Complex so that we can compare data members
        Employee e = (Employee) o;

        // Compare the data members and return accordingly
        return id == e.id
                && salary == e.salary
                && firstName.compareTo(e.firstName) == 0
                && lastName.compareTo(e.lastName) == 0;
    }
}
