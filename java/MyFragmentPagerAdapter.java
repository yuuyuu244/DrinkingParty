package jjj.drinkingparty;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

/**
 * FragmentPagerAdapter は Fragment をアイテムとして表示する PagerAdapter.
 *
 * @author Tatsuya_Eshiro
 * @version 1.0
 * @see android.support.v4.app.Fragment
 * @see android.support.v4.app.FragmentManager
 * @see android.support.v4.app.FragmentPagerAdapter
 * @see android.support.v4.view.ViewPager
 * @see <a href = "http://dev.classmethod.jp/smartphone/android/android-tips-31-fragment-pager-adapter/">Android Tips #31 ViewPager で Fragment を使う</a>
 * @see <a href = "https://rakuishi.com/archives/6645/">[Android] FragmentPagerAdapter で Fragment のページ切り替えを実装する</a>
 * @since 1.0
 */
public class MyFragmentPagerAdapter
        extends FragmentPagerAdapter implements ViewPager.OnPageChangeListener {

    /**
     * コンストラクタ constructor.
     * <br>親クラスの読み込み - Accessing Superclass Members
     *
     * @since 1.0
     */
    public MyFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    /**
     * 画面切り替え - change pages.
     *
     * @param i ページ番号
     * @return Fragmentのインスタンス
     * @author Tatsuya_Eshiro
     * @version 1.0
     * @since 1.0
     */
    @Override
    public Fragment getItem(int i) {
        //画面切り替え
        switch (i) {
            case 0:
                return new CallFragment();
            case 1:
                return new ProfFragment();
            default:
                return new ManualFragment();
        }

    }

    /**
     * Fragmentで利用するアイテムの数を返す - return the number of items which is used in Fragment.
     * <br>このメソッドは必ずoverrideしないといけない.
     *
     * @return ウィンドウ数
     * @author Tatsuya_Eshiro
     * @version 1.0
     * @since 1.0
     */
    @Override
    public int getCount() {
        //ウィンドウ数
        return 3;
    }

    /**
     * ページのタイトルを渡すメソッド.
     *
     * @param position 表示されているページの番号
     * @return ページのタイトル
     * @author Tatsuya_Eshiro
     * @version 1.0
     * @since 1.0
     */
    @Override
    public String getPageTitle(int position) {
        //タイトル選択
        switch (position) {
            case 0:
                return MyApplication.getContext().getString(R.string.home);
            case 1:
                return MyApplication.getContext().getString(R.string.profile);
            default:
                return MyApplication.getContext().getString(R.string.manual);
        }
    }

}