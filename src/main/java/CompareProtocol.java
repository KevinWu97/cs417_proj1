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

    public static long[] serializeToJSON(String path, String outputJson) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        // Index 0 is time
        // Index 1 is size
        // Index 2 is rate
        long[] messageProperties = new long[3];

        long startTime = System.currentTimeMillis();
        Stream<String> lines = Files.lines(Paths.get(path));
        ArrayList<Person> students = lines.map(l -> l.split(":"))
                .filter(s -> s[0].split(",").length >= 3)
                .map(s -> new Person(s[0], Arrays.copyOfRange(s, 1, s.length)))
                .collect(Collectors.toCollection(ArrayList::new));
        String studentJSON = objectMapper.writeValueAsString(students);

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        long messageSize = studentJSON.getBytes().length;

        messageProperties[0] = totalTime;
        messageProperties[1] = messageSize;
        messageProperties[2] = messageSize/totalTime;

        BufferedWriter bufferedWriter = Files.newBufferedWriter(Paths.get(outputJson));
        bufferedWriter.write(studentJSON);
        bufferedWriter.close();

        return messageProperties;
    }

    public static long[] deserializeFromJSON(String jsonFile, String outputFile) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayList<Person> students = objectMapper.readValue(new File(jsonFile),
                new TypeReference<ArrayList<Person>>(){});

        // Index 0 is time
        // Index 1 is size
        // Index 2 is rate
        long[] messageProperties = new long[3];

        long startTime = System.currentTimeMillis();
        StringBuilder personString = new StringBuilder();
        for(Person p : students){
            personString.append(p.toString()).append("\n");
        }
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        String outputString = personString.toString();
        long messageSize = outputString.getBytes().length;

        messageProperties[0] = totalTime;
        messageProperties[1] = messageSize;
        messageProperties[2] = messageSize/totalTime;

        BufferedWriter bufferedWriter = Files.newBufferedWriter(Paths.get(outputFile));
        bufferedWriter.write(outputString);
        bufferedWriter.close();
        return messageProperties;
    }

    public static long[] serializeWithProtobuf(String inputFile, String resultPath) throws IOException {

        FileOutputStream fileOutputStream = new FileOutputStream(resultPath);
        BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(inputFile));
        Stream<String> lines = bufferedReader.lines();

        // Index 0 is time
        // Index 1 is size
        // Index 2 is rate
        long[] messageProperties = new long[3];

        long startTime = System.currentTimeMillis();
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

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        long messageSize = result.toByteArray().length;

        messageProperties[0] = totalTime;
        messageProperties[1] = messageSize;
        messageProperties[2] = messageSize/totalTime;

        result.writeTo(fileOutputStream);
        return messageProperties;
    }

    public static long[] deserializeWithProtobuf(String protoFile, String recordPath) throws IOException {

        FileOutputStream fileOutputStream = new FileOutputStream(recordPath);
        FileInputStream fileInputStream = new FileInputStream(protoFile);

        // Index 0 is time
        // Index 1 is size
        // Index 2 is rate
        long[] messageProperties = new long[3];

        long startTime = System.currentTimeMillis();
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

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        long messageSize = studentInfoString.toString().getBytes().length;

        messageProperties[0] = totalTime;
        messageProperties[1] = messageSize;
        messageProperties[2] = messageSize/totalTime;

        fileOutputStream.write(studentInfoString.toString().getBytes());
        fileOutputStream.close();
        return messageProperties;
    }

    public static long[] getJsonMeasurements(String inputFile, String jsonFile, String outputFile) throws IOException {
        // Index 0 is serialization time
        // Index 1 is serialization size
        // Index 2 is serialization rate
        // Index 3 is deserialization time
        // Index 4 is deserialization size
        // Index 5 is deserialization rate
        long[] measurements = new long[6];

        long[] jsonSerializeProp = serializeToJSON(inputFile, jsonFile);
        long[] jsonDeserializeProp = deserializeFromJSON(jsonFile, outputFile);

        measurements[0] = jsonSerializeProp[0];
        measurements[1] = jsonSerializeProp[1];
        measurements[2] = jsonSerializeProp[2];
        measurements[3] = jsonDeserializeProp[0];
        measurements[4] = jsonDeserializeProp[1];
        measurements[5] = jsonDeserializeProp[2];
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

        long[] protoSerializeProp = serializeWithProtobuf(inputFile, protoFile);
        long[] protoDeserializeProp = deserializeWithProtobuf(protoFile, outputFile);

        measurements[0] = protoSerializeProp[0];
        measurements[1] = protoSerializeProp[1];
        measurements[2] = protoSerializeProp[2];
        measurements[3] = protoDeserializeProp[0];
        measurements[4] = protoDeserializeProp[1];
        measurements[5] = protoDeserializeProp[2];
        return measurements;
    }

    public static void main(String[] args) throws IOException {

        String inputFile = "C:\\Users\\Kevin Wu\\IdeaProjects\\cs417-project-1\\csv_files\\input.txt";

        String jsonFile = "C:\\Users\\Kevin Wu\\IdeaProjects\\cs417-project-1\\json_files\\result.json";
        String outJson = "C:\\Users\\Kevin Wu\\IdeaProjects\\cs417-project-1\\json_files\\output_json.txt";

        String protoFile = "C:\\Users\\Kevin Wu\\IdeaProjects\\cs417-project-1\\protobuf_text\\result_protobuf";
        String outProto = "C:\\Users\\Kevin Wu\\IdeaProjects\\cs417-project-1\\protobuf_text\\output_protobuf.txt";

        String[] units = {" ms", " bytes", " bytes/ms", " ms", " bytes", " bytes/ms"};

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

        for(int i = 0; i < jsonMeasurements.length; i++){
            System.out.println(prefixJson[i] + jsonMeasurements[i] + units[i]);
        }

        for(int i = 0; i < protoMeasurements.length; i++){
            System.out.println(prefixProto[i] + protoMeasurements[i] + units[i]);
        }

        /*
        if(args[0].equals("j")) {
            long[] jsonMeasurements = getJsonMeasurements(args[1], args[2], args[3]);
            String[] prefixJson = {"JSON Serialization Time is ",
                    "JSON Serialization Size is ",
                    "JSON Serialization Rate is ",
                    "JSON Deserialization Time is ",
                    "JSON Deserialization Size is ",
                    "JSON Deserialization Rate is "};

            for(int i = 0; i < jsonMeasurements.length; i++){
                System.out.println(prefixJson[i] + jsonMeasurements[i] + units[i]);
            }
        }else if(args[0].equals("p")) {
            long[] protoMeasurements = getProtobufMeasurements(args[1], args[2], args[3]);
            String[] prefixProto = {"Protobuf Serialization Time is ",
                    "Protobuf Serialization Size is ",
                    "Protobuf Serialization Rate is ",
                    "Protobuf Deserialization Time is ",
                    "Protobuf Deserialization Size is ",
                    "Protobuf Deserialization Rate is "};

            for(int i = 0; i < protoMeasurements.length; i++){
                System.out.println(prefixProto[i] + protoMeasurements[i] + units[i]);
            }
        }
         */
    }

}