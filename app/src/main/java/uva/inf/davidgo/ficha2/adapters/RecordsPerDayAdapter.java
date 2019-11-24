package uva.inf.davidgo.ficha2.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import uva.inf.davidgo.ficha2.R;
import uva.inf.davidgo.ficha2.pojos.Record;

public class RecordsPerDayAdapter extends RecyclerView.Adapter<RecordsPerDayAdapter.RecordsPerDayHolder> {
    private Context context;
    private HashMap<Date, Double> dateTimeWorked;
    private HashMap<Date, RecordWithConfirmationAdapter> dateRecords;
    private ArrayList<Date> myKeys;

    //private ArrayList<Date> dates;
    //private ArrayList<RecordWithConfirmationAdapter> adapters;

    public RecordsPerDayAdapter(Context ctx) {
        this.context = ctx;
        this.dateTimeWorked = new HashMap<>();
        this.dateRecords = new HashMap<>();
        this.myKeys = new ArrayList<>();
    }

    public void addRecords(List<Record> items) {
        // TODO: --
    }

    public void clearData() {
        //this.dates.clear();
        //this.adapters.clear();
    }

    @NonNull
    @Override
    public RecordsPerDayHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.records_per_day, parent, false);
        RecordsPerDayHolder holder = new RecordsPerDayHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecordsPerDayHolder viewHolder, int position) {
        /*
        Date date = this.dates.get(position);
        RecordWithConfirmationAdapter adapter = this.adapters.get(position);

        SimpleDateFormat sdfDate = new SimpleDateFormat("dd-MM-yyyy");
        sdfDate.setTimeZone(TimeZone.getDefault());

        String strDate = sdfDate.format(date);
        viewHolder.tv_record_date.setText(strDate);
        float dayHoursWorked = adapter.getTotalHours();
        float dayMinsWorked = dayHoursWorked - (int) dayHoursWorked;
        String totalHours = String.format("%d h %f m", (int)dayHoursWorked, dayMinsWorked);
        viewHolder.tv_total_hours.setText(totalHours);
        viewHolder.rv_records_in_day.setAdapter(adapter);
        */
    }

    @Override
    public int getItemCount() {
        return 0;
        /*
        int count = 0;
        for (RecordWithConfirmationAdapter adapter : this.adapters) {
            count = count + adapter.getItemCount();
        }
        return count;
        */
        // Esto o data.size();
    }

    class RecordsPerDayHolder extends RecyclerView.ViewHolder {

        private TextView tv_record_date, tv_total_hours;
        private RecyclerView rv_records_in_day;

        public RecordsPerDayHolder(@NonNull View itemView) {
            super(itemView);
            rv_records_in_day.setLayoutManager(new LinearLayoutManager(context));
        }

        public void addRecords(List<Record> items) {
            // TODO:
            // rv_records_in_day.addRecords(items);
        }
    }

}
