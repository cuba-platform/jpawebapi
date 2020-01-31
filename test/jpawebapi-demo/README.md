[![license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0)
[![Build Status](https://travis-ci.org/cuba-platform/restapi.svg?branch=master)](https://travis-ci.org/cuba-platform/restapi)

# CUBA REST API Demo

This is a Demo application with tests for the CUBA JPA WEB API Add-on.

## How to run tests

Let's assume that you have cloned CUBA JPA WEB API Add-on.

```

restapi/
    .github/
    config/
    gradle/
    modules/
    test/
        jpawebapi-demo/
```

Open terminal in the `jpawebapi` directory and run the following command to build and install the CUBA JPA WEB API Add-on into your local Maven repository (`~/.m2`):

```
gradlew install
```

After that, go to the CUBA JPA WEB API Demo directory and run the test database with the command:

```
cd test/jpawebapi-demo
gradlew startDb
```

Then in the same directory you can run the project and tests with the command:

```
gradlew prepareTest
gradlew test
```
Or:
```
gradlew funcTest 
```