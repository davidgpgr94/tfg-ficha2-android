package uva.inf.davidgo.ficha2.fragments;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uva.inf.davidgo.ficha2.R;
import uva.inf.davidgo.ficha2.adapters.ExpandableRecordsSection;
import uva.inf.davidgo.ficha2.listeners.PaginationScrollListener;
import uva.inf.davidgo.ficha2.pojos.Employee;
import uva.inf.davidgo.ficha2.pojos.Record;
import uva.inf.davidgo.ficha2.pojos.RecordsContext;
import uva.inf.davidgo.ficha2.services.ApiClient;
import uva.inf.davidgo.ficha2.services.EmployeeService;
import uva.inf.davidgo.ficha2.services.RecordService;
import uva.inf.davidgo.ficha2.utils.SharedPreferencesKeys;


public class ExpandableRecordsFragment extends BaseFragment implements View.OnClickListener {
    // TODO: Se ha creado BaseFragment para usar su método de "logout" cuando en la respuesta de alguna llamada a la api nos devuelva un HTTP_UNAUTHORIZED debido a que el token ha expirado
    // TODO: y así redirigir a la activity de logeo
    public ExpandableRecordsFragment() {}
    String TAG = ExpandableRecordsFragment.class.getSimpleName();
    /* Preferences */
    // private SharedPreferences prefs;

    /* RecyclerView */
    private SectionedRecyclerViewAdapter sectionAdapter;
    private RecyclerView recyclerView;

    /* Time ranges */
    private Spinner spinnerTimeRanges;
    private HashMap<String, Date[]> timeRanges = new HashMap<>();

    /* Pagination */
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private int page = 1;

    /* ProgressBar */
    private ProgressBar progressBar;

    /* Refresh Button */
    private ImageButton buttonRefresh;

    /* Only Admin views */
    private TextView labelEmployees;
    private Spinner spinnerEmployees;
    private ArrayAdapter<String> spinnerEmployeesAdapter;
    HashMap<String, Employee> employees;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View view = inflater.inflate(R.layout.fragment_expandable_records, container, false);

        /* ProgressBar */
        progressBar = view.findViewById(R.id.pb_records);

        /* Set visible the progress bar */
        progressBar.setVisibility(View.VISIBLE);

