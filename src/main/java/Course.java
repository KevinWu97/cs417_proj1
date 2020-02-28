public class Course {
    // fields
    private String courseName;
    private String courseScore;

    public Course(String courseName, String courseScore){
        this.courseName = courseName;
        this.courseScore = courseScore;
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
