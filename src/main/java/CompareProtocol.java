import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import proto.ProtocolDefn;
import proto.ProtocolDefn.CourseMarks;
import proto.ProtocolDefn.Student;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CompareProtocol {

    public static void serializeToJSON(String path, String outputJson){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        try(Stream<String> lines = Files.lines(Paths.get(path))){
            ArrayList<Person> students = lines.map(l -> l.split(":"))
                    .filter(s -> s[0].split(",").length >= 3)
                    .map(s -> new Person(s[0], Arrays.copyOfRange(s, 1, s.length)))
                    .collect(Collectors.toCollection(ArrayList::new));
            String studentJSON = objectMapper.writeValueAsString(students);

            try(FileWriter fileWriter = new FileWriter(outputJson)){
                fileWriter.write(studentJSON);
                fileWriter.flush();
            }catch (IOException e){
                e.printStackTrace();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public static void deserializeFromJSON(String jsonFile, String outputFile){
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            ArrayList<Person> students = objectMapper.readValue(new File(jsonFile),
                    new TypeReference<ArrayList<Person>>(){});
            StringBuilder personString = new StringBuilder();
            for(Person p : students){
                personString.append(p.toString()).append("\n");
            }

            try(BufferedWriter bufferedWriter = Files.newBufferedWriter(Paths.get(outputFile))){
                bufferedWriter.write(personString.toString());
            }catch (IOException e){
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void serializeWithProtobuf(String inputFile, String resultPath) throws IOException {

        FileOutputStream fileOutputStream = new FileOutputStream(resultPath);
        BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(inputFile));
        Stream<String> lines = bufferedReader.lines();

        ArrayList<Student> students = lines
                .map(s -> s.split(":"))
                .filter(s -> s[0].split(",").length >= 3)
                .map(s -> {
                    String[] personalInfoString = s[0].split(",");
                    Student.Builder studentBuilder = Student.newBuilder();
                    studentBuilder.setId(personalInfoString[0])
                            .setLastname(personalInfoString[1])
                            .setFirstname(personalInfoString[2])
                            .setEmail((personalInfoString.length > 3) ? personalInfoString[3] : "");

                    for(int i = 1; i < s.length; i++){
                        String[] courseInfoString = s[i].split(",");
                        studentBuilder.addMarks(
                                CourseMarks.newBuilder().setName(courseInfoString[0])
                                        .setScore(Integer.parseInt(courseInfoString[1])).build()
                        );
                    }
                    return studentBuilder.build();
                })
                .collect(Collectors.toCollection(ArrayList::new));

        ProtocolDefn.Result.Builder resultBuilder = ProtocolDefn.Result.newBuilder();
        ProtocolDefn.Result result = resultBuilder.addAllStudent(students).build();
        result.writeTo(fileOutputStream);
    }

    public static void deserializeWithProtobuf(String protoFile, String recordPath) throws IOException {

        FileOutputStream fileOutputStream = new FileOutputStream(recordPath);
        FileInputStream fileInputStream = new FileInputStream(protoFile);

        ProtocolDefn.Result res = ProtocolDefn.Result.parseFrom(fileInputStream);
        StringBuilder studentInfoString = new StringBuilder();
        for(Student s : res.getStudentList()){
            String personalInfoString = s.getId() + "," + s.getLastname() + "," + s.getFirstname() +
                    ((!s.getEmail().equals("")) ? "," + s.getEmail() : "");
            String courseInfoString = s.getMarksList().stream()
                    .map(c -> ":" + c.getName() + "," + c.getScore())
                    .collect(Collectors.joining());
            studentInfoString.append(personalInfoString).append(courseInfoString).append("\n");
        }
        fileOutputStream.write(studentInfoString.toString().getBytes());
        fileOutputStream.close();
    }

    public static void main(String[] args) throws IOException {
        String inputFile = "C:\\Users\\Kevin Wu\\IdeaProjects\\cs417-project-1\\csv_files\\input.txt";
        String jsonPath = "C:\\Users\\Kevin Wu\\IdeaProjects\\cs417-project-1\\json_files\\result_json.json";
        serializeToJSON(inputFile, jsonPath);

        String jsonFile = "C:\\Users\\Kevin Wu\\IdeaProjects\\cs417-project-1\\json_files\\result_json.json";
        String outputPath = "C:\\Users\\Kevin Wu\\IdeaProjects\\cs417-project-1\\json_files\\output_json.txt";
        deserializeFromJSON(jsonFile, outputPath);

        String resultPath = "C:\\Users\\Kevin Wu\\IdeaProjects\\cs417-project-1\\protobuf_text\\result_protobuf";
        serializeWithProtobuf(inputFile, resultPath);

        String protoFile = "C:\\Users\\Kevin Wu\\IdeaProjects\\cs417-project-1\\protobuf_text\\result_protobuf";
        String recordPath = "C:\\Users\\Kevin Wu\\IdeaProjects\\cs417-project-1\\protobuf_text\\output_protobuf.txt";
        deserializeWithProtobuf(protoFile, recordPath);
    }

}