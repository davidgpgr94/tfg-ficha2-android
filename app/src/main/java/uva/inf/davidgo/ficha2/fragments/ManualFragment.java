package uva.inf.davidgo.ficha2.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

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


public class ManualFragment extends Fragment implements View.OnClickListener, View.OnFocusChangeListener, CompoundButton.OnCheckedChangeListener {
    private static final String CERO = "0";
    private static final String BARRA = "/";
    private static final String DOS_PUNTOS = ":";

    // Calendario para obtener fecha y hora
    public final Calendar cal = Calendar.getInstance();



    // Variables para obtener la fecha
    final int mes = cal.get(Calendar.MONTH);
    final int dia = cal.get(Calendar.DAY_OF_MONTH);
    final int anio = cal.get(Calendar.YEAR);

    // Variables para obtener la hora
    final int hora = cal.get(Calendar.HOUR_OF_DAY);
    final int minuto = cal.get(Calendar.MINUTE);

    EditText et_date, et_time_entry, et_time_exit;
    TextView tv_date, tv_time_entry, tv_time_exit;
    Button btn_send;
    TextView tv_timezone_entry, tv_timezone_exit;
    ImageView iv_clear_date, iv_clear_entry, iv_clear_exit;

    Switch sw_completed_record;
    TextView tv_incompleted_record_date, tv_incompleted_record_entry, tv_incompleted_record_exit;

    ProgressBar pb_spinner_manual;

    SharedPreferences prefs;

    Record incompletedRecord = null;

