----------------------------
HORSE RACING SIMULATOR
by Joshua Lewis
----------------------------

PROJECT SETUP INSTRUCTIONS
----------------------------

1. Install Java 21 or higher.
   - Download from the official Oracle website or OpenJDK.
   - Verify installation by running `java -version` in your terminal or command prompt. It should show version 21 or higher.
   - Also verify that the compiler has the correct version by running `javac -version`. This should also show 21 or higher.

2. Download or clone the project files to your computer.

3. Open a terminal or command prompt and navigate to the project folder using command `cd {your file path here}`.

DEPENDENCIES
----------------------------
- Java 21 or higher.
- No external libraries are needed. The program uses only standard Java.

RUNNING INSTRUCTIONS
----------------------------

To compile and run the textual version of the simulator:

1. type `cd part1` or navigate to the part one folder
2. type `javac *.java` to compile all files
3. type `java Race` to run the race - this will initialize the racers and call startRace()


To compile and run the graphical version of the simulator:

1. type `cd part2` or navigate to the part one folder
2. type `javac *.java` to compile all files
3. type `java GameManager` to run the race - this will initialize the racers and provide access to event driven buttons to call startRaceGUI()


USAGE NOTES
----------------------------

- No IDE (like IntelliJ or Eclipse) is required. The project can be fully compiled and run from the terminal.
- Always recompile if you make any changes to the Java files.
- If you encounter errors about Java versions, ensure you are using Java 21 or later for both compiling and running.
- The text-based version will simulate a horse race using console output.
- The graphical version will simulate the horse race using a basic GUI window.

----------------------------
GOOD LUCK AND HAVE FUN!
----------------------------