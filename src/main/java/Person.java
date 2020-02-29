import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Person {
    // fields
    private String lastName;
    private String firstName;
    private ArrayList<Course> courseMarks;
    private String id;
    private String email;

    // Default constructor
    public Person(){}

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

    @Override
    public String toString(){
        System.out.println("do the la la la");
        String personString = this.getId() + "," + this.getFirstName() + "," +
                this.getLastName() + ((this.getEmail() == null) ? "" : "," + this.getEmail());
        String courseString = Stream.of(this.getCourseMarks())
                .map(AbstractCollection::toString)
                .collect(Collectors.joining(":"));
        System.out.println(courseString);
        return personString + ":" + courseString.substring(1, courseString.length() - 1);
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
