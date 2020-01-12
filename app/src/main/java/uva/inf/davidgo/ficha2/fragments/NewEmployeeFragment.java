package uva.inf.davidgo.ficha2.fragments;


import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uva.inf.davidgo.ficha2.R;
import uva.inf.davidgo.ficha2.pojos.Employee;
import uva.inf.davidgo.ficha2.services.ApiClient;
import uva.inf.davidgo.ficha2.services.EmployeeService;
import uva.inf.davidgo.ficha2.utils.SharedPreferencesKeys;


public class NewEmployeeFragment extends BaseFragment implements View.OnClickListener {
    String TAG = this.getClass().getSimpleName();

    TextInputEditText etName, etSurname, etUser, etPassword;
    CheckBox cbIsAdmin;
    Button btnSend;
    ProgressBar progressBar;


    public NewEmployeeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_new_employee, container, false);

        etName = view.findViewById(R.id.et_name);
        etSurname = view.findViewById(R.id.et_surname);
        etUser = view.findViewById(R.id.et_user);
        etPassword = view.findViewById(R.id.et_password);
        cbIsAdmin = view.findViewById(R.id.cb_is_admin);
        btnSend = view.findViewById(R.id.btn_send);
        progressBar = view.findViewById(R.id.pb_new_employee);

        btnSend.setOnClickListener(this);

        return view;
    }

    @Override
    protected int getNavigationItemId() {
        return R.id.nav_new_user;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_send:
                createEmployee();
                break;
        }
    }

    private void createEmployee() {
        Log.d(TAG, "createEmployee - Antes del if(isInputOk())");
        if (isInputOk()) {
            progressBar.setVisibility(View.VISIBLE);

            String jwt = prefs.getString(SharedPreferencesKeys.TOKEN, "");
            String name = etName.getText().toString();
            String surname = etSurname.getText().toString();
            String user = etUser.getText().toString();
            String password = etPassword.getText().toString();
            boolean isAdmin = cbIsAdmin.isChecked();
            EmployeeService employeeService = ApiClient.createService(EmployeeService.class);
            Call<Employee> call = employeeService.createEmployee(jwt, name, surname, user, password, isAdmin);
            call.enqueue(new Callback<Employee>() {
                @Override
                public void onResponse(Call<Employee> call, Response<Employee> response) {
                    progressBar.setVisibility(View.GONE);
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "Empleado añadido satisfactoriamente", Toast.LENGTH_SHORT).show();
                        etName.getText().clear();
                        etSurname.getText().clear();
                        etUser.getText().clear();
                        etPassword.getText().clear();
                        cbIsAdmin.setChecked(false);
                    } else {
                        try {
                            JSONObject msg = new JSONObject(response.errorBody().string());
                            Toast.makeText(getContext(), msg.getString("message"), Toast.LENGTH_SHORT).show();
                            if (response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) onTokenNotValid();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(Call<Employee> call, Throwable t) {
                    progressBar.setVisibility(View.GONE);
                }
            });
        }
    }

    private boolean isInputOk() {
        boolean ok = true;

        if (TextUtils.isEmpty(etName.getText())) {
            etName.setError("El nombre es obligatorio");
            ok = false;
        }
        if (TextUtils.isEmpty(etSurname.getText())) {
            etSurname.setError("Los apellidos son obligatorios");
            ok = false;
        }
        if (TextUtils.isEmpty(etUser.getText())) {
            etUser.setError("El usuario es obligatorio");
            ok = false;
        }
        if (TextUtils.isEmpty(etPassword.getText())) {
            etPassword.setError("La contraseña es obligatoria");
            ok = false;
        }
        return ok;
    }
}
