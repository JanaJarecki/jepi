# Grading Server

The grading server is the backend of JEPI, the Java Exercise Plugin for Ilias. The server needs the Ilias-Plugin assProgQuestion to be of any use.

## Requirements

You need to have the following software installed to build and run the grading server:

- Gradle, a build tool for java.
- Java 8, as the server uses lambdas. Note that it does not need to be the systems default java. It only has to be configured and set in the bash you are starting the server.

## Simple Instructions

You need to have the requirements installed. Then follow the steps:

- Adapt __setup.sh__ to your needs.
- Setup your environment by executing __source setup.sh__
- Build the server running __build.sh__
- Start the server by calling __run.sh__ . This script takes an optional parameter, the port the sever listens on.

