package caltrack.blub.com.caltrack.PageAdapter;

import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;

import caltrack.blub.com.caltrack.R;


public class FragmentPageAdapter extends FragmentActivity
{
    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private static ViewPager mPager;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PagerAdapter mPagerAdapter;

    static FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.viewpager_adapter);

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = findViewById(R.id.loginViewPager);
        fragmentManager = getSupportFragmentManager();
        mPagerAdapter = new ScreenSlidePagerAdapter(fragmentManager);
        mPager.setAdapter(mPagerAdapter);
    }

    public static void changeFragment(int item)
    {
        mPager.setCurrentItem(item);
    }

    @Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }
}
