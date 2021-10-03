package ch.so.agi.geomvalidator;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeomValidatorLibTest {
    Logger logger = LoggerFactory.getLogger(GeomValidatorLibTest.class);
    
    @Test
    public void validate_Ok() {
        int result = GeomValidatorLib.validate();
        
        
        logger.info("result: " + String.valueOf(result));
    }
    
}
