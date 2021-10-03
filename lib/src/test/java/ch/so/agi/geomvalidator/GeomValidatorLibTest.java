package ch.so.agi.geomvalidator;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeomValidatorLibTest {
    Logger logger = LoggerFactory.getLogger(GeomValidatorLibTest.class);
    
    @Test
    public void validate_Ok() {
        //String wktGeom = "POLYGON ((30 10, 40 40, 20 40, 10 20, 30 10))";
        //String wktGeom = "MultiPolygon (((2609000 1236700, 2609200 1236700, 2609200 1236600, 2609000 1236600, 2609000 1236700)))";
        String wktGeom = "POLYGON ((2609000 1236700, 2609200 1236700, 2609200 1236600, 2609000 1236600, 2609000 1236700))";
        int result = GeomValidatorLib.validate(wktGeom);
        
        
        logger.info("result: " + String.valueOf(result));
    }
    
}
