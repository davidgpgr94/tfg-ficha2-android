package uva.inf.davidgo.ficha2.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import uva.inf.davidgo.ficha2.R;
import uva.inf.davidgo.ficha2.pojos.Record;
import uva.inf.davidgo.ficha2.services.RecordService;
import uva.inf.davidgo.ficha2.utils.ServerURLs;
import uva.inf.davidgo.ficha2.utils.SharedPreferencesKeys;


public class MainFragment extends Fragment implements View.OnClickListener {

    private OnFragmentInteractionListener mListener;

    Button btn_quick_entry, btn_quick_exit;
    ProgressBar pb_spinner_quick;

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

        btn_quick_entry.setOnClickListener(this);
        btn_quick_exit.setOnClickListener(this);

        pb_spinner_quick = view.findViewById(R.id.pb_spinner_quick);

        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        return view;
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
                quickEntry();
                break;
            case R.id.btn_quick_exit:
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
        Retrofit retrofit = new Retrofit.Builder().baseUrl(ServerURLs.ROOT_URL).addConverterFactory(GsonConverterFactory.create()).build();
        RecordService recordService = retrofit.create(RecordService.class);
        Call<Record> call = recordService.quick_entry(prefs.getString(SharedPreferencesKeys.TOKEN, ""));

        call.enqueue(new Callback<Record>() {
            @Override
            public void onResponse(Call<Record> call, Response<Record> response) {
                pb_spinner_quick.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), response.body().getEntry().toString(), Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        JSONObject msg = new JSONObject(response.errorBody().string());
                        Toast.makeText(getContext(), msg.getString("message"), Toast.LENGTH_SHORT).show();
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
        Retrofit retrofit = new Retrofit.Builder().baseUrl(ServerURLs.ROOT_URL).addConverterFactory(GsonConverterFactory.create()).build();
        RecordService recordService = retrofit.create(RecordService.class);
        Call<Record> call = recordService.quick_exit(prefs.getString(SharedPreferencesKeys.TOKEN, ""));

        call.enqueue(new Callback<Record>() {
            @Override
            public void onResponse(Call<Record> call, Response<Record> response) {
                pb_spinner_quick.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), response.body().getExit().toString(), Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        JSONObject msg = new JSONObject(response.errorBody().string());
                        Toast.makeText(getContext(), msg.getString("message"), Toast.LENGTH_SHORT).show();
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
}
