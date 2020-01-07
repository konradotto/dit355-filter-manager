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

#### The following functionalities are available 
* Message validation (check that incoming data can be parsed into TravelRequests)
* Distance filtering: splits the TravelRequests into short\_trips and long\_trips
* Location filtering: checks whether incoming TravelRequests are in Gothenburg or not
* Origin clustering: cluster TravelRequests by their origin
* Destination clustering: cluster TravelRequests by their destination

#### The user can make the following decisions:
* Select the broker address
* Select one or multiple topics to subscribe to
* Turn filters on and off
* Initiate clustering
* Change clustering settings

## Format
The filtered messages will be sent out in the format of a JSON.

## Support

If you have any questions regarding this specific module,
please contact the lead developer of this module, [Tobias Bank](mailto:TobiasBanck90@gmail.com), 
or the co-developer and maintainer, [Konrad Otto](gusottko@student.gu.se).

## Authors and Acknowledgment

This module is part of the distributed system for Visual Transportation Support 
developed by Clusterrot (Group 9) during the course 
DIT355 Miniproject: Distributed Systems at the University of Gothenburg.  
The system was implemented from November 2019 through January 2020.  

Clusterrot consists of the following members:
- Tobias Bank
- Armin Ghoroghi
- Simon Johansson
- Kardo Marof
- Jean Paul Massoud
- Konrad Otto

### Documentation

* To access Diagrams and Documentaion please visit [Documentaion](https://git.chalmers.se/courses/dit355/2019/group-9/dit355-project-documentation)
