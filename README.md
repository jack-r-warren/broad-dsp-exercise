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
  challenge: Supply one of the below commands to see the associated output or
  pass -h to a command to see help

Options:
  -h, --help  Show this message and exit

Commands:
  q1  Perform the task for question 1: print out the long names of each subway
      line
  q2  Perform the task for question 2: print out the routes with the most and
      least stops and stops on multiple routes
  q3  Perform the task for question 3: given a source stop and a destination
      stop, determine what lines to getfrom the source to the destination
```

The only command to individually accept arguments is `q3`, which needs a source and destination stop to be given:

```
Usage: java -jar "Broad DSP Exercise.jar" q3 [OPTIONS]

  Perform the task for question 3: given a source stop and a destination stop,
  determine what lines to getfrom the source to the destination

Options:
  --source TEXT  The name of the stop to start from (case insensitive)
  --dest TEXT    The name of the stop to end at (case insensitive)
  -h, --help     Show this message and exit
```

## Overall Design Considerations
These comments are more general and aren't about any specific piece of code. Notable parts of the code have their own documentation, as does the command line interface.

- **API parameters abstracted up**: Despite having an API wrapper, I didn't want to bloat my code with reinventing the wheel. [Google has a spec for numeric mappings to route types](https://developers.google.com/transit/gtfs/reference#routestxt), so my code just works directly with those integers and documents it in the method comments.
- **Being selective with deserialization**: There's a lot of data provided in the endpoints--that informed my process in two ways. First, the classes I use for deserialization have only a subset of the fields that could be parsed from the data. Second, since even that more minimal structure could be difficult to work with, my tests use either a mock server or the real endpoint to do testing, instead of trying to craft complex scenarios myself that themselves might contain errors.
- **Kotlin as a language**: I used Kotlin simply because its what I'm most comfortable with. I think it improves on Java's syntax and type inference in key ways that further the benefit of the JVM's library maturity. 