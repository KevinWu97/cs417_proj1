import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Json {

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

    public static void main(String[] args) throws IOException {
        switch (args[0]) {
            case "-s":
                serializeToJSON(args[1], args[2]);
                break;
            case "-d":
                deserializeFromJSON(args[1], args[2]);
                break;
            case "-m":
                long[] jsonMeasurements = getJsonMeasurements(args[1], args[2], args[3]);
                String[] units = {" ms", " bytes", " bytes/ms", " ms", " bytes", " bytes/ms"};
                String[] prefixJson = {"Protobuf Serialization Time is ",
                        "Protobuf Serialization Size is ",
                        "Protobuf Serialization Rate is ",
                        "Protobuf Deserialization Time is ",
                        "Protobuf Deserialization Size is ",
                        "Protobuf Deserialization Rate is "};
                for (int i = 0; i < jsonMeasurements.length; i++) {
                    System.out.println(prefixJson[i] + jsonMeasurements[i] + units[i]);
                }
                break;
        }
    }

}
