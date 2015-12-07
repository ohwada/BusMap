/**
 * Bus Map
 * 2015-12-01 K.OHWADA
 */

package jp.ohwada.java.busmap.busfile;
import java.io.*;

/**
 * var data = { "count": 8466,
 * "items": [
 * {"name": "城堀", "lon": 139.10569763183594, "lat": 35.145912170410156},
 * {"name": "前栗場", "lon": 139.11940002441406, "lat": 35.157684326171875},
 * {"name": "横浜駅西口", "lon": 139.6196746826172, "lat": 35.46623992919922}
 * ]}
 */

/**
 * BusFileJson
 */ 
public class BusFileJson extends BusFileCommon {

    private boolean isFirst = true;

    /**
     * constractor
     */ 
    public BusFileJson() {
        super(); 
    }

    /**
     * open
     * @param File dir
     * @param String name
     */ 
    public String open( File dir, String name )  {
        File file = new File( dir, name + ".json" );
        openWriter( file );
        return file.getName();
    }

    /**
     * writeHeader
     * @param int count
     */ 
    public void writeHeader( int count )  {
        try {
            mWriter.write("var data = { \"count\": "+ count + ",");
            mWriter.newLine();
            mWriter.write( "\"items\": [");
            mWriter.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        isFirst = true;
    }

    /**
     * writePoint
     * @param Double lat
     * @param Double lon
     * @param String name
     */ 
    public void writePoint( Double lat, Double lon, String name ) {
        try {
            if ( !isFirst ) {
                mWriter.write(",");
                mWriter.newLine();
            }
            mWriter.write("{");
            mWriter.write("\"name\": \"" + name + "\", ");
            mWriter.write("\"lon\": " + lon + ", ");
            mWriter.write("\"lat\": " + lat);
            mWriter.write("}");
            mWriter.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        isFirst = false;
    }

    /**
     * writeFooter
     */ 
    public void writeFooter()  {
        try {
            mWriter.write("]}");
            mWriter.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
