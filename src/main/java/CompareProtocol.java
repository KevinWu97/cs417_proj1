import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

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
                    .map(s -> new Person(s[0], Arrays.copyOfRange(s, 1, s.length - 1)))
                    .collect(Collectors.toCollection(ArrayList::new));
            String studentJSON = objectMapper.writeValueAsString(students);

            try(FileWriter fileWriter = new FileWriter(
                    "C:\\Users\\Kevin Wu\\IdeaProjects\\cs417-project-1\\json_files\\students.json")){
                fileWriter.write(studentJSON);
                fileWriter.flush();
            }catch (IOException e){
                e.printStackTrace();
            }
            // System.out.print(studentJSON);
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
            // System.out.print(personString.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        String csvPath = "C:\\Users\\Kevin Wu\\IdeaProjects\\cs417-project-1\\csv_files\\input_v2.csv";
        serializeToJSON(csvPath);

        String jsonPath = "C:\\Users\\Kevin Wu\\IdeaProjects\\cs417-project-1\\json_files\\students.json";
        String csvWritePath = "C:\\Users\\Kevin Wu\\IdeaProjects\\cs417-project-1\\csv_files\\deserialized.csv";
        deserializeFromJSON(jsonPath, csvWritePath);
    }

}
