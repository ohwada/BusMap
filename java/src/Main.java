/**
 * Bus Map
 * 2015-12-01 K.OHWADA
 */

import jp.ohwada.java.busmap.BusMain;

/**
 * Main
 */
public class Main {
    /**
     * main
     */
    public static void main(String[] args) {
        BusMain bus  = new BusMain();
        bus.initDb( Config.DB_HOST, Config.DB_PORT, Config.DB_NAME, Config.DB_USER, Config.DB_PASS );
        bus.setDubugMsg( false );
        bus.setCeateFlag( false );
        bus.setBusRouteFlag( false  );
        bus.setBusStopFlag( false );
        bus.setCountPrefFlag( false );
        bus.setCountWholeFlag( false );
        bus.setFileFlag( false );
        bus.execute();
    }
}
