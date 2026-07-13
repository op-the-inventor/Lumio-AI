#!/bin/bash
./gradlew app:dependencies --configuration debugRuntimeClasspath | grep -i ktor
