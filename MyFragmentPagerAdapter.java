package jjj.drinkingparty;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class MyFragmentPagerAdapter
        extends FragmentPagerAdapter {

    public MyFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        //画面切り替え
        switch(i){
            case 0:
                return new ProfFragment();
            case 1:
                return new CallFragment();
            default:
                return new ManualFragment();
        }

    }

    @Override
    public int getCount() {
        //ウィンドウ数
        return 3;
    }

    @Override
    public  String getPageTitle(int position) {
        //タイトル選択
        switch(position){
            case 0:
                return MyApplication.getContext().getString(R.string.prof);
            case 1:
                return MyApplication.getContext().getString(R.string.home);
            default:
                return MyApplication.getContext().getString(R.string.use);
        }
    }

}