package com.htlhl.tourismus_hl;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class WahlradCustomRow extends ArrayAdapter<String> {
    private final Activity context;
    private final String[] name;
    private final Integer imageId;
    public static int selectedRow = -1;
    ListView lvWahlrad;
    ImageView imageView;

    public WahlradCustomRow(Activity context,
                            String[] name, Integer imageId) {
        super(context, R.layout.wahlrad_custom_row, name);
        this.context = context;
        this.name = name;
        this.imageId = imageId;
    }
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.wahlrad_custom_row, null, true);

        TextView text = (TextView) rowView.findViewById(R.id.tvWahlradList);
        imageView = (ImageView) rowView.findViewById(R.id.imgWahlradList);

        text.setText(name[position]);
        imageView.setImageResource(imageId);

        if((position == selectedRow || position == FragmentWahlradRouten.posLogRoute || position == FragmentWahlradStation.posLogStation)) {
            imageView.setVisibility(View.VISIBLE);
        }else {
            imageView.setVisibility(View.INVISIBLE);
        }
        return rowView;
    }
}
