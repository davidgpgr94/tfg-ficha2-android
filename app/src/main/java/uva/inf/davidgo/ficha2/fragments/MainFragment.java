package uva.inf.davidgo.ficha2.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import uva.inf.davidgo.ficha2.R;
import uva.inf.davidgo.ficha2.adapters.RecordAdapter;
import uva.inf.davidgo.ficha2.pojos.Record;
import uva.inf.davidgo.ficha2.pojos.RecordsContext;
import uva.inf.davidgo.ficha2.services.ApiClient;
import uva.inf.davidgo.ficha2.services.RecordService;
import uva.inf.davidgo.ficha2.utils.ServerURLs;
import uva.inf.davidgo.ficha2.utils.SharedPreferencesKeys;

public class MainFragment extends BaseFragment implements View.OnClickListener {

    private OnFragmentInteractionListener mListener;

    Button btn_quick_entry, btn_quick_exit;
    ProgressBar pb_spinner_quick;
    RecyclerView rv_records;
    RecordAdapter adapter;
    LinearLayoutManager linearLayoutManager;

    SharedPreferences prefs;

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        btn_quick_entry = view.findViewById(R.id.btn_quick_entry);
        btn_quick_exit = view.findViewById(R.id.btn_quick_exit);
        rv_records = view.findViewById(R.id.rv_records_main_fragment);

        btn_quick_entry.setOnClickListener(this);
        btn_quick_exit.setOnClickListener(this);

        pb_spinner_quick = view.findViewById(R.id.pb_spinner_quick);

        pb_spinner_quick.setVisibility(View.VISIBLE);

        // Configuracion del layoutmanager para el RecyclerView
        linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        rv_records.setLayoutManager(linearLayoutManager);


        // Obtencion de las preferencias del usuario para obtener el token
        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        adapter = new RecordAdapter(getContext(), new ArrayList<Record>());
        rv_records.setAdapter(adapter);
        setUpRecordsAndButtons();

        return view;
    }

    @Override
    protected int getNavigationItemId() {
        return R.id.nav_quick_record;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_quick_entry:
                pb_spinner_quick.setVisibility(View.VISIBLE);
                quickEntry();
                break;
            case R.id.btn_quick_exit:
                pb_spinner_quick.setVisibility(View.VISIBLE);
                quickExit();
                break;
            default:
                Log.d("MAIN_FRAGMENT", "Default case in onClick()");
        }
    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private void quickEntry() {
        pb_spinner_quick.setVisibility(View.VISIBLE);
        RecordService recordService = ApiClient.createService(RecordService.class);
        Call<Record> call = recordService.quickEntry(prefs.getString(SharedPreferencesKeys.TOKEN, ""));

        call.enqueue(new Callback<Record>() {
            @Override
            public void onResponse(Call<Record> call, Response<Record> response) {
                pb_spinner_quick.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), response.body().getEntry().toString(), Toast.LENGTH_SHORT).show();
                    pb_spinner_quick.setVisibility(View.VISIBLE);
                    setUpRecordsAndButtons();
                } else {
                    try {
                        Log.d(TAG, "MainFragment - quickEntry");
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
            public void onFailure(Call<Record> call, Throwable t) {
                pb_spinner_quick.setVisibility(View.GONE);
                Toast.makeText(getContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void quickExit() {
        pb_spinner_quick.setVisibility(View.VISIBLE);
        RecordService recordService = ApiClient.createService(RecordService.class);
        Call<Record> call = recordService.quickExit(prefs.getString(SharedPreferencesKeys.TOKEN, ""));

        call.enqueue(new Callback<Record>() {
            @Override
            public void onResponse(Call<Record> call, Response<Record> response) {
                pb_spinner_quick.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), response.body().getExit().toString(), Toast.LENGTH_SHORT).show();
                    pb_spinner_quick.setVisibility(View.VISIBLE);
                    setUpRecordsAndButtons();
                } else {
                    try {
                        Log.d(TAG, "MainFragment - quickExit");
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
            public void onFailure(Call<Record> call, Throwable t) {
                pb_spinner_quick.setVisibility(View.GONE);
                Toast.makeText(getContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setUpRecordsAndButtons() {

        RecordService recordService = ApiClient.createService(RecordService.class);

        Call<RecordsContext> call = recordService.getMyCurrentDayRecords(prefs.getString(SharedPreferencesKeys.TOKEN, ""));
        call.enqueue(new Callback<RecordsContext>() {
            @Override
            public void onResponse(Call<RecordsContext> call, Response<RecordsContext> response) {
                // pb_spinner_quick.setVisibility(View.GONE);

                if (response.isSuccessful()) {
                    List<Record> records = response.body().getRecords();

                    RecordAdapter recordAdapter = (RecordAdapter) rv_records.getAdapter();
                    recordAdapter.setData(records);
                    recordAdapter.notifyDataSetChanged();

                    setUpButtons();
                    pb_spinner_quick.setVisibility(View.GONE);
                } else {
                    pb_spinner_quick.setVisibility(View.GONE);
                    try {
                        Log.d(TAG, "MainFragment - setUpRecordsAndButtons");
                        JSONObject msg = new JSONObject(response.errorBody().string());
                        Toast.makeText(getContext(), msg.getString("message"), Toast.LENGTH_SHORT).show();
                        if (response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) onTokenNotValid();
                    } catch (JSONException e) {
                        Log.d("MAIN_GET_CATCH_JSONExc", e.getMessage());
                        e.printStackTrace();
                    } catch (IOException e) {
                        Log.d("MAIN_GET_CATCH_IOExc:", e.getMessage());
                        e.printStackTrace();
                    }
                }

            }

            @Override
            public void onFailure(Call<RecordsContext> call, Throwable t) {
                pb_spinner_quick.setVisibility(View.GONE);
                Log.d("MAIN_GET_OnFailure:", t.getLocalizedMessage());
                Toast.makeText(getContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setUpButtons() {
        RecordService recordService = ApiClient.createService(RecordService.class);

        Call<Record> call = recordService.getIncompleteRecord(prefs.getString(SharedPreferencesKeys.TOKEN, ""));
        call.enqueue(new Callback<Record>() {
            @Override
            public void onResponse(Call<Record> call, Response<Record> response) {
                pb_spinner_quick.setVisibility(View.GONE);

                if (response.isSuccessful()) {
                    if (response.code() == 204) { // Code 204=NoContent -> No hay registro incompleto
                        btn_quick_entry.setVisibility(View.VISIBLE);
                        btn_quick_exit.setVisibility(View.GONE);
                        btn_quick_exit.setEnabled(true);
                    } else {
                        btn_quick_entry.setVisibility(View.GONE);
                        btn_quick_exit.setVisibility(View.VISIBLE);
                        Record incompleteRecord = response.body();
                        Calendar cal = Calendar.getInstance();
                        cal.set(Calendar.HOUR_OF_DAY, 0);
                        cal.set(Calendar.MINUTE, 0);
                        cal.set(Calendar.SECOND, 0);
                        cal.set(Calendar.MILLISECOND, 0);
                        Date today = cal.getTime();
                        if ( incompleteRecord.getEntry().before(today) ) {
                            btn_quick_exit.setEnabled(false);
                        } else {
                            btn_quick_exit.setEnabled(true);
                        }
                    }
                } else {
                    try {
                        Log.d(TAG, "MainFragment - setUpButtons");
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
            public void onFailure(Call<Record> call, Throwable t) {
                pb_spinner_quick.setVisibility(View.GONE);
                t.getCause().printStackTrace();
                Toast.makeText(getContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
