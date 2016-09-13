#!/bin/bash

gradle -Dorg.gradle.java.home=${JAVA_HOME} fatJar
