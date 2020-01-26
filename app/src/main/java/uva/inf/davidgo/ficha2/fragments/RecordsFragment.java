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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import uva.inf.davidgo.ficha2.R;
import uva.inf.davidgo.ficha2.adapters.RecordWithConfirmationAdapter;
import uva.inf.davidgo.ficha2.listeners.PaginationScrollListener;
import uva.inf.davidgo.ficha2.pojos.Employee;
import uva.inf.davidgo.ficha2.pojos.RecordsContext;
import uva.inf.davidgo.ficha2.services.ApiClient;
import uva.inf.davidgo.ficha2.services.EmployeeService;
import uva.inf.davidgo.ficha2.services.RecordService;
import uva.inf.davidgo.ficha2.utils.ServerURLs;
import uva.inf.davidgo.ficha2.utils.SharedPreferencesKeys;

/**
 * NOT USED
 * TODO: remove this fragment
 */
public class RecordsFragment extends Fragment implements View.OnClickListener {
    String TAG = RecordsFragment.class.getSimpleName();
    public RecordsFragment() {
        // Required empty public constructor
    }

    private HashMap<String, Date[]> timeRanges = new HashMap<>();

    TextView tv_label_employees;
    Spinner sp_employees, sp_time_range_filter;
    ProgressBar pb_records;

    ImageButton ib_refresh;

    RecyclerView rv_records;
    RecordWithConfirmationAdapter recordsAdapter;

    SharedPreferences prefs;

    List<Employee> employees;

    private boolean isLoading = false;
    private boolean isLastPage = false;
    private int page = 1;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_records, container, false);

        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        pb_records = view.findViewById(R.id.pb_records);
        rv_records = view.findViewById(R.id.rv_records_records_fragment);

        ib_refresh = view.findViewById(R.id.ib_download);
        ib_refresh.setOnClickListener(this);

        initTimeRangesValues();

        // TODO: Admin - Creo que ya esta terminado. Repasar!
        if (prefs.getBoolean(SharedPreferencesKeys.USER_IS_ADMIN, false)) {
            tv_label_employees = view.findViewById(R.id.tv_label_employees);
            sp_employees = view.findViewById(R.id.sp_employees);

            tv_label_employees.setVisibility(View.VISIBLE);
            sp_employees.setVisibility(View.VISIBLE);

            setUpEmployeesSpinner();
        }

        sp_time_range_filter = view.findViewById(R.id.sp_time_range_filter);


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        rv_records.setLayoutManager(linearLayoutManager);

        recordsAdapter = new RecordWithConfirmationAdapter(getContext());
        rv_records.setAdapter(recordsAdapter);
        rv_records.addOnScrollListener(new PaginationScrollListener(linearLayoutManager) {
            @Override
            protected void loadMoreItems() {
                isLoading = true;
                if (!isLastPage) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            String dateRange = sp_time_range_filter.getSelectedItem().toString();
                            loadData(page, dateRange);
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

        setUpTimeRangeFilter("Hoy");
        // loadData(page, "Hoy");

        return view;
    }

    private void loadData(int page, String timeRange) {
        pb_records.setVisibility(View.VISIBLE);
        RecordService recordService = ApiClient.createService(RecordService.class);
        Date[] dates = timeRanges.get(timeRange);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String from, to;
        from = sdf.format(dates[0]);
        to = sdf.format(dates[1]);
        Call<RecordsContext> call = recordService.getRecords(prefs.getString(SharedPreferencesKeys.TOKEN, ""),
                page,
                from, to
        );
        call.enqueue(new Callback<RecordsContext>() {
            @Override
            public void onResponse(Call<RecordsContext> call, Response<RecordsContext> response) {
                if (response.isSuccessful()) {
                    RecordsContext serverResponse = response.body();
                    resultAction(serverResponse);
                } else {
                    Toast.makeText(getContext(), "Response notSuccesful", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RecordsContext> call, Throwable t) {
                Toast.makeText(getContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resultAction(RecordsContext model) {
        pb_records.setVisibility(View.GONE);
        isLoading = false;
        if (model != null) {
            recordsAdapter.addItems(model.getRecords());
            if (model.getPage() == model.getNum_pages()) {
                isLastPage = true;
            } else {
                page = model.getPage() + 1;
            }
        }
    }

    private void setUpTimeRangeFilter(String defaultFilter) {
        String[] filters = timeRanges.keySet().toArray(new String[5]);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, filters);
        sp_time_range_filter.setAdapter(adapter);

        sp_time_range_filter.setSelection(adapter.getPosition(defaultFilter));

        sp_time_range_filter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // TODO: ----------->
                String itemSelected = (String) parent.getItemAtPosition(position);
                page = 1;
                isLoading = false;
                isLastPage = false;
                recordsAdapter.clearData();
                rv_records.scrollToPosition(0);
                loadData(page, itemSelected);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
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

    public void setUpEmployeesSpinner() {
        Retrofit retrofit = new Retrofit.Builder().baseUrl(ServerURLs.ROOT_URL).addConverterFactory(GsonConverterFactory.create()).build();
        EmployeeService employeeService = retrofit.create(EmployeeService.class);

        Call<List<Employee>> call = employeeService.getEmployees(prefs.getString(SharedPreferencesKeys.TOKEN, ""));
        call.enqueue(new Callback<List<Employee>>() {
            @Override
            public void onResponse(Call<List<Employee>> call, Response<List<Employee>> response) {
                if (response.isSuccessful()) {
                    employees = response.body();
                    ArrayList<String> employeesLogin = new ArrayList<>();
                    for (Employee e : employees) {
                        employeesLogin.add(e.getLogin());
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, employeesLogin.toArray(new String[0]));
                    sp_employees.setAdapter(adapter);

                    sp_employees.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            // TODO: ------>
                            String itemSelected = (String) parent.getItemAtPosition(position);
                            Toast.makeText(getContext(), itemSelected, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                            Toast.makeText(getContext(), "Nada de nah!!", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    try {
                        JSONObject msg = new JSONObject(response.errorBody().string());
                        Toast.makeText(getContext(), msg.getString("message"), Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        Log.d("EMPLOYEES_CATCH_JSONExc", e.getMessage());
                        e.printStackTrace();
                    } catch (IOException e) {
                        Log.d("EMPLOYEES_CATCH_IOExc:", e.getMessage());
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Employee>> call, Throwable t) {
                Log.d("EMPLOYEES_OnFailure", t.getLocalizedMessage());
                Toast.makeText(getContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ib_download:
                recordsAdapter.clearData();
                page = 1;
                isLoading = false;
                isLastPage = false;
                rv_records.scrollToPosition(0);
                loadData(page, sp_time_range_filter.getSelectedItem().toString());
                break;
        }
    }
}
