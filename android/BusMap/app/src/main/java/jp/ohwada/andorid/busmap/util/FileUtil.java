/**
 * Bus Map
 * 2015-12-01 K.OHWADA
 */
package jp.ohwada.andorid.busmap.util;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import jp.ohwada.andorid.busmap.Constant;

/*
 * FileUti
 */
public class FileUtil {

   // debug
    private static final String TAG_SUB = FileUtil.class.getSimpleName();

    // read
    private static final int TMP_BUF_SIZE = 1024;
    private static final int BUF_OFFSET = 0;
    private static final int EOF = -1;

    // write
    private static final boolean APPEND = false;

    private String mSubDir = "";

    /**
     * === Constractor ===
     * @param String dir
     */
    public FileUtil(String dir) {
        mSubDir = dir;
    }

    /**
     * makeSubDir
     */ 
    public void makeSubDir() {
        // make dir if not exists
        File dir = new File( getDir() );
        if ( !dir.exists() ) { 
            dir.mkdir();
        }
    }

    /**
     * getFile
     */ 
    public File getFile( String name ) {
        String path = getDir() + File.separator + name;
        File file = new File( path );
        return file;
    }

    /**
     * write
     */ 
    public void write( File file, String data ) {
        FileOutputStream os = null;
        try {
            os = new FileOutputStream( file, APPEND );
            os.write( data.getBytes() );
        } catch ( FileNotFoundException e ) {
            if (Constant.DEBUG) e.printStackTrace();
        } catch ( IOException e ) {
            if (Constant.DEBUG) e.printStackTrace();
        }
        if ( os != null ) {
            try {
                os.close();
            } catch ( IOException e ) {
                if (Constant.DEBUG) e.printStackTrace();
            }
        }
    }

    /**
     * read
     * @param File file
     * @return String 
     */ 
    public String read( File file ) {
        FileInputStream is = null;
        String str = "";
        try {
            is = new FileInputStream( file );
            str = readText( is );
        } catch (IOException e) {
            if (Constant.DEBUG) e.printStackTrace();
        }
        if ( is != null ) {
            try {
                is.close();
            } catch ( IOException e ) {
                if (Constant.DEBUG) e.printStackTrace();
            }
        }
        return str;
    }

    /**
     * readText
     */
    private String readText( InputStream is ) {
        String str = "";
        try {
            byte[] bytes = readBinary( is );
            str = new String( bytes, "UTF-8" );
        } catch (IOException e) {
            e.printStackTrace();
        }
        return str;
    }

    /**
     * readBinary
     */
    private byte[] readBinary( InputStream is ) {
        BufferedInputStream bis = null;
        ByteArrayOutputStream baos = null;
        int len;
        byte[] data = new byte[ TMP_BUF_SIZE ];  
        byte[] bytes = new byte[0];
        try {
            bis = new BufferedInputStream( is );
            baos = new ByteArrayOutputStream();  
            while ( ( len = bis.read(data) ) != EOF ) {
                baos.write( data, BUF_OFFSET, len );  
            }
            bytes = baos.toByteArray();
        } catch(Exception e) {
            if (Constant.DEBUG) e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (bis != null) {
                    bis.close();
                }
                if (baos != null) {
                    baos.close();
                }
            } catch (Exception e) {
                if (Constant.DEBUG) e.printStackTrace();
            }
        }
        return bytes;
    }

    /**
     * isExpired
     * @param File file
     * @param long expire
     * @return boolean : true = expired
     */
    public boolean isExpired( File file, long expire ) {
        // if not exists
        if ( !file.exists() ) return true;
        // if expired
        if (( expire > 0 )&&( System.currentTimeMillis() > ( file.lastModified() + expire ) )) return true;
        return false;
    }

    /**
     * get dirctory
     */ 
    private String getDir() {
        String path = Environment.getExternalStorageDirectory().getPath();
        path += File.separator + mSubDir;
        return path;
    }

    /**
     * clear all files
     */
    public void clearAllFiles() {
        // get file list
        File dir = new File( getDir() );
        File[] files = dir.listFiles();
        // delete all file 
        for ( int i=0; i<files.length; i++ ) {
            File f = files[ i ];
            if ( f != null ) {
                f.delete();
            }
        }
    }

    /**
     * clear old files
     * @param int max_files
     */
    public void clearOldFiles( int max_files ) {
        // get file list
        File dir = new File( getDir() );
        File[] files = dir.listFiles();
        if ( files.length <= max_files ) return;
        // create file list
        List<File> list = new ArrayList<File>();
        for ( int i=0; i<files.length; i++ ) {
            File f = files[ i ];
            if ( f != null ) {
	        list.add( f );
            }
        }
        int size = list.size();
        if ( size <= max_files ) return;
        // sort by lastModified
        Collections.sort(list, new FileComparator());
        // delete file 
        for ( int i=max_files; i<size; i++ ) {
            File f = list.get( i );
            if ( f != null ) {
                f.delete();
            }
        }
        int del = size - max_files;
        log_d("clearOldFiles deleted " + del );
    }

    /**
     * log_d
     */
    private void log_d( String str ) {
        if (Constant.DEBUG) Log.d( Constant.TAG, TAG_SUB + " " + str );
    }

    /**
     * --- class FileComparator ---
     */	
    private class FileComparator implements Comparator<File> {
        /**
         * Constractor
         */
        public FileComparator() {
            // dummy
        }

        /**
         * compare: sort in new order by lastModified
         */
        @Override
        public int compare( File file0, File file1 ) {
            long time0 = file0.lastModified();
            long time1 = file1.lastModified();
            if ( time0 >  time1 ) {
                return -1;
            } else if ( time0 == time1 ) {
                return 0;
            } else {
                return 1;
            }
        }
    }

}