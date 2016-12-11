package com.lukechenshui.beatpulse;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.google.common.collect.HashBiMap;
import com.lukechenshui.beatpulse.layout.BrowsingActivity;
import com.lukechenshui.beatpulse.layout.MainActivity;
import com.lukechenshui.beatpulse.layout.PlayActivity;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.util.HashMap;

/**
 * Created by luke on 12/10/16.
 */

public class DrawerInitializer {
    private final static HashBiMap<Integer, Class> drawerActivities = HashBiMap.create();
    private static int selectedItemPos;

    public static Drawer createDrawer(final Context context, final Activity activity, final Toolbar toolbar) {
        PrimaryDrawerItem item1 = new PrimaryDrawerItem().withIdentifier(1).withName(R.string.drawer_item_home);
        SecondaryDrawerItem item2 = new SecondaryDrawerItem().withIdentifier(2).withName(R.string.drawer_item_browse);
        SecondaryDrawerItem item3 = new SecondaryDrawerItem().withIdentifier(3).withName(R.string.drawer_item_now_playing);
        final HashMap<Integer, String> drawerItemNames = new HashMap<>();

        drawerItemNames.put(Config.HOME_DRAWER_ITEM_POS, "Home");
        drawerItemNames.put(Config.BROWSE_DRAWER_ITEM_POS, "Browse");
        drawerItemNames.put(Config.NOW_PLAYING_DRAWER_ITEM_POS, "Now Playing");


        drawerActivities.put(Config.HOME_DRAWER_ITEM_POS, MainActivity.class);
        drawerActivities.put(Config.BROWSE_DRAWER_ITEM_POS, BrowsingActivity.class);
        drawerActivities.put(Config.NOW_PLAYING_DRAWER_ITEM_POS, PlayActivity.class);

        DrawerBuilder drawerBuilder = new DrawerBuilder()
                .withActivity(activity)
                .withToolbar(toolbar)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        // do something with the clicked item :D
                        Toast.makeText(context, drawerItemNames.get(position), Toast.LENGTH_SHORT).show();
                        Class activity = drawerActivities.get(position);
                        if (activity != null) {
                            Intent intent = new Intent(context, activity);
                            context.startActivity(intent);
                            ((Activity) context).finish();
                        }
                        /*
                        Returning true causes the drawer to remain open after clicking an item.
                        We want it to close, so we return false
                        */
                        setSelectedItemPos(position);
                        return false;
                    }
                })
                .addDrawerItems(
                        item1,
                        item2,
                        item3
                );
        Drawer drawer = drawerBuilder.build();
        drawer.setSelectionAtPosition(getSelectedItemPos(), false);
        return drawer;
    }

    public static int getSelectedItemPos() {
        return selectedItemPos;
    }

    public static void setSelectedItemPos(int selectedItemPos) {
        DrawerInitializer.selectedItemPos = selectedItemPos;
    }

    public static HashBiMap<Integer, Class> getDrawerActivities() {
        return drawerActivities;
    }
}
