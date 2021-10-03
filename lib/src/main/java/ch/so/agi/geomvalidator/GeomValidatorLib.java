package ch.so.agi.geomvalidator;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import ch.interlis.iom.IomObject;
import ch.interlis.iox_j.jts.Jts2iox;
//import ch.interlis.ili2c.metamodel.SurfaceOrAreaType;

public class GeomValidatorLib {
    public static int validate(String wktGeom) {
        try {
            WKTReader wktReader = new WKTReader();
            Geometry jtsGeom = wktReader.read(wktGeom);
            
            IomObject geom = null;
            if (jtsGeom instanceof Point) {
                System.out.println("not yet supported");
                return 0;
            } else if (jtsGeom instanceof LineString) {
                System.out.println("not yet supported");
                return 0;   
            } else if (jtsGeom instanceof MultiLineString) {
                System.out.println("not yet supported");
                return 0;   
            } else if (jtsGeom instanceof Polygon) {
                geom = Jts2iox.JTS2surface((Polygon)jtsGeom);
            } else if (jtsGeom instanceof MultiPolygon) {
                geom = Jts2iox.JTS2multisurface((MultiPolygon)jtsGeom);
                validatePolygon(geom);
                
            }
            
            
            
            
            System.out.println(geom);
            
            
        } catch (ParseException e) {
            e.printStackTrace();
            return 1;
        }
        
        return 0;
    }
    
    private static boolean validatePolygon(IomObject surfaceValue) {
        return false;
    }
}
