/**
 * Bus Stop
 * 2015-12-01 K.OHWADA
 */

package jp.ohwada.java.ksj;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import jp.ohwada.java.ksj.busutil.*;

/*
 *  <?xml version="1.0" encoding="UTF-8"?>
 *  <ksj:Dataset gml:id="P11Dataset" xmlns:ksj="http://nlftp.mlit.go.jp/ksj/schemas/ksj-app" xmlns:gml="http://www.opengis.net/gml/3.2" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://nlftp.mlit.go.jp/ksj/schemas/ksj-app KsjAppSchema-P11-v2_0.xsd">
 *      <gml:description>国土数値情報 バス停留所 インスタンス文書</gml:description>
 *      <!-- データ提供範囲 -->
 *      <gml:boundedBy>
 *          <gml:EnvelopeWithTimePeriod srsName="JGD2000 / (B, L)" frame="GC / JST">
 *              <gml:lowerCorner>20.0 123.0</gml:lowerCorner>
 *              <gml:upperCorner>46.0 154.0</gml:upperCorner>
 *                  <gml:beginPosition calendarEraName="西暦">1900</gml:beginPosition>
 *              <gml:endPosition indeterminatePosition="unknown"/>
 *          </gml:EnvelopeWithTimePeriod>
 *      </gml:boundedBy>
 *      <gml:Point gml:id="n1">
 *          <gml:pos>35.14591397 139.10569573</gml:pos>
 *      </gml:Point>
 *      <gml:Point gml:id="n2">
 *          ...
 *      </gml:Point>
 *      <ksj:BusStop gml:id="ED01_1">
 *          <ksj:position xlink:href="#n1"/>
 *          <ksj:busStopName>城堀</ksj:busStopName>
 *          <ksj:busRouteInformation>
 *              <ksj:BusRouteInformation>
 *                  <ksj:busType>1</ksj:busType>
 *                  <ksj:busOperationCompany>箱根登山バス</ksj:busOperationCompany>
 *                  <ksj:busLineName>小01</ksj:busLineName>
 *              </ksj:BusRouteInformation>
 *          </ksj:busRouteInformation>
 *          <ksj:busRouteInformation>
 *              ...
 *          </ksj:busRouteInformation>
 *      </ksj:BusStop>
 *      <ksj:BusStop gml:id="ED01_2">
 *          ...
 *      </ksj:BusStop>
 *  </ksj:Dataset>
 * /

/**
 * BusStop
 */
public class BusStop extends BusParserBase {

    private int mPrefId;

