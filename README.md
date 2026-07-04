# scheduler-perf
Comparing persistent scheduling libs performance with load testing

Modes
===================
For JobRunr enabling jobrunr profile activating is needed, for example with ENV-s:
```
spring.profiles.active=jobrunr
```
For db-scheduler enabling use:
```
spring.profiles.active=db-scheduler
```
For special generic lock and fetch mode testing several profile activation is needed:
```
spring.profiles.active=db-scheduler,db-scheduler-generic
```


