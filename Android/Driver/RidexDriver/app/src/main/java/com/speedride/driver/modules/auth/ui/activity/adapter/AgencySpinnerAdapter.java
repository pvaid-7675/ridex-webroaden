package com.speedride.driver.modules.auth.ui.activity.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.appcompat.widget.AppCompatTextView;

import com.speedride.driver.R;
import com.speedride.driver.model.AgencyModel;

import java.util.List;

public class AgencySpinnerAdapter extends ArrayAdapter {
    Context context;
    List<AgencyModel.Data> list;
    public AgencySpinnerAdapter(Context context, int textViewResourceId,
                                List<AgencyModel.Data> list) {
        super(context, textViewResourceId, list);
        this.context = context;
        this.list = list;

    }

    public View getCustomView(int position, View convertView,
                              ViewGroup parent) {

// Inflating the layout for the custom Spinner
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View layout = inflater.inflate(R.layout.agency_list_layout, parent, false);
        AppCompatTextView tvName = layout.findViewById(R.id.tvAgencyName);
        AgencyModel.Data item = list.get(position);
        tvName.setText(item.getCompany_name());
        return layout;
    }

    // It gets a View that displays in the drop down popup the data at the specified position
    @Override
    public View getDropDownView(int position, View convertView,
                                ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    // It gets a View that displays the data at the specified position
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }
}