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

```
from ctypes import *
dll = CDLL("/home/vagrant/sources/geomvalidator-libnative/libgeomvalidator.so")
isolate = c_void_p()
isolatethread = c_void_p()
dll.graal_create_isolate(None, byref(isolate), byref(isolatethread))
dll.geomvalidator.restype = int

layer = QgsVectorLayer("/home/vagrant/sources/geomvalidator-libnative/data/ch.so.awjf.foerderprogramm_biodiversitaet.gpkg|layername=biotopflaechen_biotopflaeche", "biotopflaechen_biotopflaeche", "ogr")
if not layer.isValid():
    print ("failed to load layer")
    
for f in layer.getFeatures():
    result = dll.geomvalidator(isolatethread, c_char_p(bytes("biotopflaechen_biotopflaeche", "utf8")), c_char_p(bytes(str(f.id()), "utf8")), c_char_p(bytes(f.geometry().asWkt(), "utf8")))
    break
```





