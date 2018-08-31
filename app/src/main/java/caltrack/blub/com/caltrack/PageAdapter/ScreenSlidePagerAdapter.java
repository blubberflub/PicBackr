package caltrack.blub.com.caltrack.PageAdapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import caltrack.blub.com.caltrack.Utilities.Constants;
import caltrack.blub.com.caltrack.Fragments.LoginFragment;
import caltrack.blub.com.caltrack.Fragments.RegisterFragment;

/**
 * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
 * sequence.
 */
public class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
    public ScreenSlidePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    // Returns the fragment to display for that page
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new LoginFragment();
            case 1:
                return new RegisterFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return Constants.NUM_PAGES;
    }
}