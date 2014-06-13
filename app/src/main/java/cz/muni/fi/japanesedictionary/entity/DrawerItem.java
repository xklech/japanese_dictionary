package cz.muni.fi.japanesedictionary.entity;

/**
 * Created by Jarek on 28.7.13.
 */
public class DrawerItem {

    private String mName;

    private int mIconResource;


    public int getIconResource() {
        return mIconResource;
    }

    public DrawerItem setIconResource(int mIconResource) {
        this.mIconResource = mIconResource;
        return this;
    }

    public String getName() {
        return mName;
    }

    public DrawerItem setName(String mName) {
        this.mName = mName;
        return this;
    }


}
