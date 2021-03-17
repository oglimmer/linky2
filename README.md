# Linky

JVM based reimplementation of linky1.com

## Technologies

* Kotlin
* Spring Reactive Streams REST API
* Mongo
* Gradle
* Spring Security JWT Authentication
* MapStruct

## How to run

* `docker run --rm -p 27017:27017 mongo`
* `./gradlew bootRun`
* use `./test.sh create-user` to create a user
* use `./test.sh auth` to authenticate
* use `./test.sh create-links` to create a new link
* use `./test.sh get-links` to list all saved links
