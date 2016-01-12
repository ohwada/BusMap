/**
 * Bus Map
 * 2015-12-01 K.OHWADA
 */
package jp.ohwada.andorid.busmap.util;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * NodeRecord
 */
public class NodeRecord 
    implements Parcelable {

    public int id = 0;
    public double lat = 0;
    public double lon = 0;
    public String name = "";
    public String pref = "";
    public int[] route_ids = new int[0];

    /**
     * Constructor
     */
    public NodeRecord( int _id, double _lat, double _lon, String _name, String _pref, int[] _array ) {
        id = _id;
        lat = _lat;
        lon = _lon;
        name = _name;
        pref = _pref;
        route_ids = _array;
    }

    /**
     * Constructor
     */
   private NodeRecord(Parcel in) { 
        id = in.readInt();  
        lat = in.readDouble();
        lon = in.readDouble();    
        name = in.readString();
        pref = in.readString(); 
        in.readIntArray(route_ids);
     }

    /**
     * writeToParcel
     * @param Parcel out
     * @param int flags  
     */
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(id);  
        out.writeDouble(lat);
        out.writeDouble(lon);    
        out.writeString(name);
        out.writeString(pref); 
        out.writeIntArray(route_ids);   
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
    public static final Parcelable.Creator<NodeRecord> CREATOR  
        = new Parcelable.Creator<NodeRecord>() { 
        /**
         * createFromParcel
         * @param Parcel in
         * @return NodeRecord
         */  
        public NodeRecord createFromParcel(Parcel in) {  
            return new NodeRecord(in);  
        }
        /**
         * newArray
         * @param int size
         * @return NodeRecord[]
         */   
        public NodeRecord[] newArray(int size) {  
            return new NodeRecord[size];  
        }  
    };

}
