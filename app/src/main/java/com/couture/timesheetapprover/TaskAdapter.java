package com.couture.timesheetapprover;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by iet on 20-Mar-18.
 */

class TaskAdapter extends BaseAdapter{
    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<Timesheet> mDataSource;
    private String pmName;

    public TaskAdapter(Context context, ArrayList<Timesheet> timesheets, String username) {
        mContext = context;
        mDataSource = timesheets;
        pmName = username;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public int getCount() {
        return mDataSource.size();
    }

    @Override
    public Object getItem(int i) {
        return mDataSource.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View rowView = mInflater.inflate(R.layout.list_item_timesheet, viewGroup, false);

        // Get subject element
        TextView subject =
                (TextView) rowView.findViewById(R.id.subject);

        // Get dateRange element
        TextView dateRange =
                (TextView) rowView.findViewById(R.id.dateRange);

        // Get button
        Button btnDetails = (Button) rowView.findViewById(R.id.btnDetails);

        final Timesheet ts = (Timesheet) getItem(i);
        subject.setText("Submitted By " + ts.getUsername());
        dateRange.setText(ts.getStartDate() + " - " + ts.getEndDate());

        btnDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, DetailTimesheetActivity.class);
                intent.putExtra("PM_NAME", pmName);
                intent.putExtra("TASK_ID", ts.getTaskId());
                intent.putExtra("USER_NAME", ts.getUsername());
                intent.putExtra("START_DATE", ts.getStartDate());
                intent.putExtra("END_DATE", ts.getEndDate());
                mContext.startActivity(intent);
            }
        });
        return rowView;
    }

}