    /**
     * constrctor
     */
    public BusStop() {
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
     * @param int prefId
     */
    public void parse( String name, int prefId ) {
        long start = System.currentTimeMillis();
        printAlways( name );
        // P11-10_14-jgd-g.xml
        if ( !checkFilename( name,  "P11-10_(.*)-jgd-g.xml" ) ) {
            printAlways( "not find" );
            return;
        }
        mPrefId = prefId;
        Node root = getRootNode( name );
        mDb.prepareStatement();
        int counter = parseRecursive( root );
        mDb.closeStatement();
        long time = System.currentTimeMillis() - start;
        printAlways( "Number of bus stop: " + counter );
        printAlways( "Excution Time: " + time );
    }

    /**
     * parseRecursive
     * @param Node node
     * @param timeStampStr
     * @return int
     */
    private int parseRecursive( Node node ) {
        int counter = 0;
        NodeList nodes = node.getChildNodes();
        for (int i=0; i<nodes.getLength(); i++) {
            Node nodeChild = (Node) nodes.item(i);
            String name = nodeChild.getNodeName();
            printProgres();
            if (name.equals("gml:Point")) {
                // gml:Point
                parseGmlPoint(nodeChild);
            } else if (name.equals("ksj:BusStop")) {
                // ksj:BusStop
                parseBusStop(nodeChild);
                counter++;
            } else {
                // recursive call myself
                counter += parseRecursive(nodeChild);
            } // if
        } // for
        return counter;
    }

    /*
     *  <ksj:BusStop gml:id="ED01_1">
     *      <ksj:position xlink:href="#n1"/>
     *      <ksj:busStopName>城堀</ksj:busStopName>
     *      <ksj:busRouteInformation>
     *              ...
     *      </ksj:busRouteInformation>
     *      <ksj:busRouteInformation>
     *              ...
     *      </ksj:busRouteInformation>
     *  </ksj:BusStop>
     */
    private void parseBusStop( Node node ) {
        int nodeId = 0;
        String nodeHref = "";
        String nodeName = "";
        List<BusRouteInformation> infos = new ArrayList<BusRouteInformation>();

        String busStopKey = parseGmlId(node);

        NodeList nodes = node.getChildNodes();
        for (int i=0; i < nodes.getLength(); i++) {
            Node nodeChild = (Node) nodes.item(i);
            String name = nodeChild.getNodeName();
            if (name.equals("ksj:position")) {
                // ksj:position
                String id = parseXlinkHref( nodeChild );
                // #n1 -> n1
                nodeHref = id.substring(1);
                // #n1 -> 1
                nodeId = Integer.parseInt( id.substring(2) );
            } else if (name.equals("ksj:busStopName")) {
                // ksj:busStopName
                nodeName = nodeChild.getTextContent();
            } else if (name.equals("ksj:busRouteInformation")) {
                // ksj:busRouteInformation
                BusRouteInformation info = parseBusRouteInformation(nodeChild);
                if ( info != null ) {
                    infos.add(info);
                } // if
            } // if
        } // for NodeList

        mDb.execBusStop( mPrefId, nodeId, nodeHref, busStopKey, nodeName, infos );
    }

    /*
     *  <gml:Point gml:id="n1">
     *      <gml:pos>35.14591397 139.10569573</gml:pos>
     *  </gml:Point>
     */
    private void parseGmlPoint(Node node) {
        String key = parseGmlId(node);
        // n1 -> 1
        int id = Integer.parseInt( key.substring(1) );
        NodeList nodes = node.getChildNodes();
        for (int i=0; i < nodes.getLength(); i++) {
            Node nodeChild = (Node) nodes.item(i);
            if (nodeChild.getNodeName().equals("gml:pos")) {
                // gml:pos
                String positionStr = nodeChild.getTextContent().trim();
                String[] str4Ary = positionStr.split(" ");
                float lat = Float.parseFloat( str4Ary[0] );
                float lon = Float.parseFloat( str4Ary[1] );
                mDb.execNodePoint( mPrefId, id, key, lat, lon );
            } // if 
        } // for NodeList
    }

    /*
     *  <ksj:busRouteInformation>
     *      <ksj:BusRouteInformation>
     *          <ksj:busType>1</ksj:busType>
     *          <ksj:busOperationCompany>箱根登山バス</ksj:busOperationCompany>
     *          <ksj:busLineName>小01</ksj:busLineName>
     *      </ksj:BusRouteInformation>
     *  </ksj:busRouteInformation>
     */
    private BusRouteInformation parseBusRouteInformation(Node node) {
        BusRouteInformation info = null;
        NodeList nodes = node.getChildNodes();
        for (int i=0; i < nodes.getLength(); i++) {
            Node nodeChild = (Node) nodes.item(i);
            if (nodeChild.getNodeName().equals("ksj:BusRouteInformation")) {
                // ksj:BusRouteInformation
                info = parseBusRouteInformationChildChild( nodeChild );
            } // if 
        } // for
         return info;
    }

    private BusRouteInformation parseBusRouteInformationChildChild(Node node) {
        BusRouteInformation info = new BusRouteInformation();
        boolean flag = false;
        // ksj:BusRouteInformation
        NodeList nodes = node.getChildNodes();
        for (int i=0; i < nodes.getLength(); i++) {
            Node nodeChild = (Node) nodes.item(i);
            String name = nodeChild.getNodeName();
            String text = new String(nodeChild.getTextContent());
            if (name.equals("ksj:busType")) {
                // ksj:busType
                info.bus_type = Integer.parseInt( text );
                flag = true;
            } else if (name.equals("ksj:busLineName")) {
                // ksj:busLineName
                info.bus_line_name = text;
                flag = true;
            } else if (name.equals("ksj:busOperationCompany")) {
                // ksj:busOperationCompany
                info.bus_operation_company = text;
                flag = true;
            } // if                
        } // for
        if ( flag ) return info;
        return null;
    }

}