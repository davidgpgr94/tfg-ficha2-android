package uva.inf.davidgo.ficha2.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import uva.inf.davidgo.ficha2.R;
import uva.inf.davidgo.ficha2.pojos.Record;

public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.RecordViewHolder> {

    private ArrayList<Record> data;
    private LayoutInflater inflater;

    public RecordAdapter(Context ctx, List<Record> data) {
        inflater = LayoutInflater.from(ctx);
        this.data = (ArrayList<Record>) data;
        Collections.reverse(this.data);
    }

    @NonNull
    @Override
    public RecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = inflater.inflate(R.layout.record_item, parent, false);
        RecordViewHolder holder = new RecordViewHolder(view);
        return holder;
        //return new RecordViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.record_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecordViewHolder recordViewHolder, int position) {
        Record record = data.get(position);

        SimpleDateFormat sdf_date = new SimpleDateFormat("dd-MM-yyyy");
        sdf_date.setTimeZone(TimeZone.getDefault());

        SimpleDateFormat sdf_time = new SimpleDateFormat("HH:mm z");
        sdf_time.setTimeZone(TimeZone.getDefault());

        String fecha = sdf_date.format(record.getEntry());
        String entry = sdf_time.format(record.getEntry());
        String exit;
        if (record.getExit() == null) {
            exit = "-";
        } else {
            exit = sdf_time.format(record.getExit());
        }


        recordViewHolder.tv_date_record_item.setText(fecha);
        recordViewHolder.tv_time_entry_record_item.setText(entry);
        recordViewHolder.tv_time_exit_record_item.setText(exit);

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setData(List<Record> data) {
        this.data.clear();
        this.data = (ArrayList<Record>) data;
        Collections.reverse(this.data);
    }

    class RecordViewHolder extends RecyclerView.ViewHolder {

        private TextView tv_date_record_item, tv_time_entry_record_item, tv_time_exit_record_item;

        public RecordViewHolder(View itemView) {
            super(itemView);

            tv_date_record_item = itemView.findViewById(R.id.tv_date_record_item);
            tv_time_entry_record_item = itemView.findViewById(R.id.tv_time_entry_record_item);
            tv_time_exit_record_item = itemView.findViewById(R.id.tv_time_exit_record_item);
        }
    }
}
