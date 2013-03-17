crostex
=======

Crossword puzzle compiler written in Java, intended to run as single JAR under JDK 1.6 or better.

To build, either load project in Eclipse and build and run us.stangl.crostex.Main, or use Apache Ant to build: "ant jar"

If you prefer, you can run the pre-built JAR file in this git repo, although it may not always be kept up-to-date.

Before running:

* You need to have installed on your computer JDK (or JRE) 1.6 or higher.
* Under your home directory, create a data directory for crostex to store its dictionaries, grid database, etc.
* Copy the *.TXT files from the data subdirectory in the crostex git repo into the data directory you created for crostex.

To run:
* Assuming the JDK's bin directory is included in your PATH environment variable (or else fully-qualifying the java command), type "java -jar crostex.jar"
* The first time you run crostex, it will ask you for the data directory. Point it to the directory you created for it. On subsequent runs it will remember where the directory is and no longer prompt you for it.
