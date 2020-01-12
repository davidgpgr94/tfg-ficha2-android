package uva.inf.davidgo.ficha2.fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;


import uva.inf.davidgo.ficha2.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends BaseFragment implements NavigationView.OnNavigationItemSelectedListener {

    NavigationView navigationView;


    public SettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        navigationView = view.findViewById(R.id.settings_nav);
        navigationView.setNavigationItemSelectedListener(this);

        return view;
    }

    @Override
    protected int getNavigationItemId() {
        return R.id.nav_settings;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        Fragment myFragment = null;

        boolean fragmentSelected = false;

        if (id == R.id.settings_password) {
            myFragment = new ChangePasswordFragment();
            fragmentSelected = true;
        }

        if (fragmentSelected) {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            if (fragmentManager != null) {
                fragmentManager.beginTransaction().replace(R.id.content_main, myFragment).addToBackStack("settings").commit();
            }
        }
        return false;
    }
}
