import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Person {
    // fields
    private String lastName;
    private String firstName;
    private ArrayList<Course> courseMarks;
    private String id;
    private String email;

    private boolean isNumber(String s){
        boolean isNum = true;
        for(int i = 0; i < s.length(); i++){
            if(!Character.isDigit(s.charAt(i))){
                isNum = false;
                break;
            }
        }
        return isNum;
    }

    public Person(String nameID, String[] courses){
        String[] splitNameID = nameID.split(",");
        if(splitNameID.length < 3){
            System.out.println(splitNameID[0]);
        }
        this.lastName = splitNameID[1];
        this.firstName = splitNameID[2];
        this.id = splitNameID[0];
        if(splitNameID.length == 4) {
            this.email = splitNameID[3];
        }else{
            this.email = null;
        }

        this.courseMarks = Arrays.stream(courses)
                .map(s -> s.split(","))
                .map(a -> new Course(a[0], a[1]))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<Course> getCourseMarks() { return courseMarks; }

    public void setCourseMarks(ArrayList<Course> courseMarks) { this.courseMarks = courseMarks; }

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

    public String getLastName() { return lastName; }

    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getFirstName() { return firstName; }

    public void setFirstName(String firstName) { this.firstName = firstName; }
}
