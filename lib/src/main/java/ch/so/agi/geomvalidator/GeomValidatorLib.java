package ch.so.agi.geomvalidator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.ResourceBundle;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import ch.ehi.basics.types.OutParam;
import ch.interlis.iom.IomObject;
import ch.interlis.iom_j.itf.impl.jtsext.algorithm.CurveSegmentIntersector;
import ch.interlis.iom_j.itf.impl.jtsext.geom.ArcSegment;
import ch.interlis.iom_j.itf.impl.jtsext.geom.CompoundCurve;
import ch.interlis.iom_j.itf.impl.jtsext.geom.CurvePolygon;
import ch.interlis.iom_j.itf.impl.jtsext.geom.CurveSegment;
import ch.interlis.iom_j.itf.impl.jtsext.geom.JtsextGeometryFactory;
import ch.interlis.iom_j.itf.impl.jtsext.geom.StraightSegment;
import ch.interlis.iom_j.itf.impl.jtsext.noding.CompoundCurveNoder;
import ch.interlis.iom_j.itf.impl.jtsext.noding.Intersection;
import ch.interlis.iom_j.itf.impl.jtsext.operation.polygonize.IoxPolygonizer;
import ch.interlis.iox.IoxException;
import ch.interlis.iox_j.IoxInvalidDataException;
import ch.interlis.iox_j.jts.Iox2jtsException;
import ch.interlis.iox_j.jts.Iox2jtsext;
import ch.interlis.iox_j.jts.Jts2iox;
import ch.interlis.iox_j.jts.Jtsext2iox;
import ch.interlis.iox_j.logging.Log2EhiLogger;
import ch.interlis.iox_j.logging.LogEventFactory;
import ch.interlis.iox_j.validator.ValidationConfig;

public class GeomValidatorLib {
    private static ResourceBundle rsrc = java.util.ResourceBundle.getBundle("ch.interlis.iox_j.validator.ValidatorMessages");
    
