package util;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Customized logger to just show one info line 
 *
 * @author mchica
 */
public class BriefFormatter extends Formatter {   


	public BriefFormatter() { 

		super(); 

	}

    @Override 
    public String format(final LogRecord record) {
        return record.getMessage();
    }   
}
