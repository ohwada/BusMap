/**
 * Bus Stop
 * 2015-12-01 K.OHWADA
 */

package jp.ohwada.java.ksj;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import jp.ohwada.java.ksj.busutil.*;

/*
 * <?xml version="1.0" encoding="UTF-8"?>
 * <ksj:Dataset gml:id="N07Dataset" xmlns:ksj="http://nlftp.mlit.go.jp/ksj/schemas/ksj-app" xmlns:gml="http://www.opengis.net/gml/3.2" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:schemaLocation="http://nlftp.mlit.go.jp/ksj/schemas/ksj-app KsjAppSchema-N07-v1_0.xsd">
 * <gml:description>国土数値情報（バスルート）データ　2011年度</gml:description>
 * <gml:boundedBy>
 *	<gml:EnvelopeWithTimePeriod srsName="JGD2000 / (B, L)" frame="GC / JST">
 *		<gml:lowerCorner>20.0 123.0</gml:lowerCorner>
 *		<gml:upperCorner>46.0 154.0</gml:upperCorner>
 *		<gml:beginPosition calendarEraName="西暦">1900</gml:beginPosition>
 *		<gml:endPosition indeterminatePosition="unknown"/>
 *	</gml:EnvelopeWithTimePeriod>
 * </gml:boundedBy>
 *	<gml:Curve gml:id="cv1">
 *		<gml:segments>
 *			<gml:LineStringSegment>
 *				<gml:posList>
 *				35.46597634 139.62560244
 *				35.46610111 139.62538722
 *				35.46632083 139.62502972
 *                                        ...
 *				</gml:posList>
 *			</gml:LineStringSegment>
 *		</gml:segments>
 *	</gml:Curve>
 *	<gml:Curve gml:id="cv2">
 *                    ...
 *	</gml:Curve>
 *       <ksj:BusRoute gml:id="br1">
 *                <ksj:brt xlink:href="#cv1"/>
 *                <ksj:bsc>1</ksj:bsc>
 *                <ksj:boc>弘南バス（株）</ksj:boc>
 *                <ksj:bln>ノクターン号</ksj:bln>
 *                <ksj:rpd>1.0</ksj:rpd>
 *                <ksj:rps>1.0</ksj:rps>
 *                <ksj:rph>1.0</ksj:rph>
 *                <ksj:rmk></ksj:rmk>
 *        </ksj:BusRoute>
 *       <ksj:BusRoute gml:id="br2">
 *            ...
 *        </ksj:BusRoute>
 * </ksj:Dataset>
 */

/**
 * BusRoute
 */
public class BusRoute extends BusParserBase {

    /**
     * constrctor
     */
    public BusRoute() {
        // dummy
    }

    /**
     * createTable
     */
    public boolean createTable() {
        return mDb.createTable();
    }

    /**
     * parse
     * @param String name 
     */
    public void parse( String name ) {
        long start = System.currentTimeMillis();
        printAlways( name );
        // N07-11_xx.xml
        if ( !checkFilename( name,  "N07-11.xml" ) &&
            !checkFilename( name,  "N07-11_(.*).xml" ) ) {
            printAlways( "not find" );
            return;
        }
        Node root = getRootNode( name );
        mDb.prepareStatement();
        int counter = parseRecursive( root );
        mDb.closeStatement();
        long time = System.currentTimeMillis() - start;
        printAlways( "Number of bus route: " + counter );
        printAlways( "Excution Time: " + time );
    }

    /**
     * parseRecursive
     * @param Node node
     * @return int
     */
    private int parseRecursive( Node node ) {
        int counter = 0;
        NodeList nodes = node.getChildNodes();
        for (int i=0; i<nodes.getLength(); i++) {
            Node nodeChild = (Node) nodes.item(i);
            String name = nodeChild.getNodeName();
            printProgres();
            if (name.equals("gml:Curve")) {
                // gml:Curve
                parseCurve(nodeChild);
            } else if (name.equals("ksj:BusRoute")) {
                // ksj:BusRoute
                parseBusRoute(nodeChild);
                counter++;
            } else {
                // recursive call myself
                counter += parseRecursive(nodeChild);
            } // if
        } // for
        return counter;
    }