    private OnFragmentInteractionListener mListener;
    public ManualFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_manual, container, false);

        cal.setTimeZone(TimeZone.getDefault());

        et_date = view.findViewById(R.id.et_date);
        et_time_entry = view.findViewById(R.id.et_time_entry);
        et_time_exit = view.findViewById(R.id.et_time_exit);

        // Quitar el teclado para que solo salga los picker
        et_date.setInputType(InputType.TYPE_NULL);
        et_time_entry.setInputType(InputType.TYPE_NULL);
        et_time_exit.setInputType(InputType.TYPE_NULL);

        tv_date = view.findViewById(R.id.tv_date);
        tv_time_entry = view.findViewById(R.id.tv_time_entry);
        tv_time_exit = view.findViewById(R.id.tv_time_exit);

        btn_send = view.findViewById(R.id.btn_send);

        et_date.setOnFocusChangeListener(this);
        et_time_entry.setOnFocusChangeListener(this);
        et_time_exit.setOnFocusChangeListener(this);

        tv_date.setOnClickListener(this);
        tv_time_entry.setOnClickListener(this);
        tv_time_exit.setOnClickListener(this);
        et_date.setOnClickListener(this);
        et_time_entry.setOnClickListener(this);
        et_time_exit.setOnClickListener(this);
        btn_send.setOnClickListener(this);

        et_date.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    iv_clear_date.setVisibility(View.VISIBLE);
                } else {
                    iv_clear_date.setVisibility(View.GONE);
                }
            }
        });
        et_time_entry.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    iv_clear_entry.setVisibility(View.VISIBLE);
                } else {
                    iv_clear_entry.setVisibility(View.GONE);
                }
            }
        });
        et_time_exit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    iv_clear_exit.setVisibility(View.VISIBLE);
                } else {
                    iv_clear_exit.setVisibility(View.GONE);
                }
            }
        });

        tv_timezone_entry = view.findViewById(R.id.tv_timezone_entry);
        tv_timezone_exit = view.findViewById(R.id.tv_timezone_exit);

        iv_clear_date = view.findViewById(R.id.iv_clear_date);
        iv_clear_entry = view.findViewById(R.id.iv_clear_entry);
        iv_clear_exit = view.findViewById(R.id.iv_clear_exit);

        iv_clear_date.setOnClickListener(this);
        iv_clear_entry.setOnClickListener(this);
        iv_clear_exit.setOnClickListener(this);

        // Setup TimeZone
        SimpleDateFormat sdf_time_zone = new SimpleDateFormat("z");

        tv_timezone_entry.setText(getContext().getString(R.string.tv_time_zone) + " " + sdf_time_zone.format(cal.getTime()));
        tv_timezone_exit.setText(getContext().getString(R.string.tv_time_zone) + " " + sdf_time_zone.format(cal.getTime()));

        pb_spinner_manual = view.findViewById(R.id.pb_spinner_manual);
        pb_spinner_manual.setVisibility(View.VISIBLE);


        // Preferencias
        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        // Registro incompleto
        sw_completed_record = view.findViewById(R.id.sw_complete_record);
        tv_incompleted_record_date = view.findViewById(R.id.tv_incompleted_record_date);
        tv_incompleted_record_entry = view.findViewById(R.id.tv_incompleted_record_entry);
        tv_incompleted_record_exit = view.findViewById(R.id.tv_incompleted_record_exit);

        sw_completed_record.setOnCheckedChangeListener(this);

        setIncompletedRecord();

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
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
            case R.id.tv_date:
                if (et_date.hasFocus()) writeDate();
                else et_date.requestFocus();
                break;
            case R.id.tv_time_entry:
                if (et_time_entry.hasFocus()) writeTime(et_time_entry);
                else et_time_entry.requestFocus();
                break;
            case R.id.tv_time_exit:
                if (et_time_exit.hasFocus()) writeTime(et_time_exit);
                else et_time_exit.requestFocus();
                break;
            case R.id.et_date:
                if (et_date.hasFocus()) writeDate();
                else et_date.requestFocus();
                break;
            case R.id.et_time_entry:
                if (et_time_entry.hasFocus()) writeTime(et_time_entry);
                else et_time_entry.requestFocus();
                break;
            case R.id.et_time_exit:
                if (et_time_exit.hasFocus()) writeTime(et_time_exit);
                else et_time_exit.requestFocus();
                break;
            case R.id.btn_send:
                if (TextUtils.isEmpty(et_date.getText()) || TextUtils.isEmpty(et_time_entry.getText())) {
                    if (TextUtils.isEmpty(et_date.getText())) et_date.setError("La fecha es obligatoria");
                    if (TextUtils.isEmpty(et_time_entry.getText())) et_time_entry.setError("La hora de entrada es obligatoria");
                } else {
                    if (sw_completed_record.isChecked()) {
                        manualExit();
                    } else {
                        manualRecord(!TextUtils.isEmpty(et_time_exit.getText()));
                    }
                }
                break;
            case R.id.iv_clear_date:
                et_date.getText().clear();
                break;
            case R.id.iv_clear_entry:
                et_time_entry.getText().clear();
                break;
            case R.id.iv_clear_exit:
                et_time_exit.getText().clear();
                break;
        }
    }

    private void writeDate() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                final int currentMonth = month + 1;
                String formatedDay = (dayOfMonth < 10) ? CERO + String.valueOf(dayOfMonth) : String.valueOf(dayOfMonth);
                String formatedMonth = (currentMonth < 10) ? CERO + String.valueOf(currentMonth) : String.valueOf(currentMonth);
                et_date.setText(formatedDay + BARRA + formatedMonth + BARRA + year);
            }
        }, anio, mes, dia);
        datePickerDialog.getDatePicker().setMaxDate(Calendar.getInstance().getTime().getTime());
        datePickerDialog.show();
    }

    private void writeTime(final EditText et_time) {
        int hour, minute;
        if (et_time.getText().length() > 0) {
            hour = Integer.valueOf(et_time.getText().toString().split(":")[0]);
            minute = Integer.valueOf(et_time.getText().toString().split(":")[1]);
        } else if (et_time.getId() == R.id.et_time_exit && et_time_entry.getText().length() > 0) {
            hour = Integer.valueOf(et_time_entry.getText().toString().split(":")[0]);
            minute = Integer.valueOf(et_time_entry.getText().toString().split(":")[1]);
        } else {
            hour = hora;
            minute = minuto;
        }

        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                String formatedHour = (hourOfDay < 10) ? CERO + String.valueOf(hourOfDay) : String.valueOf(hourOfDay);
                String formatedMinute = (minute < 10) ? CERO + String.valueOf(minute) : String.valueOf(minute);
                et_time.setText(formatedHour + DOS_PUNTOS + formatedMinute);
            }
        }, hour, minute, true);
        timePickerDialog.show();
    }

    private void manualRecord(boolean withExit) {
        pb_spinner_manual.setVisibility(View.VISIBLE);
        Retrofit retrofit = new Retrofit.Builder().baseUrl(ServerURLs.ROOT_URL).addConverterFactory(GsonConverterFactory.create()).build();
        RecordService recordService = retrofit.create(RecordService.class);
        Call< Record > call = null;

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        try {
            Date fecha = sdf.parse(et_date.getText().toString());
            Calendar cal_entry = Calendar.getInstance();
            int entry_hour, entry_minute;
            entry_hour = Integer.parseInt(et_time_entry.getText().toString().split(":")[0]);
            entry_minute = Integer.parseInt(et_time_entry.getText().toString().split(":")[1]);
            cal_entry.setTime(fecha);
            cal_entry.set(Calendar.HOUR_OF_DAY, entry_hour);
            cal_entry.set(Calendar.MINUTE, entry_minute);
            cal_entry.setTimeZone(TimeZone.getDefault());

            if (withExit) {
                Calendar cal_exit = Calendar.getInstance();
                int exit_hour, exit_minute;
                exit_hour = Integer.parseInt(et_time_exit.getText().toString().split(":")[0]);
                exit_minute = Integer.parseInt(et_time_exit.getText().toString().split(":")[1]);
                cal_exit.setTime(fecha);
                cal_exit.set(Calendar.HOUR_OF_DAY, exit_hour);
                cal_exit.set(Calendar.MINUTE, exit_minute);
                cal_exit.setTimeZone(TimeZone.getDefault());

                call = recordService.manualRecord(prefs.getString(SharedPreferencesKeys.TOKEN, ""), cal_entry.getTime(), cal_exit.getTime());
            } else {
                call = recordService.manualRecord(prefs.getString(SharedPreferencesKeys.TOKEN, ""), cal_entry.getTime());
            }

            call.enqueue(new Callback<Record>() {
                @Override
                public void onResponse(Call<Record> call, Response<Record> response) {
                    pb_spinner_manual.setVisibility(View.GONE);
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "Registro realizado satisfactoriamente", Toast.LENGTH_SHORT).show();
                        et_date.setText("");
                        et_time_entry.setText("");
                        et_time_exit.setText("");
                        pb_spinner_manual.setVisibility(View.VISIBLE);
                        setIncompletedRecord();
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
                    pb_spinner_manual.setVisibility(View.GONE);
                    Toast.makeText(getContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (ParseException e) {
            pb_spinner_manual.setVisibility(View.GONE);
            Toast.makeText(getContext(), "Algo ha salido mal", Toast.LENGTH_SHORT).show();
            Log.d("MANUAL_RECORD_CATCH", e.getMessage());
        }
    }

    private void manualExit() {
        pb_spinner_manual.setVisibility(View.VISIBLE);
        Retrofit retrofit = new Retrofit.Builder().baseUrl(ServerURLs.ROOT_URL).addConverterFactory(GsonConverterFactory.create()).build();
        RecordService recordService = retrofit.create(RecordService.class);
        Call< Record > call = null;
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        try {
            Date exit = sdf.parse(et_date.getText().toString() + " " + et_time_exit.getText().toString());
            Calendar cal = Calendar.getInstance();
            cal.setTimeZone(TimeZone.getDefault());
            cal.setTime(exit);

            call = recordService.manualExit(prefs.getString(SharedPreferencesKeys.TOKEN, ""), cal.getTime());

            call.enqueue(new Callback<Record>() {
                @Override
                public void onResponse(Call<Record> call, Response<Record> response) {
                    pb_spinner_manual.setVisibility(View.GONE);
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "Registro completado satisfactoriamente", Toast.LENGTH_SHORT).show();
                        et_date.setText("");
                        et_time_entry.setText("");
                        et_time_exit.setText("");
                        pb_spinner_manual.setVisibility(View.VISIBLE);
                        setIncompletedRecord();
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
                    pb_spinner_manual.setVisibility(View.GONE);
                    Toast.makeText(getContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (ParseException e) {
            pb_spinner_manual.setVisibility(View.GONE);
            Toast.makeText(getContext(), "Algo ha salido mal", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }



    }

    private void setIncompletedRecord() {
        Retrofit retrofit = new Retrofit.Builder().baseUrl(ServerURLs.ROOT_URL).addConverterFactory(GsonConverterFactory.create()).build();
        RecordService recordService = retrofit.create(RecordService.class);

        Call<Record> call = recordService.getIncompleteRecord(prefs.getString(SharedPreferencesKeys.TOKEN, ""));
        call.enqueue(new Callback<Record>() {
            @Override
            public void onResponse(Call<Record> call, Response<Record> response) {
                pb_spinner_manual.setVisibility(View.GONE);

                if (response.isSuccessful()) {
                    if (response.code() == 204) { // Code 204=NoContent -> No hay registro incompleto
                        tv_incompleted_record_date.setText("-");
                        tv_incompleted_record_entry.setText("-");
                        tv_incompleted_record_exit.setText("-");

                        // Desactivamos el switch
                        sw_completed_record.setEnabled(false);
                        sw_completed_record.setChecked(false);

                        incompletedRecord = null;
                    } else { // Hay un registro incompleto
                        incompletedRecord = response.body();
                        SimpleDateFormat sdf_date = new SimpleDateFormat("dd-MM-yyyy");
                        sdf_date.setTimeZone(TimeZone.getDefault());
                        SimpleDateFormat sdf_time = new SimpleDateFormat("HH:mm z");
                        sdf_time.setTimeZone(TimeZone.getDefault());
                        String fecha = sdf_date.format(incompletedRecord.getEntry());
                        String entry = sdf_time.format(incompletedRecord.getEntry());

                        sw_completed_record.setEnabled(true);
                        sw_completed_record.setChecked(false);

                        tv_incompleted_record_date.setText(fecha);
                        tv_incompleted_record_entry.setText(entry);
                        tv_incompleted_record_exit.setText("-");
                    }
                } else {
                    incompletedRecord = null;
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
                pb_spinner_manual.setVisibility(View.GONE);
                incompletedRecord = null;
                Toast.makeText(getContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            switch (v.getId()) {
                case R.id.et_date:
                    writeDate();
                    break;
                case R.id.et_time_entry:
                    writeTime(et_time_entry);
                    break;
                case R.id.et_time_exit:
                    writeTime(et_time_exit);
                    break;
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.sw_complete_record) {
            if (isChecked && incompletedRecord != null) {
                // Solo se puede modificar si hay registro incompleto
                SimpleDateFormat sdf_date = new SimpleDateFormat("dd/MM/yyyy");
                sdf_date.setTimeZone(TimeZone.getDefault());
                SimpleDateFormat sdf_time = new SimpleDateFormat("HH:mm");
                sdf_time.setTimeZone(TimeZone.getDefault());
                String fecha = sdf_date.format(incompletedRecord.getEntry());
                String entry = sdf_time.format(incompletedRecord.getEntry());

                et_date.setText(fecha);
                et_time_entry.setText(entry);

                et_date.setClickable(false);
                et_time_entry.setClickable(false);
                et_date.setFocusable(false);
                et_time_entry.setFocusable(false);

                tv_date.setClickable(false);
                tv_time_entry.setClickable(false);
            } else {
                et_date.setFocusableInTouchMode(true);
                et_time_entry.setFocusableInTouchMode(true);
                et_date.setClickable(true);
                et_time_entry.setClickable(true);

                tv_date.setClickable(true);
                tv_time_entry.setClickable(true);
            }

        }
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
