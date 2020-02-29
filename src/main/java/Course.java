public class Course {
    // fields
    private String courseName;
    private String courseScore;

    // Default constructor
    public Course(){}

    public Course(String courseName, String courseScore){
        this.courseName = courseName;
        this.courseScore = courseScore;
    }

    @Override
    public String toString(){
        return this.getCourseName() + "," + this.getCourseScore();
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getCourseScore() { return courseScore; }

    public void setCourseScore(String courseScore) { this.courseScore = courseScore; }
}
