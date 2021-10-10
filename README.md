# geomvalidator-libnative

## TODO
- Wie testet man die Methode?

## Build

macOS:

```
./gradlew clean lib:build shadowJar && \
native-image --no-fallback --no-server -cp lib/build/libs/lib-all.jar --shared -H:Name=libgeomvalidator
cc -Wall -I. -L. -lgeomvalidator geomvalidator.c -o geomvalidator
```

Linux:

```
sudo apt-get install build-essential
sudo apt install libstdc++-8-dev
```

```
./gradlew clean lib:build shadowJar && \
native-image --no-fallback --no-server -cp lib/build/libs/lib-all.jar --shared -H:Name=libgeomvalidator && \
cc  geomvalidator.c -I. -L. -lgeomvalidator -o geomvalidator
```

```
export LD_LIBRARY_PATH=.:$LD_LIBRARY_PATH && ./geomvalidator
```

QGIS:

```
export LD_LIBRARY_PATH=.:$LD_LIBRARY_PATH && qgis
```

Siehe `validate_data.py`.