    /*  
     *	<gml:Curve gml:id="cv1">
     *		<gml:segments>
     *			<gml:LineStringSegment>
     *				<gml:posList>
     *				35.46757838 139.62545170
     *				35.46727496 139.62519785
     *				</gml:posList>
     *			</gml:LineStringSegment>
     *		</gml:segments>
     *	</gml:Curve>
     */
    private void parseCurve(Node node) {
        String key = parseGmlId(node);
        NodeList nodes = node.getChildNodes();
        for (int i=0; i < nodes.getLength(); i++) {
            Node nodeChild = (Node) nodes.item(i);
            String name = nodeChild.getNodeName();
            if (name.equals("gml:segments")) {
                String list = parseSegments(nodeChild);
                mDb.execRouteCurve( key, convPosList(list) );
            } 
        }
    }

    private String parseSegments(Node node) {
        String list = "";
        NodeList nodes = node.getChildNodes();
        for (int i=0; i < nodes.getLength(); i++) {
            Node nodeChild = (Node) nodes.item(i);
            String name = nodeChild.getNodeName();
            if (name.equals("gml:LineStringSegment")) {
                // gml:LineStringSegment
                list = parseLineStringSegment(nodeChild);
            } // if 
        } // for
        return list;
    }

    private String parseLineStringSegment(Node node) {
        String list = "";
        NodeList nodes = node.getChildNodes();
        for (int i=0; i < nodes.getLength(); i++) {
            Node nodeChild = (Node) nodes.item(i);
            String name = nodeChild.getNodeName();
            if (name.equals("gml:posList")) {
                // gml:posList
                list = nodeChild.getTextContent();
            } // if 
        } // for
        return list;
    }

    /*
     * before
     *      35.46597634 139.62560244
     *      35.46610111 139.62538722
     * after
     * 35.46597634,139.62560244,35.46610111,139.62538722
     */
    private String convPosList( String str ) {
        // Split with newline
        String[] array = str.split("\n");
        String ret = "";
        boolean isFirst = true;
        String s2, s3;
        for( String s1: array ) {
            // Remove the front and back of space and tab
            s2 = s1.trim();
            if ( "".equals(s2) ) continue;
            // convert space in the middle to comma
            s3 = s2.replaceAll( " ", "," );
            if ( !isFirst ) {
                // Separate the line with comma
                ret += ",";                
            }
            isFirst = false;
            ret += s3;
        }
        return ret;
    }

    /*
     *       <ksj:BusRoute gml:id="br1">
     *                <ksj:brt xlink:href="#cv1"/>
     *                <ksj:bsc>1</ksj:bsc>
     *                <ksj:boc>弘南バス（株）</ksj:boc>
     *                <ksj:bln>ノクターン号</ksj:bln>
     *                <ksj:rpd>1.0</ksj:rpd>
     *                <ksj:rps>1.0</ksj:rps>
     *                <ksj:rph>1.0</ksj:rph>
     *                <ksj:rmk></ksj:rmk>
     *        </ksj:BusRoute>
     */
    private void parseBusRoute(Node node) {
        BusRouteInformation info = new BusRouteInformation();
        info.bus_route_key = parseGmlId(node);
        NodeList nodes = node.getChildNodes();
        for (int i=0; i < nodes.getLength(); i++) {
            Node nodeChild = (Node) nodes.item(i);
            String name = nodeChild.getNodeName();
            String text = new String(nodeChild.getTextContent());
            if (name.equals("ksj:brt")) {
                // ksj:brt
                String id = parseXlinkHref( nodeChild );
                // #cv1 -> cv1
                info.bus_curve_key = id.substring(1);
                // #cv1 -> 1
                info.bus_curve_id = Integer.parseInt( id.substring(3) );
            } else if (name.equals("ksj:bsc")) {
                // ksj:bsc
                info.bus_type = Integer.parseInt( text );
            } else if (name.equals("ksj:boc")) {
                // ksj:boc
                info.bus_operation_company = text;
            } else if (name.equals("ksj:bln")) {
                // ksj:bln
                info.bus_line_name = text;
            } else if (name.equals("ksj:rpd")) {
                // ksj:rpd
                info.rate_per_day = Float.parseFloat(text);
            } else if (name.equals("ksj:rps")) {
                // ksj:rps
                info.rate_per_saturday = Float.parseFloat(text);
            } else if (name.equals("ksj:rph")) {
                // ksj:rph
                info.rate_per_holiday = Float.parseFloat(text);
            } else if (name.equals("ksj:rmk")) {
                // ksj:rmk
                info.remarks = text;
            } // if
        }  // for
        mDb.execBusRoute( info );               
    }

}