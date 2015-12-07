/**
 * Bus Stop
 * 2015-12-01 K.OHWADA
 */

package jp.ohwada.java.ksj.busutil;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * BusParserBase
 */
public class BusParserBase {

    protected BusDbFactory mDb;

    private boolean isDubugMsg = true;

    /**
     * constrctor
     */
    public BusParserBase() {
        // dummy
    }

    /**
     * setDb
     * @param BusStopDb db
     */
    public void setDb( BusDbFactory db ) {
        mDb = db;
    }

    /**
     * setDubugMsg
     * @param boolean flag
     */
    public void setDubugMsg( boolean flag ) {
        isDubugMsg = flag;
        mDb.setDubugMsg( flag );
    }

    /**
     * checkFilename
     * @param String name
     * @param String pattern
     * @return boolean
     */
    protected boolean checkFilename( String name,  String pattern ) {
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(name);
        return m.find();
    }
 
    /**
     * getRootNode
     * @param String name
     * @return Node
     */   
    protected Node getRootNode( String name ) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setIgnoringElementContentWhitespace(true);
        factory.setIgnoringComments(true);
        factory.setValidating(true);
        DocumentBuilder builder = null;
        Node root = null;
        try {
            builder = factory.newDocumentBuilder();
            builder.setErrorHandler( new BusParserErrorHandler() );
            root = builder.parse(name);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return root;
    }

    /**
     * <gml:Curve gml:id="cv1">
     */
    protected String parseGmlId(Node node) {
        return parseAttribute( node, "gml:id" );
    }

    /**
     * <ksj:brt xlink:href="#cv1"/>
     */
    protected String parseXlinkHref(Node node) {
        return parseAttribute( node, "xlink:href" );
    }

    private String parseAttribute( Node node, String key ) {
        String ret = "";
        NamedNodeMap nodeMap = node.getAttributes();
        if (null != nodeMap) {
            for ( int i=0; i < nodeMap.getLength();i++ ) {
                if (nodeMap.item(i).getNodeName().equals(key)) {
                    ret = nodeMap.item(i).getNodeValue();
                    break;
                } // if
            } // for
        } // if nodeMap
        return ret;
    }

    /**
     * printMsg
     * @param String str
     */
    protected void printMsg( String str ) {
        if (isDubugMsg) System.out.println( str );
    }

    /**
     * printAlways (high level)
     * @param String str
     */
    protected void printAlways( String str ) {
        if (!isDubugMsg) System.out.println();
        System.out.println( str );
    }

    /**
     * printProgres
     */    
    protected void printProgres() {
        if (!isDubugMsg) System.out.print(".");
    }

}