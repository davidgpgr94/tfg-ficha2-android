package uva.inf.davidgo.ficha2.fragments;


import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uva.inf.davidgo.ficha2.R;
import uva.inf.davidgo.ficha2.services.ApiClient;
import uva.inf.davidgo.ficha2.services.EmployeeService;
import uva.inf.davidgo.ficha2.utils.SharedPreferencesKeys;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChangePasswordFragment extends BaseFragment implements View.OnClickListener {
    String TAG = this.getClass().getSimpleName();

    TextInputEditText etOldPassword, etNewPassword, etRepeatPassword;
    TextInputLayout tilOldPassworad, tilNewPassword, tilRepeatPassword;
    Button btnSend;
    ProgressBar progressBar;






    public ChangePasswordFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_change_password, container, false);

        etOldPassword = view.findViewById(R.id.et_old_password);
        etNewPassword = view.findViewById(R.id.et_new_password);
        etRepeatPassword = view.findViewById(R.id.et_repeat_password);
        tilOldPassworad = view.findViewById(R.id.til_old_password);
        tilNewPassword = view.findViewById(R.id.til_new_password);
        tilRepeatPassword = view.findViewById(R.id.til_repeat_password);
        progressBar = view.findViewById(R.id.pb_change_password);
        btnSend = view.findViewById(R.id.btn_send);

        btnSend.setOnClickListener(this);

        return view;
    }

    @Override
    protected int getNavigationItemId() {
        return R.id.nav_settings;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_send:
                changePassword();
                break;
        }
    }

    private void changePassword() {
        if (isInputOk()) {
            progressBar.setVisibility(View.VISIBLE);

            String jwt = prefs.getString(SharedPreferencesKeys.TOKEN, "");
            String oldPassword = etOldPassword.getText().toString();
            String newPassword = etNewPassword.getText().toString();
            String repeatPassword = etRepeatPassword.getText().toString();

            EmployeeService employeeService = ApiClient.createService(EmployeeService.class);
            Call<ResponseBody> call = employeeService.changePassword(jwt, oldPassword, newPassword, repeatPassword);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    progressBar.setVisibility(View.GONE);
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "Inicie sesión con la nueva contraseña", Toast.LENGTH_LONG).show();
                        onTokenNotValid();
                    } else {
                        try {
                            if (response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) onTokenNotValid();
                            JSONObject msg = new JSONObject(response.errorBody().string());
                            String strMsg = msg.getString("message");
                            tilOldPassworad.setError(strMsg);
                            tilNewPassword.setError(strMsg);
                            tilRepeatPassword.setError(strMsg);
                            Toast.makeText(getContext(), strMsg, Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    if (t instanceof java.net.SocketTimeoutException) {
                        Toast.makeText(getContext(), "Se ha perdido la conexión con el servidor", Toast.LENGTH_LONG).show();
                    }
                    onTokenNotValid();
                    progressBar.setVisibility(View.GONE);
                }
            });

        }
    }

    private boolean isInputOk() {
        boolean ok = true;
        Editable newPassword = etNewPassword.getText();
        Editable repeatPassword = etRepeatPassword.getText();
        Editable oldPassword = etOldPassword.getText();
        if (TextUtils.isEmpty(newPassword) ) {
            tilNewPassword.setError("Campo obligatorio");
            ok = false;
        }
        if (TextUtils.isEmpty(repeatPassword)) {
            tilRepeatPassword.setError("Campo obligatorio");
            ok = false;
        }
        if (!TextUtils.equals(newPassword, repeatPassword)) {
            tilNewPassword.setError("Las contraseñas deben coincidir");
            tilRepeatPassword.setError("Las contraseñas deben coincidir");
            ok = false;
        }
        if (TextUtils.equals(newPassword, oldPassword)) {
            tilNewPassword.setError("La nueva contraseña debe ser distinta a la actual");
            ok = false;
        }
        return ok;
    }
}
