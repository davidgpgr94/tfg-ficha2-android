package uva.inf.davidgo.ficha2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import uva.inf.davidgo.ficha2.pojos.LoginResponse;
import uva.inf.davidgo.ficha2.services.LoginService;
import uva.inf.davidgo.ficha2.utils.ServerURLs;
import uva.inf.davidgo.ficha2.utils.SharedPreferencesKeys;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    Button btn_login;
    EditText et_user, et_password;

    ProgressBar pb_spinner;

    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btn_login = findViewById(R.id.btn_login);
        et_user = findViewById(R.id.et_user);
        et_password = findViewById(R.id.et_password);

        pb_spinner = findViewById(R.id.pb_spinner_login);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        btn_login.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                login();
                break;
        }
    }

    private void login() {
        pb_spinner.setVisibility(View.VISIBLE);

        Retrofit retrofit = new Retrofit.Builder().baseUrl(ServerURLs.ROOT_URL).addConverterFactory(GsonConverterFactory.create()).build();
        LoginService loginService = retrofit.create(LoginService.class);

        Call<LoginResponse> call;

        String login, password;
        login = et_user.getText().toString();
        password = et_password.getText().toString();

        call = loginService.login(login, password);
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                pb_spinner.setVisibility(View.GONE);

                if (response.isSuccessful()) {
                    SharedPreferences.Editor editor = prefs.edit();

                    editor.putString(SharedPreferencesKeys.TOKEN, response.body().getToken());
                    editor.putString(SharedPreferencesKeys.USER_NAME, response.body().getEmployee().getName());
                    editor.putString(SharedPreferencesKeys.USER_SURNAME, response.body().getEmployee().getSurname());
                    editor.putString(SharedPreferencesKeys.USER_LOGIN, response.body().getEmployee().getLogin());
                    editor.putBoolean(SharedPreferencesKeys.USER_IS_ADMIN, response.body().getEmployee().isIs_admin());
                    editor.apply();

                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finish();
                } else {
                    try {
                        JSONObject msg = new JSONObject(response.errorBody().string());
                        Toast.makeText(LoginActivity.this, msg.getString("message"), Toast.LENGTH_LONG).show();
                    } catch (JSONException e) {
                        Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                pb_spinner.setVisibility(View.GONE);
                Toast.makeText(LoginActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
