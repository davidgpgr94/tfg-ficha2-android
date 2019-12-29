package uva.inf.davidgo.ficha2.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import uva.inf.davidgo.ficha2.LoginActivity;

abstract public class BaseFragment extends Fragment {

    protected String TAG = this.getClass().getSimpleName();

    SharedPreferences prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        return null;
    }

    protected void onTokenNotValid() {
        prefs.edit().clear().apply();
        startActivity(new Intent(getContext(), LoginActivity.class));
        getActivity().finish();
    }

}
