/**
 * Bus Map
 * 2015-12-01 K.OHWADA
 */

package jp.ohwada.java.busmap.busfile;
import java.io.*;

/*
 * <?xml version="1.0" encoding="UTF-8"?>
 * <kml xmlns="http://www.opengis.net/kml/2.2">
 *  <Document>
 *    <name><![CDATA[]]></name>
 *    <Folder>
 *      <name>Placemarks</name>
 *      <Placemark>
 *        <name>Simple placemark</name>
 *        <description>Attached to the ground. Intelligently places itself at the height of the
 *          underlying terrain.</description>
 *        <Point>
 *          <coordinates>-122.0822035425683,37.42228990140251,0</coordinates>
 *       </Point>
 *      </Placemark>
 *    </Folder>
 *  </Document>
 * </kml>
 */

/**
 * BusFileKml
 */ 
public class BusFileKml extends BusFileCommon {

    /**
     * constractor
     */ 
    public BusFileKml() {
        super(); 
    }

    /**
     * open
     * @param File dir
     * @param String name
     */ 
    public String open( File dir,  String name )  {
        File file = new File( dir, name  +".kml");
        openWriter( file );
        return file.getName();
    }

    /**
     * writeHeader
     * @param String title
     */
    public void writeHeader( String title )  {
        try {
            mWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            mWriter.newLine();
            mWriter.write("<kml xmlns=\"http://www.opengis.net/kml/2.2\">");
            mWriter.newLine();
            mWriter.write("<Document>");
            mWriter.newLine();
            mWriter.write("<name><![CDATA[" + title +"]]></name>");
            mWriter.newLine();
            mWriter.write("<Folder>");
            mWriter.newLine();
            mWriter.write("<name>Placemarks</name>");
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
     */ 
    public void writePoint( Double lat, Double lon, String name ) {
        try {
            mWriter.write("<Placemark>");
            mWriter.newLine();
            mWriter.write("<name>" + name + "</name>");
            mWriter.newLine();
            mWriter.write("<description></description>");
            mWriter.newLine();
            mWriter.write("<Point>");
            mWriter.newLine();
            mWriter.write("<coordinates>");
            mWriter.write( lon + "," + lat + ",0" );
            mWriter.write("</coordinates>");
            mWriter.newLine();
            mWriter.write("</Point>");
            mWriter.newLine();
            mWriter.write("</Placemark>");
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
            mWriter.write("</Folder>");
            mWriter.newLine();
            mWriter.write("</Document>");
            mWriter.newLine();
            mWriter.write("</kml>");
            mWriter.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