        /* Preferences */
        // prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        /* Common spinners */
        spinnerTimeRanges = view.findViewById(R.id.sp_time_range_filter);
        initTimeRangesValues();
        String[] filters = timeRanges.keySet().toArray(new String[5]);
        ArrayAdapter<String> timeRangesAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, filters);
        spinnerTimeRanges.setAdapter(timeRangesAdapter);
        spinnerTimeRanges.setSelection(timeRangesAdapter.getPosition("Hoy"));
        spinnerTimeRanges.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String timeRangeSelected = (String) parent.getItemAtPosition(position);
                page = 1;
                isLoading = false;
                isLastPage = false;
                sectionAdapter.removeAllSections();
                recyclerView.scrollToPosition(0);
                if (prefs.getBoolean(SharedPreferencesKeys.USER_IS_ADMIN, false)) {
                    if (spinnerEmployees.getCount() > 0) {
                        loadData(page, timeRangeSelected, (String) spinnerEmployees.getSelectedItem());
                    }
                } else {
                    loadData(page, timeRangeSelected);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                /* Do nothing */
            }
        });



        /* Refresh button */
        buttonRefresh = view.findViewById(R.id.ib_refresh);
        buttonRefresh.setOnClickListener(this);


        /* Only admin views */
        if (prefs.getBoolean(SharedPreferencesKeys.USER_IS_ADMIN, false)) {
            employees = new HashMap<>();
            labelEmployees = view.findViewById(R.id.tv_label_employees);
            spinnerEmployees = view.findViewById(R.id.sp_employees);

            labelEmployees.setVisibility(View.VISIBLE);
            spinnerEmployees.setVisibility(View.VISIBLE);

            spinnerEmployeesAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item);
            spinnerEmployees.setAdapter(spinnerEmployeesAdapter);
            spinnerEmployees.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    // TODO: Display records of the employee selected
                    String employeeSelected = (String) parent.getItemAtPosition(position);
                    page = 1;
                    isLoading = false;
                    isLastPage = false;
                    sectionAdapter.removeAllSections();
                    recyclerView.scrollToPosition(0);
                    if (spinnerEmployees.getCount() > 0) {
                        loadData(page, (String) spinnerTimeRanges.getSelectedItem(), employeeSelected);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    /* Do nothing */
                }
            });
            setUpEmployeesSpinner();
        }

        /* RecyclerView */
        recyclerView = view.findViewById(R.id.rv_records);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        sectionAdapter = new SectionedRecyclerViewAdapter();
        recyclerView.setAdapter(sectionAdapter);
        recyclerView.addOnScrollListener(new PaginationScrollListener(linearLayoutManager) {
            @Override
            protected void loadMoreItems() {
                isLoading = true;
                if (!isLastPage) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            String dateRange = spinnerTimeRanges.getSelectedItem().toString();
                            if (prefs.getBoolean(SharedPreferencesKeys.USER_IS_ADMIN, false)) {
                                String employeeSelected = (String) spinnerEmployees.getSelectedItem();
                                loadData(page, dateRange, employeeSelected);
                            } else {
                                loadData(page, dateRange);
                            }
                        }
                    }, 200);
                }
            }

            @Override
            public boolean isLastPage() {
                return isLastPage;
            }

            @Override
            public boolean isLoading() {
                return isLoading;
            }
        });


        progressBar.setVisibility(View.GONE);
        return view;
    }


    /* Pagination methods */
    private void loadData(int page, String timeRange) {
        if (progressBar.getVisibility() != View.VISIBLE) progressBar.setVisibility(View.VISIBLE);

        RecordService recordService = ApiClient.createService(RecordService.class);
        Date[] dates = timeRanges.get(timeRange);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String from, to;
        from = sdf.format(dates[0]);
        to = sdf.format(dates[1]);

        Call<RecordsContext> call = recordService.getRecords(
                prefs.getString(SharedPreferencesKeys.TOKEN, ""),
                page,
                from, to
        );
        call.enqueue(new Callback<RecordsContext>() {
            @Override
            public void onResponse(Call<RecordsContext> call, Response<RecordsContext> response) {
                Log.d(TAG, response.raw().toString());
                if (response.isSuccessful()) {
                    RecordsContext serverResponse = response.body();
                    resultAction(serverResponse);
                    Log.d(TAG, "Total records: "+serverResponse.getTotal_records());
                } else if(response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    Log.d(TAG, "TOKEN: loadData de 2");
                    Toast.makeText(getContext(), "Session expired", Toast.LENGTH_SHORT).show();
                    onTokenNotValid();
                } else {
                    Log.d(TAG, "En donde loadData de 2");
                    Toast.makeText(getContext(), "Response not successful", Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<RecordsContext> call, Throwable t) {
                Log.d(TAG, "onFailure: " + t.getLocalizedMessage());
            }
        });
    }

    private void resultAction(RecordsContext model) {
        isLoading = false;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        if (model != null) {
            for (Record record : model.getRecords()) {
                String date = sdf.format(record.getEntry());
                ExpandableRecordsSection recordDateSection = (ExpandableRecordsSection) sectionAdapter.getSection(date);
                if (recordDateSection == null) {
                    recordDateSection = new ExpandableRecordsSection(date, sectionAdapter);
                }
                recordDateSection.addRecord(record);
                sectionAdapter.addSection(date, recordDateSection);
                sectionAdapter.notifyDataSetChanged();
            }
            if (model.getPage() == model.getNum_pages()) {
                isLastPage = true;
            } else {
                page = model.getPage() + 1;
            }
        }
    }

    /* ------------ */

    /* Events handlers */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ib_refresh:
                sectionAdapter.removeAllSections();
                page = 1;
                isLoading = false;
                isLastPage = false;
                recyclerView.scrollToPosition(0);
                if (prefs.getBoolean(SharedPreferencesKeys.USER_IS_ADMIN, false)) {
                    loadData(page, (String) spinnerTimeRanges.getSelectedItem(), (String) spinnerEmployees.getSelectedItem());
                } else {
                    loadData(page, (String) spinnerTimeRanges.getSelectedItem());
                }

                break;
        }
    }

    /* ------------ */

    /* Auxiliary methods */
    private void initTimeRangesValues() {
        Calendar cal = getCurrentDayCleared();

        Date today = cal.getTime();
        cal.add(Calendar.DATE, 1);
        Date tomorrow = cal.getTime();
        timeRanges.put("Hoy", new Date[]{today, tomorrow});

        cal = getCurrentDayCleared();
        cal.add(Calendar.DATE, -1);
        Date yesterday = cal.getTime();
        timeRanges.put("Ayer", new Date[]{yesterday, today});

        Date startOfWeek, endOfWeek;
        cal = getCurrentDayCleared();
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        startOfWeek = cal.getTime();
        cal.add(Calendar.WEEK_OF_YEAR, 1);
        endOfWeek = cal.getTime();
        timeRanges.put("Esta semana", new Date[]{startOfWeek, endOfWeek});

        Date startOfMonth, endOfMonth;
        cal = getCurrentDayCleared();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        startOfMonth = cal.getTime();
        cal.add(Calendar.MONTH, 1);
        endOfMonth = cal.getTime();
        timeRanges.put("Este mes", new Date[]{startOfMonth, endOfMonth});

        Date startOfLastMonth, endOfLastMonth;
        cal = getCurrentDayCleared();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        endOfLastMonth = cal.getTime();
        cal.add(Calendar.MONTH, -1);
        startOfLastMonth = cal.getTime();
        timeRanges.put("Mes pasado", new Date[]{startOfLastMonth, endOfLastMonth});
    }

    private Calendar getCurrentDayCleared() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getDefault());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);

        return cal;
    }

    /* ------------ */

    /* Admin methods */
    private void setUpEmployeesSpinner() {
        EmployeeService employeeService = ApiClient.createService(EmployeeService.class);
        Call<List<Employee>> call = employeeService.getEmployees(prefs.getString(SharedPreferencesKeys.TOKEN, ""));

        call.enqueue(new Callback<List<Employee>>() {
            @Override
            public void onResponse(Call<List<Employee>> call, Response<List<Employee>> response) {
                if (response.isSuccessful()) {
                    ArrayList<Employee> responseEmployees = (ArrayList<Employee>) response.body();

                    if (!(responseEmployees == null)) {
                        for (Employee e : responseEmployees) {
                            employees.put(e.getLogin(), e);
                        }
                    }


                    spinnerEmployeesAdapter.addAll(employees.keySet());
                    spinnerEmployeesAdapter.remove(prefs.getString(SharedPreferencesKeys.USER_LOGIN, ""));
                    spinnerEmployeesAdapter.insert(prefs.getString(SharedPreferencesKeys.USER_LOGIN, ""), 0);
                    spinnerEmployeesAdapter.notifyDataSetChanged();
                    spinnerEmployees.setSelection(
                            spinnerEmployeesAdapter.getPosition(
                                    prefs.getString(SharedPreferencesKeys.USER_LOGIN, spinnerEmployeesAdapter.getItem(0))
                            )
                    );
                } else if(response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    Log.d(TAG, "TOKEN: SetUpEmployeesSpinner");
                    Toast.makeText(getContext(), "Session expired", Toast.LENGTH_SHORT).show();
                    onTokenNotValid();
                } else {
                    try {
                        JSONObject msg = new JSONObject(response.errorBody().string());
                        Toast.makeText(getContext(), msg.getString("message"), Toast.LENGTH_SHORT).show();
                    } catch (JSONException | IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Employee>> call, Throwable t) {
                Toast.makeText(getContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadData(int page, String timeRange, String employeeLogin) {
        if (progressBar.getVisibility() != View.VISIBLE) progressBar.setVisibility(View.VISIBLE);

        RecordService recordService = ApiClient.createService(RecordService.class);
        Date[] dates = timeRanges.get(timeRange);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String from, to;
        from = sdf.format(dates[0]);
        to = sdf.format(dates[1]);

        Call<RecordsContext> call = recordService.getRecords(
                prefs.getString(SharedPreferencesKeys.TOKEN, ""),
                employees.get(employeeLogin).get_id(),
                page,
                from, to
        );
        call.enqueue(new Callback<RecordsContext>() {
            @Override
            public void onResponse(Call<RecordsContext> call, Response<RecordsContext> response) {
                if (response.isSuccessful()) {
                    RecordsContext serverResponse = response.body();
                    resultAction(serverResponse);
                } else if(response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    Log.d(TAG, "TOKEN: loadData de 3");
                    Toast.makeText(getContext(), "Session expired", Toast.LENGTH_SHORT).show();
                    onTokenNotValid();
                } else {
                    Log.d(TAG, "En donde loadData de 3");
                    Toast.makeText(getContext(), "Response not successful", Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<RecordsContext> call, Throwable t) {

            }
        });
    }

    /* ------------ */

}
