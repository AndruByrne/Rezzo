package com.andrubyrne.rezzo;
import android.preference.*;
import android.os.*;

public class UserSettings extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
