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

    public DrawerAdapter(Context _context){
        super(_context, R.layout.drawer_list_item);
        mContext = _context;
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
        DrawerViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.drawer_list_item, parent,false);
            holder = new DrawerViewHolder();
            holder.icon = (ImageView) convertView.findViewById(R.id.drawer_item_icon);
            holder.text = (TextView) convertView.findViewById(R.id.drawer_item_text);
            convertView.setTag(holder);
        } else {
            holder = (DrawerViewHolder) convertView.getTag();
        }

        DrawerItem drawerItem = getItem(position);
        holder.icon.setImageResource(drawerItem.getIconResource());
        holder.text.setText(drawerItem.getName());

        return convertView;
    }

    private static class DrawerViewHolder {
        ImageView icon;
        protected TextView text;
    }
}
