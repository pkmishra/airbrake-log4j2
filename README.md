airbrake-log4j2
================
> Airbrake has also published offcial client which can be found [here](https://github.com/airbrake/log4javabrake2)

[![Build Status](https://travis-ci.org/pkmishra/airbrake-log4j2.svg?branch=master)](https://travis-ci.org/pkmishra/airbrake-log4j2)
[![codecov](https://codecov.io/gh/pkmishra/airbrake-log4j2/branch/master/graph/badge.svg)](https://codecov.io/gh/pkmishra/airbrake-log4j2)
log4j2 Appender for Airbrake

Built on the top of the official [airbrake.io library](https://github.com/airbrake/airbrake-java) 

```

appender.airbrake.type = AirbrakeLog4j2Appender
appender.airbrake.name = Airbrake
appender.airbrake.enabled = true
appender.airbrake.async = true
appender.airbrake.apiKey = <YOUR_KEY> 
appender.airbrake.env = <Environment>


rootLogger.level = info
rootLogger.appenderRef.airbrake.ref= Airbrake
rootLogger.appenderRef.airbrake.level= Error
```

Java code
```
Logger logger = LoggerFactory.getLogger(getClass());
IllegalArgumentException exception = new IllegalArgumentException("This is exception message");
logger.error("Error logged to Airbrake", exception);
```

