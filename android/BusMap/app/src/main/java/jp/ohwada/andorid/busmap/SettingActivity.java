/**
 * Bus Map
 * 2015-12-01 K.OHWADA
 */
package jp.ohwada.andorid.busmap;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

/**
 * SettingActivity
 */
public class SettingActivity extends PreferenceActivity {
    /**
     * === onCreate ===
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager()
            .beginTransaction()
            .replace(android.R.id.content, new SettingFragment())
            .commit();
    }

    /**
     * --- class SettingFragment ---
     * 
     * This fragment class should be public and static 
     */
    public static class SettingFragment extends PreferenceFragment {
        /**
         * === onCreate ===
         */	
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preference);
        }
    }

}
