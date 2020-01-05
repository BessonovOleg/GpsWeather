package com.example.gpsweather.history;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.myapplication2.R;
import com.example.gpsweather.dao.HistoryRecord;

import java.util.ArrayList;
import java.util.Date;

public class HistoryRowAdapter extends BaseAdapter {
    private Context ctx;
    private LayoutInflater lInflater;
    private ArrayList<HistoryRecord> objects;

    public HistoryRowAdapter(Context context,ArrayList<HistoryRecord> historyRecords){
        ctx = context;
        objects = historyRecords;
        lInflater = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public Object getItem(int position) {
        return objects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        if(view == null){
            view = lInflater.inflate(R.layout.list_item,parent,false);
        }

        HistoryRecord record = getHistoryRecord(position);
        ((TextView) view.findViewById(R.id.li_rowNo)).setText(String.valueOf(position+1));
        ((TextView) view.findViewById(R.id.li_locationName)).setText(record.locationName);
        ((TextView) view.findViewById(R.id.li_lbLat)).setText("Lat: " + String.valueOf(record.lat));
        ((TextView) view.findViewById(R.id.li_lbLon)).setText("Lon: " + String.valueOf(record.lon));
        ((TextView) view.findViewById(R.id.li_timeLabel)).setText(dateFormat(record.dateEvent));
        return view;
    }

    private HistoryRecord getHistoryRecord(int position){
        return ((HistoryRecord) getItem(position));
    }

    private String dateFormat(Date date){
        String result = "";
        if(date != null){
          result = (android.text.format.DateFormat.format("yyyy-MM-dd hh:mm:ss", date)).toString();
        }
        return result;
    }
}
