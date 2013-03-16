crostex
=======

Crossword puzzle compiler written in Java, intended to run as single JAR under JDK 1.6 or better.

To build, either load project in Eclipse and build and run us.stangl.crostex.Main, or use Apache Ant to build: "ant jar"

If you prefer, you can run the pre-built JAR file in this git repo, although it may not always be kept up-to-date.

To run:

* You need to have installed on your computer JDK (or JRE) 1.6 or higher
* Assuming the JDK's bin directory is included in your PATH environment variable (or else fully-qualifying the java command), type "java -jar crossword.jar"
* The first time crostex runs, it will ask you for a data directory. This is where it expects to read dictionaries and store its files, so set aside a directory for it under your home directory.