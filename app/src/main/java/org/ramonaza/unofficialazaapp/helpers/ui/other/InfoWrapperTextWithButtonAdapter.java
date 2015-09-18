package org.ramonaza.unofficialazaapp.helpers.ui.other;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;

import org.ramonaza.unofficialazaapp.R;
import org.ramonaza.unofficialazaapp.helpers.backend.InfoWrapper;

import java.util.ArrayList;

/**
 * An abstract adapter for the creation of lists of the form -Item Name -- Button -.
 * Created by ilanscheinkman on 7/27/15.
 */

public abstract class InfoWrapperTextWithButtonAdapter extends ArrayAdapter<InfoWrapper>{

    public InfoWrapperTextWithButtonAdapter(Context context){
        super(context,0,new ArrayList<InfoWrapper>());
    }

    /**
     * Method for getting the name of the button.
     * @return the name of the button.
     */
    public abstract String getButtonText();

    /**
     * What happens when the button is clicked.
     * @param info the infowrapper in the line the button was clicked
     */
    public abstract void onButton(InfoWrapper info);

    /**
     * What happens when the name is clicked.
     * @param info the infowrapper entry whose name was clicked
     */
    public abstract void onText(InfoWrapper info);

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final InfoWrapper info= getItem(position);
        ViewHolder viewHolder;
        if(convertView == null){
            viewHolder= new ViewHolder();
            convertView= LayoutInflater.from(getContext()).inflate(R.layout.infowrapper_textbutbut, parent, false);
            viewHolder.name=(Button) convertView.findViewById(R.id.infowrappername);
            viewHolder.button=(Button) convertView.findViewById(R.id.infowrapperbutton);
            viewHolder.button.setText(getButtonText());
            convertView.setTag(viewHolder);
        } else viewHolder=(ViewHolder) convertView.getTag();
        viewHolder.name.setText(info.getName());
        viewHolder.name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onText(info);
            }
        });
        viewHolder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButton(info);
            }
        });
        return convertView;
    }

    private static class ViewHolder{
        public Button name;
        public Button button;
    }
}
