import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Person {
    // fields
    private String name;
    private ArrayList<Course> courseMarks;
    private String rollNumber;

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
        if(splitNameID.length == 0){
            this.name = null;
            this.rollNumber = null;
        }else if(splitNameID.length == 1){
            if(isNumber(splitNameID[0])){
                this.name = null;
                this.rollNumber = splitNameID[0];
            }else{
                this.name = splitNameID[0];
                this.rollNumber = null;
            }
        }else{
            this.name = splitNameID[0];
            this.rollNumber = splitNameID[1];
        }

        this.courseMarks = Arrays.stream(courses)
                .map(s -> s.split(","))
                .map(a -> new Course(a[0], a[1]))
                .collect(Collectors.toCollection(ArrayList::new));
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }

    public ArrayList<Course> getCourseMarks() { return courseMarks; }

    public void setCourseMarks(ArrayList<Course> courseMarks) { this.courseMarks = courseMarks; }
}
