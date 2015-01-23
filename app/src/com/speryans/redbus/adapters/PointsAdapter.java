package com.speryans.redbus.adapters;

import com.speryans.redbus.R;
import com.speryans.redbus.classes.En5EstoyActivity;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PointsAdapter extends CursorAdapter {

	public PointsAdapter(Context context, Cursor c, boolean autoRequery) {
		super(context, c, autoRequery);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		TextView label = (TextView)view.findViewById(R.id.label);
		label.setText(cursor.getString(cursor.getColumnIndex("address")));
		
		TextView sublabel = (TextView)view.findViewById(R.id.sublabel);
		sublabel.setText(cursor.getString(cursor.getColumnIndex("barrio")));
		
		TextView icon = (TextView) view.findViewById(R.id.iconTxt);
		icon.setTypeface(En5EstoyActivity.icons);
		icon.setText("\uf1ff");
		
		((LinearLayout) view.findViewById(R.id.rightLayout)).setVisibility(View.GONE);
	}
 
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View v = inflater.inflate(R.layout.row, parent, false);
		bindView(v, context, cursor);
		return v;
	}

}
