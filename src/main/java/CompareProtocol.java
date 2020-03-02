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

    public static long serializeToJSON(String path, String outputJson){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        long messageSize = 0;
        try(Stream<String> lines = Files.lines(Paths.get(path))){
            ArrayList<Person> students = lines.map(l -> l.split(":"))
                    .filter(s -> s[0].split(",").length >= 3)
                    .map(s -> new Person(s[0], Arrays.copyOfRange(s, 1, s.length)))
                    .collect(Collectors.toCollection(ArrayList::new));
            String studentJSON = objectMapper.writeValueAsString(students);
            messageSize = studentJSON.getBytes().length;

            try(FileWriter fileWriter = new FileWriter(outputJson)){
                fileWriter.write(studentJSON);
                fileWriter.flush();
            }catch (IOException e){
                e.printStackTrace();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
        return messageSize;
    }

    public static long deserializeFromJSON(String jsonFile, String outputFile){
        ObjectMapper objectMapper = new ObjectMapper();
        long messageSize = 0;
        try {
            ArrayList<Person> students = objectMapper.readValue(new File(jsonFile),
                    new TypeReference<ArrayList<Person>>(){});
            StringBuilder personString = new StringBuilder();
            for(Person p : students){
                personString.append(p.toString()).append("\n");
            }

            String outputString = personString.toString();
            messageSize = outputString.getBytes().length;

            try(BufferedWriter bufferedWriter = Files.newBufferedWriter(Paths.get(outputFile))){
                bufferedWriter.write(outputString);
            }catch (IOException e){
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return messageSize;
    }

    public static long serializeWithProtobuf(String inputFile, String resultPath) throws IOException {

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
        long messageSize = result.toByteArray().length;
        result.writeTo(fileOutputStream);
        return messageSize;
    }

    public static long deserializeWithProtobuf(String protoFile, String recordPath) throws IOException {

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
        long messageSize = studentInfoString.toString().getBytes().length;
        fileOutputStream.write(studentInfoString.toString().getBytes());
        fileOutputStream.close();
        return messageSize;
    }

    public static long[] getJsonMeasurements(String inputFile, String jsonFile, String outputFile){
        // Index 0 is serialization time
        // Index 1 is serialization size
        // Index 2 is serialization rate
        // Index 3 is deserialization time
        // Index 4 is deserialization size
        // Index 5 is deserialization rate
        long[] measurements = new long[6];

        long serializeStartTime = System.currentTimeMillis();
        long serializeSize = serializeToJSON(inputFile, jsonFile);
        long serializeEndTime = System.currentTimeMillis();

        long deserializeStartTime = System.currentTimeMillis();
        long deserializeSize = deserializeFromJSON(jsonFile, outputFile);
        long deserializeEndTime = System.currentTimeMillis();

        measurements[0] = serializeEndTime - serializeStartTime;
        measurements[1] = serializeSize;
        measurements[2] = measurements[1]/measurements[0];
        measurements[3] = deserializeEndTime - deserializeStartTime;
        measurements[4] = deserializeSize;
        measurements[5] = measurements[4]/measurements[3];
        return measurements;
    }

    public static long[] getProtobufMeasurements(String inputFile, String protoFile, String outputFile) throws IOException {
        // Index 0 is serialization time
        // Index 1 is serialization size
        // Index 2 is serialization rate
        // Index 3 is deserialization time
        // Index 4 is deserialization size
        // Index 5 is deserialization rate
        long[] measurements = new long[6];

        long serializeStartTime = System.currentTimeMillis();
        long serializeSize = serializeWithProtobuf(inputFile, protoFile);
        long serializeEndTime = System.currentTimeMillis();

        long deserializeStartTime = System.currentTimeMillis();
        long deserializeSize = deserializeWithProtobuf(protoFile, outputFile);
        long deserializeEndTime = System.currentTimeMillis();

        measurements[0] = serializeEndTime - serializeStartTime;
        measurements[1] = serializeSize;
        measurements[2] = measurements[1]/measurements[0];
        measurements[3] = deserializeEndTime - deserializeStartTime;
        measurements[4] = deserializeSize;
        measurements[5] = measurements[4]/measurements[3];
        return measurements;
    }

    public static void main(String[] args) throws IOException {
        String inputFile = "C:\\Users\\Kevin Wu\\IdeaProjects\\cs417-project-1\\csv_files\\input.txt";

        String jsonFile = "C:\\Users\\Kevin Wu\\IdeaProjects\\cs417-project-1\\json_files\\result.json";
        String outJson = "C:\\Users\\Kevin Wu\\IdeaProjects\\cs417-project-1\\json_files\\output_json.txt";

        String protoFile = "C:\\Users\\Kevin Wu\\IdeaProjects\\cs417-project-1\\protobuf_text\\result_protobuf";
        String outProto = "C:\\Users\\Kevin Wu\\IdeaProjects\\cs417-project-1\\protobuf_text\\output_protobuf.txt";

        long[] jsonMeasurements = getJsonMeasurements(inputFile, jsonFile, outJson);
        long[] protoMeasurements = getProtobufMeasurements(inputFile, protoFile, outProto);
        String[] prefixJson = {"JSON Serialization Time is ",
                "JSON Serialization Size is ",
                "JSON Serialization Rate is ",
                "JSON Deserialization Time is ",
                "JSON Deserialization Size is ",
                "JSON Deserialization Rate is "};
        String[] prefixProto = {"Protobuf Serialization Time is ",
                "Protobuf Serialization Size is ",
                "Protobuf Serialization Rate is ",
                "Protobuf Deserialization Time is ",
                "Protobuf Deserialization Size is ",
                "Protobuf Deserialization Rate is "};
        String[] units = {" ms", " bytes", " bytes/ms", " ms", " bytes", " bytes/ms"};

        for(int i = 0; i < jsonMeasurements.length; i++){
            System.out.println(prefixJson[i] + jsonMeasurements[i] + units[i]);
        }

        for(int i = 0; i < protoMeasurements.length; i++){
            System.out.println(prefixProto[i] + protoMeasurements[i] + units[i]);
        }
    }

}