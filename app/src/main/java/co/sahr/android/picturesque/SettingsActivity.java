/*
 * Copyright (C) Habib Rehman (git.io/HR) 2018
 */

package co.sahr.android.picturesque;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import co.sahr.android.picturesque.utilities.Utils;


public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Utils.setupToolbar(this, null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Go back to the previous activity
                finish();
                return true;
            default:
                // let Android handle the menu click
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    //    protected boolean isValidFragment(String fragmentName) {
    //        return PreferenceFragment.class.getName().equals(fragmentName)
    //                || GeneralPreferenceFragment.class.getName().equals(fragmentName);
    //    }
}
