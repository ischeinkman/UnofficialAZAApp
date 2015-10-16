package org.ramonaza.unofficialazaapp.helpers.ui.other;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;

import org.ramonaza.unofficialazaapp.R;
import org.ramonazaapi.interfaces.InfoWrapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by ilanscheinkman on 7/24/15.
 */
public class InfoWrapperCheckboxListAdapter extends ArrayAdapter<InfoWrapper> {

    List<Boolean> itemChecked;

    public InfoWrapperCheckboxListAdapter(Context context) {
        super(context, 0, new ArrayList<InfoWrapper>());
        this.itemChecked = new ArrayList<Boolean>();
    }

    public InfoWrapperCheckboxListAdapter(Context context, InfoWrapper[] objects) {
        super(context, 0, objects);
        this.itemChecked = new ArrayList<Boolean>(objects.length);
        for (int i = 0; i < objects.length; i++) itemChecked.add(false);
    }

    @Override
    public void add(InfoWrapper object) {
        super.add(object);
        this.itemChecked.add(false);
    }

    @Override
    public void addAll(Collection<? extends InfoWrapper> collection) {
        super.addAll(collection);
        for (int i = 0; i < collection.size(); i++) itemChecked.add(false);
    }

    @Override
    public void addAll(InfoWrapper... items) {
        super.addAll(items);
        for (int i = 0; i < items.length; i++) itemChecked.add(false);
    }

    @Override
    public void clear() {
        super.clear();
        this.itemChecked.clear();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.infowrapper_checkbox, parent, false);
            viewHolder.checkbox = (CheckBox) convertView.findViewById(R.id.infowrappercheckbox);
            convertView.setTag(viewHolder);
        } else viewHolder = (ViewHolder) convertView.getTag();
        viewHolder.checkbox.setText(getItem(position).getName());
        viewHolder.checkbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox cb = (CheckBox) v.findViewById(R.id.infowrappercheckbox);
                if (cb.isChecked()) {
                    itemChecked.set(position, true);
                } else if (!cb.isChecked()) {
                    itemChecked.set(position, false);
                }
            }
        });
        viewHolder.checkbox.setChecked(itemChecked.get(position));
        return convertView;
    }

    public InfoWrapper[][] getBoth() {
        ArrayList<InfoWrapper> checked = new ArrayList<InfoWrapper>();
        ArrayList<InfoWrapper> unchecked = new ArrayList<InfoWrapper>();
        int bsize = itemChecked.size();
        for (int i = 0; i < bsize; i++) {
            if (itemChecked.get(i).booleanValue()) checked.add(getItem(i));
            else unchecked.add(getItem(i));
        }
        InfoWrapper[] checkedArray = checked.toArray(new InfoWrapper[checked.size()]);
        InfoWrapper[] uncheckedArray = unchecked.toArray(new InfoWrapper[unchecked.size()]);
        return new InfoWrapper[][]{checkedArray, uncheckedArray};
    }

    private static class ViewHolder {
        public CheckBox checkbox;
    }
}
