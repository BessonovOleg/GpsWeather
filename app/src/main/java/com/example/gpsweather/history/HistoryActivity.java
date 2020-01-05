package com.example.gpsweather.history;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.myapplication2.R;
import com.example.gpsweather.dao.AppDatabase;
import com.example.gpsweather.dao.DataBaseFactory;
import com.example.gpsweather.dao.HistoryRecord;
import com.example.gpsweather.dao.HistoryRecordDao;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {

    private AppDatabase db;
    private ListView historyList;
    private ArrayList<HistoryRecord> historyRecordsList;
    private HistoryRowAdapter historyRowAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        DataBaseFactory dataBaseFactory = new DataBaseFactory(getApplicationContext());
        db = dataBaseFactory.getDataBase();

        historyList = (ListView)findViewById(R.id.historyList);

        DataLoader dataLoader = new DataLoader();
        dataLoader.execute();

        historyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HistoryRecord selectedRow = historyRecordsList.get(position);
                Intent intent = new Intent();
                intent.putExtra(HistoryRecord.class.getSimpleName(),selectedRow);
                setResult(RESULT_OK,intent);
                finish();
            }
        });

        SwipeDismissListViewTouchListener touchListener =
                new SwipeDismissListViewTouchListener(
                        historyList,
                        new SwipeDismissListViewTouchListener.DismissCallbacks() {
                            @Override
                            public boolean canDismiss(int position) {
                                return true;
                            }

                            @Override
                            public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                                for (int position : reverseSortedPositions) {
                                    deleteRecord(historyRecordsList.get(position));
                                    historyRecordsList.remove(position);
                                    historyRowAdapter.notifyDataSetChanged();
                                }
                            }
                        });
            historyList.setOnTouchListener(touchListener);
    }

    private void deleteRecord(HistoryRecord historyRecord){
        if(historyRecord != null && db != null){
            HistoryRecordDao historyRecordDao = db.historyRecordDao();
            new DeleteHistoryRecord(historyRecordDao,this).execute(historyRecord);
        }
    }

    private static class DeleteHistoryRecord extends AsyncTask<HistoryRecord,Void,Void>{
        private HistoryRecordDao historyRecordDao;
        private Context ctx;
        public ProgressDialog dialog;

        public DeleteHistoryRecord(HistoryRecordDao dao,Context context){
            historyRecordDao = dao;
            ctx = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(ctx);
            dialog.setMessage("удаление...");
            dialog.setIndeterminate(true);
            dialog.setCancelable(true);
            dialog.show();
        }

        @Override
        protected Void doInBackground(final HistoryRecord... historyRecords) {
            historyRecordDao.delete(historyRecords[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dialog.dismiss();
        }
    }

    private class DataLoader extends AsyncTask<Void,Void,Void>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (db != null) {
                HistoryRecordDao historyRecordDao = db.historyRecordDao();
                historyRecordsList = (ArrayList<HistoryRecord>) historyRecordDao.getAll();
                historyRowAdapter = new HistoryRowAdapter(getApplicationContext(), historyRecordsList);
                historyList.setAdapter(historyRowAdapter);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }
}
