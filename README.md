# Broad DSP Engineering Interview Take-Home -- Jack Warren

This is my work for the Broad Institute's coding exercise [sourced from this document](https://drive.google.com/file/d/1mEc1jNHIbeUFhFDRqrJiBzX-9T9jpau3/view).

## Building
Building the program requires Java 13+. From the root of the project, run the following:

```
./gradlew copyJar
```

That command will test and compile all code, use the [`shadowJar`](https://imperceptiblethoughts.com/shadow/) Gradle plugin to create an executable `jar` with all dependencies, and copy that `jar` to the root of the project.

> Note: Depending on your OS and shell, you may have to alter your invocation of `gradlew`. There is a `gradlew` Linux shell file and a `gradlew.bat` Windows batch file in the project; use whatever is correct for your platform. 

## Running
Running the program requires Java 13+. After building, the executable `jar` can be run as follows:

```
java -jar "Broad DSP Exercise.jar"
```

The above command will print the usage information:

```
Usage: java -jar "Broad DSP Exercise.jar" [OPTIONS] COMMAND [ARGS]...

  An executable answering individual questions from the Broad Institute coding
  challenge: Supply one of the below commands to see the associated output

Options:
  -h, --help  Show this message and exit

Commands:
  q1  Perform the task for question 1: print out the long names of each subway
      line
```