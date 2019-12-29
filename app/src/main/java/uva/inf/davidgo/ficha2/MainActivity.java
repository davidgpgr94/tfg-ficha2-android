package uva.inf.davidgo.ficha2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import uva.inf.davidgo.ficha2.fragments.ExpandableRecordsFragment;
import uva.inf.davidgo.ficha2.fragments.MainFragment;
import uva.inf.davidgo.ficha2.fragments.ManualFragment;
import uva.inf.davidgo.ficha2.fragments.RecordsFragment;
import uva.inf.davidgo.ficha2.utils.SharedPreferencesKeys;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, MainFragment.OnFragmentInteractionListener, ManualFragment.OnFragmentInteractionListener {

    NavigationView navigationView;

    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().findItem(R.id.nav_quick_record).setChecked(true);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (!prefs.contains(SharedPreferencesKeys.TOKEN)) {
            startActivity(new Intent(this, LoginActivity.class));
            Toast.makeText(this, "La sesión se ha cerrado.", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Fragment main_fragment = new MainFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.content_main, main_fragment).commit();
            TextView tv_header_main_user = navigationView.getHeaderView(0).findViewById(R.id.tv_header_main_user);
            TextView tv_header_main_is_admin = navigationView.getHeaderView(0).findViewById(R.id.tv_header_main_is_admin);
            String nombre = prefs.getString(SharedPreferencesKeys.USER_NAME, "--");
            String apellidos = prefs.getString(SharedPreferencesKeys.USER_SURNAME, "--");
            String completo = nombre.substring(0, 1).toUpperCase() + nombre.substring(1) + " " + apellidos.substring(0, 1).toUpperCase() + apellidos.substring(1);
            tv_header_main_user.setText(completo);
            if (prefs.getBoolean(SharedPreferencesKeys.USER_IS_ADMIN, false)) {
                tv_header_main_is_admin.setText("Administrador");
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            logout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        if (prefs.contains(SharedPreferencesKeys.TOKEN)) {
            prefs.edit().clear().commit();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        Fragment myFragment = null;

        boolean fragmentSelected = false;

        if (id == R.id.nav_quick_record) {
            myFragment = new MainFragment();
            fragmentSelected = true;
        } else if (id == R.id.nav_manual_record) {
            myFragment = new ManualFragment();
            fragmentSelected = true;
        } else if (id == R.id.nav_my_records) {
            myFragment = new ExpandableRecordsFragment();
            fragmentSelected = true;
        }

        if (fragmentSelected) {
            getSupportFragmentManager().beginTransaction().replace(R.id.content_main, myFragment).commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

}
