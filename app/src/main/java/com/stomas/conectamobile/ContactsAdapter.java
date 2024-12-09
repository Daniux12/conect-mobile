package com.stomas.conectamobile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

public class ContactsAdapter extends ArrayAdapter<String> {
    private Context context;
    private List<String> contactsList;

    public ContactsAdapter(Context context, List<String> contactsList) {
        super(context, R.layout.contact_item, contactsList);
        this.context = context;
        this.contactsList = contactsList;
    }

    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.contact_item, parent, false);
            holder = new ViewHolder();
            holder.tvContactEmail = convertView.findViewById(R.id.tv_contact_email);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String contactEmail = contactsList.get(position);
        if (contactEmail != null) {
            holder.tvContactEmail.setText(contactEmail);
        }

        return convertView;
    }

    // ViewHolder Pattern for better performance
    static class ViewHolder {
        TextView tvContactEmail;
    }
}
