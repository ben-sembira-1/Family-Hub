package com.example.sembi.logingui;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class MedicalRecordsListAdapter extends ArrayAdapter<MedicalRecordModel> {

    private Context mContext;
    private ArrayList<MedicalRecordModel> records;

    public MedicalRecordsListAdapter(@NonNull Context context, ArrayList<MedicalRecordModel> records) {
        super(context, 0, records);
        mContext = context;
        this.records = records;
    }

    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
        View listItem = convertView;
        if(listItem == null){
            listItem = LayoutInflater.from(mContext).inflate(R.layout.medical_record_item_layout,null);

        }
        MedicalRecordModel current = records.get(position);
        TextView title = listItem.findViewById(R.id.headerMedicalRecord);
        TextView content = listItem.findViewById(R.id.dataMedicalRecord);
        title.setText(current.getTitle());
        content.setText(current.getContent());
        return listItem;
    }
}
