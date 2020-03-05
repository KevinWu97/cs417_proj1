import proto.ProtocolDefn;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Proto {

    public static long[] serializeWithProtobuf(String inputFile, String resultPath) throws IOException {

        FileOutputStream fileOutputStream = new FileOutputStream(resultPath);
        BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(inputFile));
        Stream<String> lines = bufferedReader.lines();

        // Index 0 is time
        // Index 1 is size
        // Index 2 is rate
        long[] messageProperties = new long[3];

        long startTime = System.currentTimeMillis();
        ArrayList<ProtocolDefn.Student> students = lines
                .map(s -> s.split(":"))
                .filter(s -> s[0].split(",").length >= 3)
                .map(s -> {
                    String[] personalInfoString = s[0].split(",");
                    ProtocolDefn.Student.Builder studentBuilder = ProtocolDefn.Student.newBuilder();
                    studentBuilder.setId(personalInfoString[0])
                            .setLastname(personalInfoString[1])
                            .setFirstname(personalInfoString[2])
                            .setEmail((personalInfoString.length > 3) ? personalInfoString[3] : "");

                    for(int i = 1; i < s.length; i++){
                        String[] courseInfoString = s[i].split(",");
                        studentBuilder.addMarks(
                                ProtocolDefn.CourseMarks.newBuilder().setName(courseInfoString[0])
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
        for(ProtocolDefn.Student s : res.getStudentList()){
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
        switch (args[0]) {
            case "-s":
                serializeWithProtobuf(args[1], args[2]);
                break;
            case "-d":
                deserializeWithProtobuf(args[1], args[2]);
                break;
            case "-m":
                long[] protoMeasurements = getProtobufMeasurements(args[1], args[2], args[3]);
                String[] units = {" ms", " bytes", " bytes/ms", " ms", " bytes", " bytes/ms"};
                String[] prefixProto = {"Protobuf Serialization Time is ",
                        "Protobuf Serialization Size is ",
                        "Protobuf Serialization Rate is ",
                        "Protobuf Deserialization Time is ",
                        "Protobuf Deserialization Size is ",
                        "Protobuf Deserialization Rate is "};
                for (int i = 0; i < protoMeasurements.length; i++) {
                    System.out.println(prefixProto[i] + protoMeasurements[i] + units[i]);
                }
                break;
        }
    }

}
