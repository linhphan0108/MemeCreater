package uit.linh.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import uit.linh.ui.OriginMemeFragment;
import uit.linh.ui.RecentlyMemeFragment;
import uit.linh.ui.NewMemeFragment;
import uit.linh.ui.StampFragment;

/**
 *
 * Created by linh on 11/06/2015.
 */
public class ViewPagerAdapter  extends FragmentPagerAdapter {

    CharSequence titles[];//this will store the titles of tabs which are going to passed when ViewPagerAdapter is created
    JSONObject jMemes;


    public ViewPagerAdapter(FragmentManager fm, CharSequence[] titles, JSONObject jMemes) {
        super(fm);
        this.titles = titles;
        this.jMemes = jMemes;
    }

    @Override
    public int getCount() {
        return titles.length;
    }


    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }

    @Override
    public Fragment getItem(int position) {

        switch (position){
            case 0:
                try {
                    String jsonString = jMemes.getJSONArray("meme new").toString();
                    return NewMemeFragment.newInstance(jsonString);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return  NewMemeFragment.newInstance(null);

            case 1:
                try {
                    String jsonString = jMemes.getJSONArray("stamp").toString();
                    return StampFragment.newInstance(jsonString);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return  StampFragment.newInstance(null);

            case 2:
                try {
                    String jsonString = jMemes.getJSONArray("meme origin").toString();
                    return OriginMemeFragment.newInstance(jsonString);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return  OriginMemeFragment.newInstance(null);

            case 3:
                return RecentlyMemeFragment.newInstance();

            default:
                break;
        }
        return null;
    }
}
