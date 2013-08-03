package cz.muni.fi.japanesedictionary.engine;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import cz.muni.fi.japanesedictionary.R;
import cz.muni.fi.japanesedictionary.entity.DrawerItem;


/**
 * Created by Jarek on 28.7.13.
 */
public class DrawerAdapter extends ArrayAdapter<DrawerItem> {
    private Context mContext;
    private LayoutInflater mInflater;


    public DrawerAdapter(Context _context){
        super(_context, R.layout.drawer_list_item);
        mContext = _context;
        mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    public void setData(List<DrawerItem> data) {
        clear();
        if (data != null) {
            for (DrawerItem drawer : data) {
                add(drawer);
            }
        }
        notifyDataSetChanged();
    }

    /**
     * Constructs view for item in list.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DrawerItem drawerItem = getItem(position);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.drawer_list_item, parent,false);
        }

        ImageView icon = (ImageView) convertView.findViewById(R.id.drawer_item_icon);
        TextView text = (TextView) convertView.findViewById(R.id.drawer_item_text);
        icon.setImageResource(drawerItem.getIconResource());
        text.setText(drawerItem.getName());

        return convertView;
    }
}
