# Tub API


## Requirements
JDK 1.8

Maven: [Install maven] (https://maven.apache.org/install.html)

A Postgis database:[Postgis website](http://postgis.net/install/) 

You can use `apt-get install postgis`

Don't forget use `CREATE EXTENSION postgis;` on you database.

## Installation
Clone the [repository](https://github.com/jpaul51/SpringBootStarterProject.git)

Edit the src/main/resources/application.properties file to fit your settup.

Go to your project folder and run `mvn install`

A .jar file should be created in the target folder. Run it with `java -jar jarName`

When It's launched you need to initialize the database. Go to `localhost:8080/init` and you are ready to use Tub API once it finished loading.

Check the several services at `localhost:8080/`