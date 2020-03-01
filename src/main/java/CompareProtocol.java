import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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

    public static void serializeToJSON(String path){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        try(Stream<String> lines = Files.lines(Paths.get(path))){
            ArrayList<Person> students = lines.map(l -> l.split(":"))
                    .filter(s -> s[0].split(",").length >= 3)
                    .map(s -> new Person(s[0], Arrays.copyOfRange(s, 1, s.length)))
                    .collect(Collectors.toCollection(ArrayList::new));
            String studentJSON = objectMapper.writeValueAsString(students);

            try(FileWriter fileWriter = new FileWriter(
                    "C:\\Users\\Kevin Wu\\IdeaProjects\\cs417-project-1\\json_files\\students.json")){
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

    public static void serializeWithProtobuf(String inputFile) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream("result_protobuf.txt");
        BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(inputFile));

        Stream<String> lines = bufferedReader.lines();
        Student.Builder studentBuilder = Student.newBuilder();
        CourseMarks.Builder courseBuilder = CourseMarks.newBuilder();

        ArrayList<byte[]> students = lines
                .map(s -> s.split(":"))
                .filter(s -> s[0].split(",").length >= 3)
                .map(s -> {
                    String[] personalInfoString = s[0].split(",");
                    studentBuilder.setId(personalInfoString[0])
                            .setLastname(personalInfoString[1])
                            .setFirstname(personalInfoString[2])
                            .setEmail((personalInfoString.length > 3) ? personalInfoString[3] : null);
                    for(int i = 1; i < s.length; i++){
                        String[] courseInfoString = s[i].split(",");
                        studentBuilder.addMarks(
                                courseBuilder.setName(courseInfoString[0])
                                        .setScore(Integer.parseInt(courseInfoString[1]))
                                        .build()
                        );
                    }
                    return studentBuilder.build().toByteArray();
                })
                .collect(Collectors.toCollection(ArrayList::new));

        for(byte[] s : students){
            fileOutputStream.write(s);
        }
        fileOutputStream.close();
    }

    public static void deserializeWithProtobuf(){

    }

    public static void main(String[] args) throws IOException {
        /*
        String csvPath = "C:\\Users\\Kevin Wu\\IdeaProjects\\cs417-project-1\\csv_files\\input_v2.csv";
        serializeToJSON(csvPath);

        String jsonPath = "C:\\Users\\Kevin Wu\\IdeaProjects\\cs417-project-1\\json_files\\students.json";
        String csvWritePath = "C:\\Users\\Kevin Wu\\IdeaProjects\\cs417-project-1\\csv_files\\deserialized.csv";
        deserializeFromJSON(jsonPath, csvWritePath);
         */

        serializeWithProtobuf("C:\\Users\\Kevin Wu\\IdeaProjects\\cs417-project-1\\csv_files\\input_v2.csv");
    }

}