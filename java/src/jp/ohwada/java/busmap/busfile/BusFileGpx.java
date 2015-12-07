/**
 * Bus Map
 * 2015-12-01 K.OHWADA
 */

package jp.ohwada.java.busmap.busfile;
import java.io.*;

/*
 * <?xml version="1.0" encoding="UTF-8" ?>
 * <gpx xmlns="http://www.topografix.com/GPX/1/1" version="1.1" creator="osmtracker-android" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd ">
 * <metadata>
 *   <name><![CDATA[]]></name>
 *   <desc><![CDATA[]]></desc>
 * </metadata>
 * <wpt lat="35.14591397" lon="139.10569573">
 *    <time>2015-11-23T21:53:30Z</time>
 *    <name><![CDATA[城堀]]></name>
 * </wpt>
 * </gpx>
 */

/**
 * BusFileGpx
 */ 
public class BusFileGpx extends BusFileCommon {

    /**
     * constractor
     */ 
    public BusFileGpx() {
        super(); 
    }

    /**
     * open
     * @param File dir
     * @param String name
     */ 
    public void open( File dir, String name )  {
        File file = new File( dir, name + ".gpx" );
        openWriter( file );
    }

    /**
     * writeHeader
     * @param String title
     */ 
    public void writeHeader( String title )  {
        try {
            mWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
            mWriter.newLine();
            mWriter.write("<gpx xmlns=\"http://www.topografix.com/GPX/1/1\" version=\"1.1\" creator=\"osmtracker-android\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd \">");
            mWriter.newLine();
            mWriter.write("<metadata>");
            mWriter.newLine();
            mWriter.write("<name><![CDATA[" + title + "]]></name>");
            mWriter.newLine();
            mWriter.write("<desc><![CDATA[国土数値情報に基づく]]></desc>");
            mWriter.newLine();
            mWriter.write("</metadata>");
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
     * @param String time
     */ 
    public void writePoint( Double lat, Double lon, String name, String time ) {
        try {
            mWriter.write("<wpt lat=\""+ lat +"\" lon=\""+ lon +"\">\n");
            mWriter.write(" <time>"+ time +"Z</time>\n");
            mWriter.write(" <name><![CDATA["+ name +"]]></name>\n");
            mWriter.write("</wpt>\n");
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
            mWriter.write("</gpx>");
            mWriter.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
