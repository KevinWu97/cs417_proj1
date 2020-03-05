#!/bin/bash

classPath="jar_files/jackson-annotations-2.10.2:jar_files/jackson-core-2.10.2:jar_files/jackson-databind-2.10.2:jar_files/protobuf-java-3.11.4"

if [ $1 = '-c' ]
then
    ##Compile the code
    echo "Compiling ..."
    javac -sourcepath "src" -cp $classPath src/main/Json.java
    javac -sourcepath "src" -cp $classPath src/main/Proto.java
elif [ $1 = '-s' ]
then
    if [ $2 = '-p' ]
    then
        ###Serialize Protobuf
        echo "Serializing Protobuf ..."
        java -cp "$classPath:src" main/Proto "-s" $3 "result_protobuf"
        echo "Protobuf Serialization Successful"
    elif [ $2 = '-j' ]
    then
        ##Serialize JSON
        echo "Serializing JSON ..."
        java -cp "$classPath:src" main/Json "-s" $3 "result.json"
        echo "JSON Serialization Successful"
    fi
elif [ $1 = '-d' ]
then
    if [ $2 = '-j' ]
    then
        ##Deserialize JSON
        echo "Deserializing JSON ..."
        java -cp "$classPath:src" main/Json "-d" $3 "output_json.txt"
        echo "JSON Deserialization Successful"
    elif [ $2 = '-p' ]
    then
        ##Deserialize Protobuf
        echo "Deserializing Protobuf ..."
        java -cp "$classPath:src" main/Proto "-d" $3 "output_protobuf.txt"
        echo "Protobuf Deserialization Successful"
    fi
elif [ $1 = '-t' ]
then
    if [ $2 = '-j' ]
    then
        ##Metric measurment JSON
        echo "Getting Measurements for JSON ..."
        java -cp "$classPath:src" main/Json "-m" $3 "result.json" "output_json.txt"
        echo "Finished Getting Metrics for JSON"
    elif [ $2 = '-p' ]
    then
        ##Metric measurment protobuf
        echo "Getting Measurements for Protobuf ..."
        java -cp "$classPath:src" main/Proto "-m" $3 "result_protobuf" "output_protobuf.txt"
        echo "Finished Getting Metrics for Protobuf"
    fi
fi
