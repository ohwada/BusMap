/**
 * Bus Stop
 * 2015-12-01 K.OHWADA
 */

package jp.ohwada.java.ksj.busutil;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

/**
 * BusParserErrorHandler
 */
public class BusParserErrorHandler implements ErrorHandler {
    /**
     * warning
     * @param SAXParseException e
     */
    public void warning( SAXParseException e ) {
        System.out.println("Warning: " + e.getLineNumber() +"line");
        System.out.println(e.getMessage());
    }

    /**
     * error
     * @param SAXParseException e
     */
    public void error( SAXParseException e ) {
        System.out.println("Error: " + e.getLineNumber() +"line");
        System.out.println(e.getMessage());
    }

    /**
     * fatalError
     * @param SAXParseException e
     */
    public void fatalError( SAXParseException e ) {
        System.out.println("FatalError: " + e.getLineNumber() +"line");
        System.out.println(e.getMessage());
    }
}


