//
//
// Copyright 2017 Kii Corporation
// http://kii.com
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
//
package com.kii.sample.balance.list;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.kii.cloud.storage.KiiObject;
import com.kii.sample.balance.R;
import com.kii.sample.balance.kiiobject.Field;

/**
 * This class is the list adapter for KiiObjects.
 */
public class KiiObjectAdapter extends BaseAdapter {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final NumberFormat AMOUNT_FORMAT = NumberFormat.getCurrencyInstance(Locale.US);
    
    private int totalAmount;
    private List<KiiObject> items = new ArrayList<KiiObject>();

    /**
     * Add a KiiObject to the end of the list.
     * @param item
     */
    void add(KiiObject item) {
        items.add(item);
        addTotalAmount(item);
    }
    
    /**
     * Update a KiiObject with its ID.
     * @param object
     * @param objectId
     */
    void updateObject(KiiObject object, String objectId) {
        // Delete the existing KiiObject and get its position.
        int position = delete(objectId);
        if (position == -1) { return; } // The target KiiObject is not found.
        items.add(position, object);
    }
    
    /**
     * Delete a KiiObject with its ID.
     * @param objectId
     * @return position / -1 if the target KiiObject is not found
     */
    int delete(String objectId) {

        // get position
        int position = -1;
        for (int i = 0 ; i < items.size() ; ++i) {
            String uri = items.get(i).toUri().toString();
            String itemID = uri.substring(uri.lastIndexOf('/') + 1);
            if (itemID.equals(objectId)) {
                position = i;
                break;
            }
        }
        if (position == -1) { return -1; } // not found
        items.remove(position);
        return position;
    }
    
    /**
     * Add the amount set in this KiiObject to the balance.
     * @param item
     */
    private void addTotalAmount(KiiObject item) {
        int amount = item.getInt(Field.AMOUNT);
        int type = item.getInt(Field.TYPE);
        if (type == Field.Type.INCOME) {
            totalAmount += amount;
        } else {
            totalAmount -= amount;
        }
    }

    /**
     * Remove all KiiObjects from the list.
     */
    void clear() {
        items.clear();
    }

    /**
     * Calculate the balance.
     */
    void computeTotalAmount() {
        totalAmount = 0;
        for (KiiObject item : items) {
            addTotalAmount(item);
        }
    }
    /**
     * @return the balance
     */
    int getTotalAmount() {
        return totalAmount;
    }
    
    /*
     * (non-Javadoc)
     * @see android.widget.Adapter#getCount()
     */
    @Override
    public int getCount() {
        return items.size();
    }

    /*
     * (non-Javadoc)
     * @see android.widget.Adapter#getItem(int)
     */
    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    /*
     * (non-Javadoc)
     * @see android.widget.Adapter#getItemId(int)
     */
    @Override
    public long getItemId(int position) {
        return 0;
    }

    /*
     * (non-Javadoc)
     * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);
            
            View layout = inflater.inflate(R.layout.list_item, null);

            // Set textViews to ViewHolder.
            TextView nameText = (TextView) layout.findViewById(R.id.text_name);
            TextView amountText = (TextView) layout.findViewById(R.id.text_amount);
            TextView dateText = (TextView) layout.findViewById(R.id.text_date);
            layout.setTag(new ViewHolder(nameText, amountText, dateText));
            
            convertView = layout;
        }
        ViewHolder holder = (ViewHolder) convertView.getTag();
        KiiObject object = items.get(position);
        
        TextView nameText = holder.nameText;
        nameText.setText(object.getString(Field.NAME));
        
        TextView amountText = holder.amountText;
        int type = object.getInt(Field.TYPE);
        if (type == Field.Type.INCOME) {
            amountText.setText(AMOUNT_FORMAT.format(object.getInt(Field.AMOUNT) / 100.0));
            amountText.setTextColor(Color.BLACK);
        } else {
            amountText.setText(AMOUNT_FORMAT.format(-object.getInt(Field.AMOUNT) / 100.0));
            amountText.setTextColor(Color.RED);
        }
        
        TextView dateText = holder.dateText;
        dateText.setText(DATE_FORMAT.format(new Date(object.getCreatedTime())));

        return convertView;
    }
    
    private static class ViewHolder {
        TextView nameText;
        TextView amountText;
        TextView dateText;
        ViewHolder(TextView nameText, TextView amountText, TextView dateText) {
            this.nameText = nameText;
            this.amountText = amountText;
            this.dateText = dateText;
        }
    }

}
