# TOTO 2
Name: Anushree Sitaram Das

Implementation of distance-vector routing protocol - RIPv2. Each pod on
the network executes RIP to exchange routing information with its neighbors, and based on this
information, the pod computes the shortest paths from itself to all the other pods and the
container. 

To perform  route message broadcasts https://github.com/ProfFryer/MulticastTestingEnvironment by Professor Sam Fryer is referred

A Docker environment for testing networking Java programs

This uses the Docker OpenJDK container with added iptables to run Java applications.  A web interface is provided to dynamically block containers from talking with certain other containers as needed for testing. 

### To build
This will also build any java files in the current directory in the container.

`docker build -t containername . `

### To create the pod network
Only needs to be done once.

`docker network create --subnet=172.122.0.0/16 networkname `


### To Run (for example, pod 1)
This will ultimately run the java Toto2 class as an application.

`docker run -it -p 8080:8080 --cap-add=NET_ADMIN --net networkname --ip 172.122.0.21 containername 1 `

### To Run (pod 2):
`docker run -it -p 8081:8080 --cap-add=NET_ADMIN --net networkname --ip 172.122.0.22 containername 2 `

### To Block pod 2 and 3 on pod 1
Using the block=ip http query parameter.

`curl "http://localhost:8080/?block=172.122.0.22&block=172.122.0.23" `

### To unblock pod 2 on pod 1
Using the unblock=ip http query parameter.

`curl "http://localhost:8080/?unblock=172.18.0.22" `
