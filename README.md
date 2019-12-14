# Filter manager

## prerequisite

* Installed Java JDK 8 or above
* Installed maven 3.5 or above
* mqtt broker (e.g. Mosquitto) installed, and running locally on default port 1883


#### Install Java JDK 8 or above
[Click here to download JDK](https://www.oracle.com/technetwork/java/javase/downloads/index.html)
_____

#### Install maven 3.5 or above
[Click here to download Maven](https://maven.apache.org/download.cgi)
_____


#### Install mosquitt (mqtt broker)

##### Windows
[Click here to download Mosquitto](https://mosquitto.org/download/)

##### Mac
`brew install mosquitto`

##### Linux
`snap install mosquitto`

##### Ubuntu

`sudo apt-add-repository ppa:mosquitto-dev/mosquitto-ppa`

`sudo apt-get update`
_____

# Functionality
##### The filter manager subscribes to a topic of the broker and runs it through the relevant filters 
* The filter is composed of a pump, pipe, filters and sinks
* The pump, pumps the messages to the pipes from the publisher.
* The pipes transport these messages through the filters. 
* The filters transform the message and spits it back out to the pipes.
* The pipe transports it to additional filters if the subscriber want additonal filerting.
* Once the message is filtered enough to the messages the subscriber wanted, the pump sends the message out through the sink.


## Format
The message will be sent out in the format of a JSON.
