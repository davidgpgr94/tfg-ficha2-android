package uva.inf.davidgo.ficha2.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


import uva.inf.davidgo.ficha2.R;

/**
 * A simple {@link Fragment} subclass.
 * SettingsFragment
 */
public class SettingsFragment extends BaseFragment implements View.OnClickListener {


    Button btnChangePassword;


    public SettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        btnChangePassword = view.findViewById(R.id.settings_password);
        btnChangePassword.setOnClickListener(this);

        return view;
    }

    @Override
    protected int getNavigationItemId() {
        return R.id.nav_settings;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.settings_password:
                changePassword();
                break;
        }
    }

    private void changePassword() {
        Fragment changePasswordFragment = new ChangePasswordFragment();
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        if (fragmentManager != null) {
            fragmentManager.beginTransaction().replace(R.id.content_main, changePasswordFragment).addToBackStack("settings").commit();
        }
    }
}
