/**
 * Bus Map
 * 2015-12-01 K.OHWADA
 */

package jp.ohwada.java.busmap.busfile;

import java.io.*;

/**
 * BusFileCommon
 */ 
public class BusFileCommon {

    protected BufferedWriter mWriter = null;

    /**
     * constractor
     */ 
    public BusFileCommon() {
        // 
    }

    /**
     * openWriter
     * @param File file
     */ 
    protected void openWriter( File file )  {
        try {
            mWriter = new BufferedWriter(
                new OutputStreamWriter(
                    new FileOutputStream(file), 
                "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * close
     */ 
    public void close()  {
        closeWriter();
    }

    private void closeWriter()  {
        try {
            mWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
