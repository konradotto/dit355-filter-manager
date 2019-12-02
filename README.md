# Prerequirements

- installed Java JDK 8 or above
- installed maven 3.5 or above
- mqtt broker (e.g. Mosquitto) installed, and running locally on default port 1883

# Install

- checkout repository to your machine
- open a terminal, traverse to the main root of the repository, and run ```mvn clean install```
- after successful install, the jar file for execution is called alchemist.jar and can be found in the target directory
- traverse to the target directory and execute ```java -jar alchemist.jar``` in order to start listening to the mqtt broker and placing gold on the public square (publishing gold message on topic 'square').

# Comments

- when exeucting the jar file, some warning messages may show up. They are nothing to worry about.
