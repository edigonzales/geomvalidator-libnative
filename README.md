# geomvalidator-libnative

## TODO
- Wie testet man die Methode?

## Build

macOS:
```
./gradlew clean lib:build shadowJar && \
native-image --no-fallback --no-server -cp lib/build/libs/lib-all.jar --shared -H:Name=libgeomvalidator
```



