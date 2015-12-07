/**
 * Bus Map
 * 2015-12-01 K.OHWADA
 */

package jp.ohwada.java.busmap.busfile;
import java.io.*;

/*
 * <?xml version="1.0" encoding="UTF-8" ?>
 * <osm version="0.6" generator="ReadKIBAN">
 * <node id="-1" timestamp="2015-11-20T21:32:50Z" lat="35.14591397" lon="139.10569573">
 *      <tag k="name" v="城堀"/>
 *      <tag k="fixme" v="platform/stop_positionを選択して、正しい位置に移動させてください"/>
 *      <tag k="source" v="KSJ2"/>
 *      <tag k="source_ref" v="http://nlftp.mlit.go.jp/ksj/gml/datalist/KsjTmplt-P11.html"/>
 *      <tag k="created_by" v="National-Land-Numerical-Information_MLIT_Japan"/>
 *      <tag k="note" v="National-Land Numerical Information (Bus stop) 2012, MLIT Japan"/>
 *      <tag k="note:ja" v="国土数値情報（バス停留所）平成２４年　国土交通省"/>
 *      <tag k="public_transport" v="platform"/>
 *      <tag k="public_transport" v="stop_position"/>
 *      <tag k="highway" v="bus_stop"/>
 *      <tag k="bus" v="yes"/>
 * </node>
 * </osm>
 */

/**
 * BusFileOsm
 */ 
public class BusFileOsm extends BusFileCommon {

    /**
     * constractor
     */ 
    public BusFileOsm() {
        super();
    }

    /**
     * open
     * @param File dir
     * @param String name
     */ 
    public String open( File dir, String name )  {
        File file = new File( dir, name + ".osm" );
        openWriter( file );
        return file.getName();
    }

    /**
     * writeHeader
     * @param String title
     */ 
    public void writeHeader( String title )  {
        try {
            mWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
            mWriter.newLine();
            mWriter.write("<osm version=\"0.6\" generator=\"ReadKIBAN\">");
            mWriter.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * writePoint
     * @param Double lat
     * @param Double lon
     * @param String name
     * @param int nodeid
     * @param String time
     */
    public void writePoint( Double lat, Double lon, String name, int nodeid, String time ) {
        String str = makeNode( nodeid, name, lat, lon, time );
        try {
            mWriter.write(str);
            mWriter.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * writeFooter
     */ 
    public void writeFooter()  {
        try {
            mWriter.write("</osm>");
            mWriter.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String makeNode( int code, String name, Double lat, Double lon, String timeStampStr ) {
        String str = ("<node id=\""+ code +"\" timestamp=\""+ timeStampStr +"Z\" lat=\""+ lat +"\" lon=\""+ lon +"\">\n");
        str += "<tag k=\"name\" v=\""+ name +"\"/>\n";
        str += "<tag k=\"fixme\" v=\"platform/stop_positionを選択して、正しい位置に移動させてください\"/>\n";
        str += "<tag k=\"source\" v=\"KSJ2\"/>\n";
        str += "<tag k=\"source_ref\" v=\"http://nlftp.mlit.go.jp/ksj/gml/datalist/KsjTmplt-P11.html\"/>\n";
        str += "<tag k=\"created_by\" v=\"National-Land-Numerical-Information_MLIT_Japan\"/>\n";
        str += "<tag k=\"note\" v=\"National-Land Numerical Information (Bus stop) 2012, MLIT Japan\"/>\n";
        str += "<tag k=\"note:ja\" v=\"国土数値情報（バス停留所）平成２４年　国土交通省\"/>\n";
        str += "<tag k=\"public_transport\" v=\"platform\"/>\n";
        str += "<tag k=\"public_transport\" v=\"stop_position\"/>\n";
        str += "<tag k=\"highway\" v=\"bus_stop\"/>\n";
        str += "<tag k=\"bus\" v=\"yes\"/>\n";
        str += "</node>\n";
        return str;
    }

}
