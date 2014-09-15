Flumeback
=========

A logback appender for Apache Flume, currently logs using HTTP/JSON.

Developed to configure our Play apps to log to our Flume setup.

How to Use
----------

Add (or append to) `conf/application-logger.xml`:

```xml
<appender name="FLUMEBACK" class="flumeback.FlumebackAppender" />
```

You can override the default settings within the appender.
Here is an example using its defaults:

```xml
<appender name="FLUMEBACK" class="flumeback.FlumebackAppender">
  <host>localhost</host>
  <port>16311</port>
  <timeout>1 second</timeout>
</appender>
```
