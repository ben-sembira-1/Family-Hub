package com.example.sembi.logingui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class FamilyTreeNodeUIListAdapter extends ArrayAdapter<FamilyTreeNodeUIModel> {

    private Context mContext;
    private ArrayList<FamilyTreeNodeUIModel> records;

    public FamilyTreeNodeUIListAdapter(@NonNull Context context, ArrayList<FamilyTreeNodeUIModel> records) {
        super(context, 0, records);
        mContext = context;
        this.records = records;
    }

    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
        View listItem = convertView;
        if(listItem == null){
            listItem = LayoutInflater.from(mContext).inflate(R.layout.family_tree_node_ui,null);

        }
        FamilyTreeNodeUIModel current = records.get(position);
        TextView name = listItem.findViewById(R.id.FamilyTreeNodeUI_TV);
        ImageView IV = listItem.findViewById(R.id.FamilyTreeNodeUI_IV);
        name.setText(current.getName());
        IV.setImageURI(current.getIV());
        return listItem;
    }
}

