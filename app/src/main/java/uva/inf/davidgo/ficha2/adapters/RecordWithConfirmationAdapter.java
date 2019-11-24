package uva.inf.davidgo.ficha2.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import uva.inf.davidgo.ficha2.R;
import uva.inf.davidgo.ficha2.pojos.Record;

public class RecordWithConfirmationAdapter extends RecyclerView.Adapter<RecordWithConfirmationAdapter.RecordWithConfirmationViewHolder> {

    private ArrayList<Record> data;
    private Context context;

    public RecordWithConfirmationAdapter(Context ctx) {
        this.context = ctx;
        data = new ArrayList<>();
    }

    public RecordWithConfirmationAdapter(Context ctx, List<Record> data) {
        this.context = ctx;
        setData(data);
    }

    @NonNull
    @Override
    public RecordWithConfirmationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.record_with_confirmation_item, parent, false);
        RecordWithConfirmationViewHolder holder = new RecordWithConfirmationViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecordWithConfirmationViewHolder viewHolder, int position) {
        Record record = data.get(position);

        SimpleDateFormat sdfDate = new SimpleDateFormat("dd-MM-yyyy");
        sdfDate.setTimeZone(TimeZone.getDefault());

        SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm z");
        sdfTime.setTimeZone(TimeZone.getDefault());

        String date = sdfDate.format(record.getEntry());
        String entry = sdfTime.format(record.getEntry());
        String exit;
        if (record.getExit() == null) exit = "-";
        else exit = sdfTime.format(record.getExit());

        viewHolder.tv_date_record_item.setText(date);
        viewHolder.tv_time_entry_record_item.setText(entry);
        viewHolder.tv_time_exit_record_item.setText(exit);

        viewHolder.cb_signed_by_user.setChecked(record.isSigned_by_employee());
        viewHolder.cb_signed_by_admin.setChecked(record.isSigned_by_admin());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setData(List<Record> data) {
        this.data.clear();
        this.data = (ArrayList<Record>) data;
        Collections.reverse(this.data);
        notifyDataSetChanged();
    }

    public void addItems(List<Record> items) {
        for(Record r : items) {
            this.data.add(r);
        }
        notifyDataSetChanged();
    }

    public void clearData() {
        this.data.clear();
    }

    public float getTotalHours() {
        float hours = 0;
        for (Record record : this.data) {
            Date entry = record.getEntry();
            Date exit = record.getExit();
            long diff = exit.getTime() - entry.getTime(); // ms
            hours = hours + (diff / (1000 * 60 * 60));
        }
        return hours;
    }

    class RecordWithConfirmationViewHolder extends RecyclerView.ViewHolder {

        private TextView tv_date_record_item, tv_time_entry_record_item, tv_time_exit_record_item;
        private CheckBox cb_signed_by_user, cb_signed_by_admin;

        public RecordWithConfirmationViewHolder(View itemView) {
            super(itemView);

            tv_date_record_item = itemView.findViewById(R.id.tv_date_record_item);
            tv_time_entry_record_item = itemView.findViewById(R.id.tv_time_entry_record_item);
            tv_time_exit_record_item = itemView.findViewById(R.id.tv_time_exit_record_item);

            cb_signed_by_user = itemView.findViewById(R.id.cb_signed_by_user);
            cb_signed_by_admin = itemView.findViewById(R.id.cb_signed_by_admin);
        }
    }
}
