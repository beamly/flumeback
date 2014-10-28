# Flumeback

A logback appender for Apache Flume, currently logs using HTTP/JSON.

Developed to configure our Play apps to log to our Flume setup.

[![Build Status](https://travis-ci.org/beamly/flumeback.svg?branch=master)](https://travis-ci.org/beamly/flumeback)

[Scaladoc](http://beamly.github.io/flumeback/latest/api)

## How to Use

#### Add flumeback to your build system.

For _sbt_:

```"com.beamly.flumeback" %% "flumeback" % "0.1.3"```

For _maven_:

```
<dependency>
  <groupId>com.beamly.flumeback</groupId>
  <artifactId>flumeback_${scala.binary}</artifactId>
  <version>0.1.3</version>
</dependency>
```
(where `scala.binary` is defined somewhere as `2.11` or `2.10`)

#### Configure logback

Add to `logback.xml` (`conf/application-logger.xml` for Play):

```xml
<appender name="FLUMEBACK" class="flumeback.FlumebackAppender" />
```

You can override the default settings within the appender. Here is an example
with its defaults:

```xml
<appender name="FLUMEBACK" class="flumeback.FlumebackAppender">
  <host>localhost</host>
  <port>16311</port>
</appender>
```

Dependencies
------------

* Scala 2.11.x or 2.10.x
* Logback 1.1.x
* Dispatch 0.11.x
