/**
 * Bus Map
 * 2015-12-01 K.OHWADA
 */
package jp.ohwada.android.busmap.util;

import java.util.List;

/**
 * CurveRecord
 */
public class CurveRecord {
    public int id = 0;
    public List<String> curves;

    /**
     * Constructor
     */
    public CurveRecord( int _id, List<String> _curves ) {
        id = _id;
        curves = _curves;
    }
}
