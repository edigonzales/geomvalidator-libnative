package ch.so.agi.geomvalidator;

import java.util.HashSet;
import java.util.ResourceBundle;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import ch.interlis.ili2c.metamodel.LineForm;
import ch.interlis.ili2c.metamodel.SurfaceType;
import ch.interlis.iom.IomConstants;
import ch.interlis.iom.IomObject;
import ch.interlis.iox.IoxException;
import ch.interlis.iox_j.jts.Jts2iox;
//import ch.interlis.ili2c.metamodel.SurfaceOrAreaType;

public class GeomValidatorLib {
    private static ResourceBundle rsrc = java.util.ResourceBundle.getBundle("ch.interlis.iox_j.validator.ValidatorMessages");
    
    public static int validate(String wktGeom) {    
        SurfaceType surfaceType = new SurfaceType();
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
                validateSurfaceTopology(surfaceType, geom);
                
            }
            
            
            
            
            System.out.println(geom);
            
            
        } catch (ParseException e) {
            e.printStackTrace();
            return 1;
        }
        
        return 0;
    }
    
    private static boolean validateSurfaceTopology(SurfaceType type, IomObject iomValue) {
        boolean surfaceTopologyValid=true;
        try {
            // mainObjTid -> exponieren für QGIS-fid
            surfaceTopologyValid=ItfSurfaceLinetable2Polygon.validatePolygon(mainObjTid, attr, iomValue, errFact,validateType);
        } catch (IoxException e) {
            surfaceTopologyValid=false;
            System.err.println(e.getMessage());
            System.err.println(rsrc.getString("validateSurfaceTopology.failedToValidatePolygon"));
        }
        return surfaceTopologyValid;

    }
    
    // Das ist weniger interessant. Was wir wollen steckt in validatePolylineTopology und validateSurfaceTopology
    /*
    private static boolean validatePolygon(SurfaceType surfaceType, IomObject surfaceValue) {
        if (surfaceValue.getobjecttag().equals("MULTISURFACE")){
            boolean clipped = surfaceValue.getobjectconsistency()==IomConstants.IOM_INCOMPLETE;
            for(int surfacei=0;surfacei< surfaceValue.getattrvaluecount("surface");surfacei++){
                if(!clipped && surfacei>0){
                    // unclipped surface with multi 'surface' elements
                    //logMsg(validateType, rsrc.getString("validatePolygon.invalidNumberOfSurfaceInCompleteBasket"));
                    System.err.println("****Fehler....");
                    return false;
                }
                IomObject surface= surfaceValue.getattrobj("surface",surfacei);
                int boundaryc=surface.getattrvaluecount("boundary");
                // a multisurface consists of at least one boundary.
                if(boundaryc==0){
//                    String objectIdentification = currentIomObj.getobjectoid();
//                    if(objectIdentification==null){
//                        objectIdentification = currentIomObj.getobjecttag();
//                    }
                    //logMsg(validateType, rsrc.getString("validatePolygon.missingOuterboundaryInXOfObjectY"), attrName, objectIdentification);
                    System.err.println("boundaryc==0");
                    return false;
                } else {
                    for(int boundaryi=0;boundaryi<boundaryc;boundaryi++){
                        IomObject boundary=surface.getattrobj("boundary",boundaryi);
                        if(boundaryi==0){
                            // shell
                        }else{
                            // hole
                        }    
                        for(int polylinei=0;polylinei<boundary.getattrvaluecount("polyline");polylinei++){
                            IomObject polyline=boundary.getattrobj("polyline",polylinei);
                            validatePolyline(surfaceType, polyline);
                            // add line to shell or hole
                        }
                        // add shell or hole to surface
                    }
                }
            }
        } else {
            //logMsg(validateType, "unexpected Type "+surfaceValue.getobjecttag()+"; MULTISURFACE expected");
            System.err.println("MULTISURFACE expected");
            return false;
        }
        return true;
    }
    
    private static boolean validatePolyline(SurfaceType polylineType, IomObject polylineValue) {
        boolean foundErrs=false;
        if (polylineValue.getobjecttag().equals("POLYLINE")){
            boolean clipped = polylineValue.getobjectconsistency()==IomConstants.IOM_INCOMPLETE;
            if(!clipped && polylineValue.getattrvaluecount("sequence")>1){
                // an unclipped polyline should have only one sequence element
                //logMsg(validateType, rsrc.getString("validatePolyline.invalidNumberOfSequenceInCompleteBasket"));
                System.err.println("validatePolyline.invalidNumberOfSequenceInCompleteBasket");
                foundErrs = foundErrs || true;
            }
            for(int sequencei=0;sequencei<polylineValue.getattrvaluecount("sequence");sequencei++){
                IomObject sequence=polylineValue.getattrobj("sequence",sequencei);
                LineForm[] lineforms = polylineType.getLineForms();
                HashSet<String> lineformNames=new HashSet<String>();
                for(LineForm lf:lineforms){
                    lineformNames.add(lf.getName());
                }
                if(sequence.getobjecttag().equals("SEGMENTS")){
                    if(sequence.getattrvaluecount("segment")<=1){
//                        logMsg(validateType, rsrc.getString("validatePolyline.invalidNumberOfSegments"));
                        System.err.println("validatePolyline.invalidNumberOfSegments");
                        foundErrs = foundErrs || true;
                    }
                    for(int segmenti=0;segmenti<sequence.getattrvaluecount("segment");segmenti++){
                        // segment = all segments which are in the actual sequence.
                        IomObject segment=sequence.getattrobj("segment",segmenti);
                        if(segment.getobjecttag().equals("COORD")){
                            if(lineformNames.contains("STRAIGHTS") || segmenti==0){
                                //validateCoordType(validateType, (CoordType) polylineType.getControlPointDomain().getType(), segment, attrName);
                            }else{
                                //logMsg(validateType, "unexpected COORD");
                                System.err.println("unexpected COORD");
                                foundErrs = foundErrs || true;
                            }
                        } else if (segment.getobjecttag().equals("ARC")){
                            if(lineformNames.contains("ARCS") && segmenti>0){
                                //validateARCSType(validateType, (CoordType) polylineType.getControlPointDomain().getType(), segment, attrName);
                            }else{
                                //logMsg(validateType, "unexpected ARC");
                                System.err.println("unexpected ARC");
                                foundErrs = foundErrs || true;
                            }
                        } else {
                            //logMsg(validateType, "unexpected Type "+segment.getobjecttag());
                            System.err.println("unexpected Type " + segment.getobjecttag());
                            foundErrs = foundErrs || true;
                        }
                    }
                } else {
//                    logMsg(validateType, "unexpected Type "+sequence.getobjecttag());
                    System.err.println("unexpected Type " + sequence.getobjecttag());
                    foundErrs = foundErrs || true;
                }
            }
        } else {
            //logMsg(validateType, "unexpected Type "+polylineValue.getobjecttag()+"; POLYLINE expected");
            System.err.println("unexpected Type "+polylineValue.getobjecttag()+"; POLYLINE expected");
            foundErrs = foundErrs || true;
        }
        return !foundErrs;
    }
    */
}
