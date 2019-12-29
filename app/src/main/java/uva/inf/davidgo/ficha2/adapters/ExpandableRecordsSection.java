package uva.inf.davidgo.ficha2.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import org.joda.time.Duration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;
import uva.inf.davidgo.ficha2.R;
import uva.inf.davidgo.ficha2.pojos.Record;

public class ExpandableRecordsSection extends StatelessSection {
    private final String TAG = ExpandableRecordsSection.class.getSimpleName();

    /* Adapter */
    private SectionedRecyclerViewAdapter myAdapter;

    /* Header holder */
    private HeaderViewHolder myHeaderHolder;

    /* Section date, worked time and records */
    private final String date;
    private final List<Record> records;
    private Duration timeWorked;

    /* true to be expanded by default */
    private boolean expanded = false;



    /* Constructors */
    public ExpandableRecordsSection(String date, List<Record> records, SectionedRecyclerViewAdapter adapter) {
        this(date, adapter);
        addRecords(records);
    }

    public ExpandableRecordsSection(String date, SectionedRecyclerViewAdapter adapter) {
        super(SectionParameters.builder()
                .itemResourceId(R.layout.section_item)
                .headerResourceId(R.layout.section_header)
                .build());

        this.date = date;
        this.myAdapter = adapter;
        this.records = new ArrayList<Record>();
        this.timeWorked = Duration.ZERO;
    }



    /* Override methods */
    @Override
    public int getContentItemsTotal() {
        return expanded ? records.size() : 0;
    }

    @Override
    public RecyclerView.ViewHolder getItemViewHolder(View view) {
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
        final ItemViewHolder itemHolder = (ItemViewHolder) holder;
        final Record record = records.get(position);

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm z");
        sdf.setTimeZone(TimeZone.getDefault());

        String entry, exit;
        entry = (record.getEntry() == null) ? "--" : sdf.format(record.getEntry());
        exit = (record.getExit() == null) ? "--" : sdf.format(record.getExit());

        itemHolder.tv_time_entry_record_item.setText(entry);
        itemHolder.tv_time_exit_record_item.setText(exit);
    }

    @Override
    public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
        return new HeaderViewHolder(view);
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
        myHeaderHolder = (HeaderViewHolder) holder;

        int hours = (int) timeWorked.getStandardHours();
        int minutes = (int) timeWorked.getStandardMinutes() - (hours*60);
        String strTimeWorked = String.format("%d h %d m", hours, minutes);

        myHeaderHolder.tvTimeWorked.setText(strTimeWorked);
        myHeaderHolder.tvDate.setText(date);
        myHeaderHolder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expanded = !expanded;
                myAdapter.notifyDataSetChanged();
            }
        });
    }



    /* ExpandableRecordsSection methods */
    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(final boolean expanded) {
        this.expanded = expanded;
    }

    public void addRecord(Record record) {
        if (!this.records.contains(record)) {
            this.records.add(record);
            Collections.sort(this.records, new Comparator<Record>() {
                @Override
                public int compare(Record o1, Record o2) {
                    return o1.getEntry().compareTo(o2.getEntry());
                }
            });
            /* Calculate the time worked in this record */
            if (record.getExit() != null) {
                Date entry = record.getEntry();
                Date exit = record.getExit();
                Duration recordDuration = new Duration(entry.getTime(), exit.getTime());
                timeWorked = timeWorked.plus(recordDuration);
            }

            if (myHeaderHolder != null) {
                int hours = (int) timeWorked.getStandardHours();
                int minutes = (int)timeWorked.getStandardMinutes() - (hours*60);
                String strTimeWorked = String.format("%d h %d m", hours, minutes);
                myHeaderHolder.tvTimeWorked.setText(strTimeWorked);
                //myAdapter.notifyDataSetChanged();
            }
        }
    }

    private void addRecords(List<Record> records) {
        for (Record record : records) {
            addRecord(record);
        }
    }



    /* ViewHolders */
    class HeaderViewHolder extends RecyclerView.ViewHolder {
        final View rootView;
        final TextView tvDate, tvTimeWorked;

        public HeaderViewHolder(@NonNull View view) {
            super(view);
            rootView = view;
            tvDate = view.findViewById(R.id.tv_record_date);
            tvTimeWorked = view.findViewById(R.id.tv_total_hours_in_day);
        }
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {

        final View rootView;
        final TextView tv_time_entry_record_item, tv_time_exit_record_item;
        //final CheckBox cb_signed_by_user, cb_signed_by_admin;

        public ItemViewHolder(@NonNull View view) {
            super(view);

            rootView = view;

            tv_time_entry_record_item = view.findViewById(R.id.tv_time_entry_record_item);
            tv_time_exit_record_item = view.findViewById(R.id.tv_time_exit_record_item);

            //cb_signed_by_user = view.findViewById(R.id.cb_signed_by_user);
            //cb_signed_by_admin = view.findViewById(R.id.cb_signed_by_admin);
        }
    }

}
