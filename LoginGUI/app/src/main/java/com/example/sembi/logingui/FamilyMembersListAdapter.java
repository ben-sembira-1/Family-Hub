package com.example.sembi.logingui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class FamilyMembersListAdapter extends ArrayAdapter<MedicalRecordModel> {

    private Context mContext;
    private ArrayList<MedicalRecordModel> records;

    public FamilyMembersListAdapter(@NonNull Context context, ArrayList<MedicalRecordModel> records) {
        super(context, 0, records);
        mContext = context;
        this.records = records;
    }

    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null) {
            listItem = LayoutInflater.from(mContext).inflate(R.layout.family_member_item_layout, null);

        }
//        title.setText(current.getTitle());
//        content.setText(current.getContent());
        return listItem;
    }
}
