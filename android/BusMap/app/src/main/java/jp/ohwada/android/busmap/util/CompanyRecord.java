/**
 * Bus Map
 * 2015-12-01 K.OHWADA
 */
package jp.ohwada.android.busmap.util;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * CompanyRecord
 */
public class CompanyRecord 
    implements Parcelable {

    public int id = 0;
    public String name = "";
    public String url_home = "";
    public String url_search = "";

    /**
     * Constructor
     */
    public CompanyRecord( int _id, String _name, String _url_home, String _url_search ) {
        id = _id;
        name = _name;
        url_home = _url_home;
        url_search = _url_search;
    }

    /**
     * Constructor
     */
   private CompanyRecord(Parcel in) {
        id = in.readInt();     
        name = in.readString();
        url_home = in.readString(); 
        url_search = in.readString(); 
     }

    /**
     * writeToParcel
     * @param Parcel out
     * @param int flags  
     */
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(id);      
        out.writeString(name);
        out.writeString(url_home); 
        out.writeString(url_search); 
    }

    /**
     * describeContents
     * @return int
     */
    public int describeContents() {  
        return 0;  
    } 

    /**
     * --- Parcelable.Creator ---
     */
    public static final Parcelable.Creator<CompanyRecord> CREATOR
        = new Parcelable.Creator<CompanyRecord>() {
        /**
         * createFromParcel
         * @param Parcel in
         * @return CompanyRecord
         */  
        public CompanyRecord createFromParcel(Parcel in) {  
            return new CompanyRecord(in);  
        } 
        /**
         * newArray
         * @param int size
         * @return CompanyRecord[]
         */   
        public CompanyRecord[] newArray(int size) {  
            return new CompanyRecord[size];  
        }  
    };

}
