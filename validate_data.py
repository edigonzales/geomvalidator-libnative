from ctypes import *
dll = CDLL("/home/vagrant/sources/geomvalidator-libnative/libgeomvalidator.so")
#dll = CDLL("/Users/stefan/sources/geomvalidator-libnative/libgeomvalidator.dylib")
isolate = c_void_p()
isolatethread = c_void_p()
dll.graal_create_isolate(None, byref(isolate), byref(isolatethread))
dll.geomvalidator.restype = int

layer = QgsVectorLayer("/home/vagrant/sources/geomvalidator-libnative/data/ch.so.awjf.foerderprogramm_biodiversitaet.gpkg|layername=biotopflaechen_biotopflaeche", "biotopflaechen_biotopflaeche", "ogr")
#layer = QgsVectorLayer("/Users/stefan/sources/geomvalidator-libnative/data/ch.so.awjf.foerderprogramm_biodiversitaet.gpkg|layername=biotopflaechen_biotopflaeche", "biotopflaechen_biotopflaeche", "ogr")
if not layer.isValid():
    print ("failed to load layer")
    
for f in layer.getFeatures():
    result = dll.geomvalidator(isolatethread, c_char_p(bytes("biotopflaechen_biotopflaeche", "utf8")), c_char_p(bytes(str(f.id()), "utf8")), c_char_p(bytes(f.geometry().asWkt(), "utf8")))
    break


