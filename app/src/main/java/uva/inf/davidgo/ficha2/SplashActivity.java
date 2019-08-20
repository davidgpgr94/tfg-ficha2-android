package uva.inf.davidgo.ficha2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import uva.inf.davidgo.ficha2.utils.SharedPreferencesKeys;

public class SplashActivity extends AppCompatActivity {
    SharedPreferences prefs;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (prefs.contains(SharedPreferencesKeys.TOKEN)) {
            // Load MainActivity
            startActivity(new Intent(this, MainActivity.class));
        } else {
            // Load LoginActivity
            startActivity(new Intent(this, LoginActivity.class));
        }

        finish();
    }
}