    public static int validate(String layername, String fid, String wktGeom) {    
        LogEventFactory errFact = new LogEventFactory();
        errFact.setLogger(new Log2EhiLogger());
        
        boolean valid = true;
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
                valid = validateSurfaceTopology(layername, fid, geom, errFact);
            } else if (jtsGeom instanceof MultiPolygon) {
                geom = Jts2iox.JTS2multisurface((MultiPolygon)jtsGeom);
                valid = validateSurfaceTopology(layername, fid, geom, errFact);

                // TODO: Wie genau ist das mit den Multipolygonen?
//                MultiPolygon multiPolygon = (MultiPolygon) jtsGeom;
//                for (int i=0; i<multiPolygon.getNumGeometries(); i++) {
//                    Polygon poly = (Polygon) multiPolygon.getGeometryN(i);
//                    geom = Jts2iox.JTS2surface(poly);
//                    valid = validateSurfaceTopology(layername, fid, geom, errFact);
//                    System.out.println("--------------****");
//                    System.out.println(geom);
//                }
            }            
        } catch (ParseException e) {
            e.printStackTrace();
            return 2;
        }
        
        // TODO: Funktioniert nicht, da auch Fehler auftreten, die keine Auswirkung auf die Variable haben (?). 
        // Z.B. duplicate coords.
        //if (!valid) return 1;  
        
        return 0;
    }
    
    private static boolean validateSurfaceTopology(String layername, String mainObjTid, IomObject iomValue, LogEventFactory errFact) {
        boolean surfaceTopologyValid=true;
        try {
            surfaceTopologyValid = validatePolygon(layername, mainObjTid, iomValue, errFact);
        } catch (IoxException e) {
            surfaceTopologyValid=false;
            errFact.addEvent(errFact.logErrorMsg(e, rsrc.getString("validateSurfaceTopology.failedToValidatePolygon")));
        }
        return surfaceTopologyValid;
    }
    
    public static boolean validatePolygon(String layername, String mainTid,IomObject polygon,LogEventFactory errFact) 
            throws IoxException
    {
        String linetableIliqname = layername;
        boolean polygonValid = true;
        double maxOverlaps = 0.001; // hardcodiert
        double size = 3.0; // Siehe Code iox-ili. Wert mit einem Beispiel-XTF debugged.
        double newVertexOffset = 0.002; // 2*Math.pow(10, -size)
        
        JtsextGeometryFactory jtsFact=new JtsextGeometryFactory();
        ArrayList<CompoundCurve> segv=createLineset(polygon,ValidationConfig.ON,0.0,errFact);
        if(segv==null){
            return true;
        }
        for(CompoundCurve seg:segv) {
            seg.setUserData(mainTid);
        }
        
        OutParam<Polygon> poly=new OutParam<Polygon>();
        ArrayList<IoxInvalidDataException> dataerrs=new ArrayList<IoxInvalidDataException>();
        try {
            createPolygon(mainTid, segv, maxOverlaps, newVertexOffset, dataerrs, linetableIliqname, null, poly);
        } finally {
            if (dataerrs.size() > 0) {
                polygonValid=false;
            }
            for (IoxInvalidDataException err : dataerrs){
                errFact.addEvent(errFact.logError(err));
            }
        }
        return polygonValid;
    }
    
    private static ArrayList<CompoundCurve> createLineset(IomObject obj, String validationType, double tolerance,LogEventFactory errFact) throws IoxException {
        return Iox2jtsext.surface2JTSCompoundCurves(obj, validationType, tolerance, errFact);
    }

    private static void createPolygon(String mainTid,
            ArrayList<CompoundCurve> segv,double maxOverlaps,double newVertexOffset,ArrayList<IoxInvalidDataException> dataerrs,
            String linetableIliqname,String geomattrIliqname,OutParam<Polygon> returnPolygon) 
                    throws IoxInvalidDataException 
    {
        boolean hasIntersections=false;
        boolean isDisconnected=false;
        
        // TODO: Will man INTERLIS-valide Selfintersections entfernen?
        for(CompoundCurve seg : segv){
            removeValidSelfIntersections(seg,maxOverlaps,newVertexOffset);
        }
        
        // ASSERT: segv might contain rings, but not nested rings
        CompoundCurveNoder validator=new CompoundCurveNoder(segv,false);
        if(!validator.isValid()){
            for(Intersection is:validator.getIntersections()){
                CompoundCurve e0=is.getCurve1();
                CompoundCurve e1=is.getCurve2();
                CurveSegment seg0=is.getSegment1();
                CurveSegment seg1=is.getSegment2();
                int segIndex0=e0.getSegments().indexOf(is.getSegment1());
                int segIndex1=e1.getSegments().indexOf(is.getSegment2());
                Coordinate p00;
                Coordinate p01;
                Coordinate p10;
                Coordinate p11;
                p00 = e0.getSegments().get(segIndex0).getStartPoint();
                p01 = e0.getSegments().get(segIndex0).getEndPoint();
                p10 = e1.getSegments().get(segIndex1).getStartPoint();
                p11 = e1.getSegments().get(segIndex1).getEndPoint();
                if(is.isOverlay()) {
                    String []tids=new String[2];
                    tids[0]=(String) is.getCurve1().getUserData();
                    tids[1]=(String) is.getCurve2().getUserData();
                    if(tids[0].equals(tids[1])) {
                        tids[1]=null;
                    }
                    dataerrs.add(new IoxInvalidDataException(is.toShortString(),linetableIliqname,null,Jtsext2iox.JTS2coord(is.getPt()[0])));
                    hasIntersections=true;
                }else if(e0!=e1 &&
                        (segIndex0==0 || segIndex0==e0.getSegments().size()-1) 
                        && (segIndex1==0 || segIndex1==e1.getSegments().size()-1) 
                        && is.getOverlap()!=null && is.getOverlap()<maxOverlaps){
                    // Ende- bzw. Anfangs-Segment verschiedener Linien
                    // valid overlap, ignore for now, will be removed later in IoxPolygonizer
                }else if(e0==e1 && (
                              Math.abs(segIndex0-segIndex1)==1 
                              || Math.abs(segIndex0-segIndex1)==e0.getNumSegments()-1  ) // bei Ring: letztes Segment und Erstes Segment
                              && (is.isIntersection(p00) || is.isIntersection(p01))
                              && (is.isIntersection(p10) || is.isIntersection(p11))
                              && is.getOverlap()!=null && is.getOverlap()<maxOverlaps){
                        // aufeinanderfolgende Segmente der selben Linie
                    throw new IllegalStateException("unexpected overlap; should have been removed before;"+is);
                }else{
                    String []tids=new String[2];
                    tids[0]=(String) is.getCurve1().getUserData();
                    tids[1]=(String) is.getCurve2().getUserData();
                    
                    dataerrs.add(new IoxInvalidDataException(is.toShortString(),linetableIliqname,null,Jtsext2iox.JTS2coord(is.getPt()[0])));
                    hasIntersections=true;
                }
            }
            if(hasIntersections){
                return;
            }
        }
        IoxPolygonizer polygonizer=new IoxPolygonizer(newVertexOffset);
        //com.vividsolutions.jts.operation.polygonize.Polygonizer polygonizer=new com.vividsolutions.jts.operation.polygonize.Polygonizer();
        //for(CompoundCurve boundary:segv){
        for(CompoundCurve boundary:validator.getNodedSubstrings()){
            //System.out.println(boundary);
            polygonizer.add(boundary);
        }
        Collection cutEdges = polygonizer.getCutEdges();
        if(!cutEdges.isEmpty()){
            for(Object edge:cutEdges){
                try {
                    dataerrs.add(new IoxInvalidDataException("cut edge "+IoxInvalidDataException.formatTids((CompoundCurve) edge),linetableIliqname,null,Jtsext2iox.JTS2polyline((CompoundCurve)edge)));
                } catch (Iox2jtsException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
        Collection dangles=polygonizer.getDangles();
        if(!dangles.isEmpty()){
            for(Object dangle:dangles){
                try {
                    dataerrs.add(new IoxInvalidDataException("dangle "+IoxInvalidDataException.formatTids((CompoundCurve) dangle),linetableIliqname,null,Jtsext2iox.JTS2polyline((CompoundCurve)dangle)));
                } catch (Iox2jtsException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
        Collection invalidRingLines=polygonizer.getInvalidRingLines();
        if(!invalidRingLines.isEmpty()){
            for(Object invalidRingLine:invalidRingLines){
                try {
                    dataerrs.add(new IoxInvalidDataException("invald ring line"+IoxInvalidDataException.formatTids((CompoundCurve) invalidRingLine),linetableIliqname,null,Jtsext2iox.JTS2polyline((CompoundCurve)invalidRingLine)));
                } catch (Iox2jtsException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
        Collection<Polygon> polys = polygonizer.getPolygons();
        if(polys.isEmpty()){
            dataerrs.add(new IoxInvalidDataException("no polygon"));
            return;
        }
        Polygon poly=null;
        if(polys.size()>1){
            Iterator<Polygon> pi=polys.iterator();
            poly=pi.next();
            Envelope shell=poly.getEnvelopeInternal();
            while(pi.hasNext()){
                Polygon nextPoly=pi.next();
                Envelope nextEnv=nextPoly.getEnvelopeInternal();
                if(nextEnv.contains(shell)){
                    poly=nextPoly;
                    shell=nextEnv;
                }
            }
            pi=polys.iterator();
            while(pi.hasNext()){
                Polygon holePoly=pi.next();
                if(holePoly==poly){
                    continue;
                }
                Envelope holeEnv=holePoly.getEnvelopeInternal();
                if(shell.contains(holeEnv) && !shell.equals(holeEnv)){
                }else{
                    isDisconnected=true;
                    try {
                        dataerrs.add(new IoxInvalidDataException("superfluous outerboundary "+IoxInvalidDataException.formatTids(new String[] {mainTid}),geomattrIliqname,mainTid,Jtsext2iox.JTS2surface(holePoly)));
                    } catch (Iox2jtsException e) {
                        throw new IllegalStateException(e);
                    }
                }
            }
            if(isDisconnected) {
                dataerrs.add(new IoxInvalidDataException("multipolygon detected"));
                return;
            }
        }else{
            poly=polys.iterator().next();
        }
        poly.normalize();
        returnPolygon.value=poly;
    }

    public static void removeValidSelfIntersections(CompoundCurve seg,double maxOverlaps, double newVertexOffset) {

        if(seg.getNumSegments()==1){
            return;
        }
        for(int segIndex0=0;segIndex0<seg.getNumSegments();segIndex0++){
            int segIndex1=segIndex0+1;
            if(segIndex1==seg.getNumSegments()){
                segIndex1=0;
            }
            CurveSegment seg0=seg.getSegments().get(segIndex0);
            CurveSegment seg1=seg.getSegments().get(segIndex1);
            CurveSegmentIntersector li = new CurveSegmentIntersector();
            li.computeIntersection(seg0, seg1);
            if(li.hasIntersection()){
                if(li.getIntersectionNum()==2){ 
                    if(li.isOverlay()) {
                        // hier ignorieren; wird danach im CompoundCurveNoder rapportiert
                    }else if(seg.getNumSegments()==2 && seg0.getStartPoint().equals2D(seg1.getEndPoint())){
                        // Ring als eine Linie, zwei Segmente
                    }else if(li.getOverlap()!=null && li.getOverlap()<maxOverlaps){
                        // aufeinanderfolgende Segmente der selben Linie
                        Intersection is = new Intersection(
                                li.getIntersection(0), li.getIntersection(1),
                                seg, seg, seg0, seg1, li.getOverlap(),false);
                        //EhiLogger.traceState(CurvePolygon.VALID_OVERLAP +" "+ is.toString());
                        System.out.println(CurvePolygon.VALID_OVERLAP +" "+ is.toString());
                          // overlap entfernen
                          if(seg0 instanceof StraightSegment){
                              seg.removeOverlap((ArcSegment) seg1, seg0, newVertexOffset);
                              //segIndex0++;
                          }else if(seg1 instanceof StraightSegment){
                              seg.removeOverlap((ArcSegment) seg0, seg1, newVertexOffset);
                          }else if(((ArcSegment) seg0).getRadius()>((ArcSegment) seg1).getRadius()){
                              seg.removeOverlap((ArcSegment) seg1, seg0, newVertexOffset);
                              //segIndex0++;
                          }else{
                              // seg1.getRadius() > seg0.getRadius()
                              seg.removeOverlap((ArcSegment) seg0, seg1, newVertexOffset);
                          }
                    }
                }else if(li.getIntersectionNum()==1){
                    // endPt==startPt
                }else{
                    throw new IllegalArgumentException("seg0 and seg1 are not connected");
                }
            }
        }
        
    }
}
