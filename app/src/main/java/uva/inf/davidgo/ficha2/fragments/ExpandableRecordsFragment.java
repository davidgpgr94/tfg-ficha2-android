package uva.inf.davidgo.ficha2.fragments;


import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import okhttp3.ResponseBody;
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
    private ImageButton buttonDownload;

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
        buttonDownload = view.findViewById(R.id.ib_download);
        buttonDownload.setOnClickListener(this);


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

    @Override
    protected int getNavigationItemId() {
        return R.id.nav_my_records;
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
                    Log.d(TAG, "ExpandableRecordsFragment - loadData(page, timeRange)");
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
            case R.id.ib_download:
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
                } else {
                    downloadReport();
                }
                break;
        }
    }

    /* ------------ */

    /* Download PDF methods */
    private void downloadReport() {
        boolean isAdmin = prefs.getBoolean(SharedPreferencesKeys.USER_IS_ADMIN, false);
        String token = prefs.getString(SharedPreferencesKeys.TOKEN, "");
        String timeRange = (String) spinnerTimeRanges.getSelectedItem();
        Date[] dates = timeRanges.get(timeRange);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String from, to;
        from = sdf.format(dates[0]);
        to = sdf.format(dates[1]);

        Call<ResponseBody> call;
        RecordService recordService = ApiClient.createService(RecordService.class);
        if (isAdmin) {
            String employeeLogin = (String) spinnerEmployees.getSelectedItem();
            call = recordService.getReport(token, employees.get(employeeLogin).get_id(), from, to);
        } else {
            call = recordService.getReport(token, from, to);
        }
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        boolean success = writeResponseBodyToDisk(response.body(), true);
                        if (!success) {
                            Toast.makeText(getContext(), "Fallo en la descarga. Inténtalo de nuevo", Toast.LENGTH_SHORT).show();
                        }
                    } else
                        Toast.makeText(getContext(), "No se ha podido descargar el reporte", Toast.LENGTH_SHORT).show();

                } else if(response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    Toast.makeText(getContext(), "La sesión ha expirado", Toast.LENGTH_SHORT).show();
                    onTokenNotValid();
                } else {
                    Log.d(TAG, response.toString());
                    Toast.makeText(getContext(), "Algo salio mal", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(TAG, t.getLocalizedMessage());
                Toast.makeText(getContext(), "Fallo al descargar el reporte de horas", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openFile(File file) {

        Intent target = new Intent(Intent.ACTION_VIEW);
        target.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Uri apkURI = FileProvider.getUriForFile(getContext(), getContext().getApplicationContext().getPackageName()+".provider", file);
            target.setDataAndType(apkURI, "application/pdf");
        } else {
            target.setDataAndType(Uri.fromFile(file), "application/pdf");
        }
        target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Intent intent = Intent.createChooser(target, "Abrir reporte");
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, e.getLocalizedMessage());
        }
    }

    private boolean writeResponseBodyToDisk(ResponseBody body, boolean openAtFinish) {
        try {
            String filename = prefs.getString(SharedPreferencesKeys.USER_LOGIN, "")+"-";
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
            filename += sdf.format(new Date());
            Log.d(TAG, "Type: " + body.contentType().toString());
            File futureStudioFile = new File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    filename+".pdf"
            );
            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[4096];
                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;
                inputStream = body.byteStream();
                outputStream = new FileOutputStream(futureStudioFile);

                while (true) {
                    int read = inputStream.read(fileReader);
                    if (read == -1) break;
                    outputStream.write(fileReader, 0, read);
                    fileSizeDownloaded += read;
                    Log.d(TAG, "File download: " + fileSizeDownloaded + " of " + fileSize);
                }
                outputStream.flush();
                Log.d(TAG, "Descarga compleatada");
                if (openAtFinish) openFile(futureStudioFile);
                return true;
            } catch (IOException e) {
                return false;
            } finally {
                if (inputStream != null) inputStream.close();
                if (outputStream != null) outputStream.close();
            }

        } catch (IOException e) {
            return false;
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
                    Log.d(TAG, "ExpandableRecordsFragment - setUpEmployeesSpinner");
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
                    Log.d(TAG, "ExpandableRecordsFragment - loadData(page, timeRange, employeeLogin)");
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
