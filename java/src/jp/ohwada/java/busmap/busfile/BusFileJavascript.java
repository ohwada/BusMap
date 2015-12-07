/**
 * Bus Map
 * 2015-12-01 K.OHWADA
 */

package jp.ohwada.java.busmap.busfile;
import java.io.*;

/**
 * var makerList = [[35.145912170410156,139.10569763183594,1,"城堀"],
 * [35.157684326171875,139.11940002441406,2,"前栗場"],
 * [35.46623992919922,139.6196746826172,8466,"横浜駅西口"]];
 */

/**
 * BusFileJavascript
 */ 
public class BusFileJavascript extends BusFileCommon {

    private boolean isFirst = true;

    /**
     * constractor
     */ 
    public BusFileJavascript() {
        super(); 
    }

    /**
     * open
     * @param File dir
     * @param String name
     */ 
    public String open( File dir, String name )  {
        File file = new File( dir, name + ".data" );
        openWriter( file );
        return file.getName();
    }

    /**
     * writeHeader
     */ 
    public void writeHeader()  {
        try {
            mWriter.write("var markerList = [");
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
     * @param int id
     */
    public void writePoint( Double lat, Double lon, String name, int id ) {
        try {
            if ( !isFirst ) {
                mWriter.write(",");
                mWriter.newLine();
            }
            mWriter.write("[" + lat  + "," + lon  + "," + id  + ",\"" + name + "\"]");
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
            mWriter.write("];");
            mWriter.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
