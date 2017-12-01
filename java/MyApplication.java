package jjj.drinkingparty;

import android.app.Application;
import android.content.Context;

/**
 * 全activityにContextを渡す.
 * <br>Contextを呼び出せる.
 *
 * @author Tatsuya_Eshiro
 * @vesion 1.1
 * @see android.app.Application
 * @see android.content.Context
 * @since 1.0
 */
public class MyApplication extends Application {

    /**
     * ApplicationのContext
     */
    private static Context mContext;

    /**
     * Application生成時に、Contextの格納を行う.
     *
     * @author Tatsuya_Eshiro
     * @since 1.0
     */
    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

    /**
     * Contextのゲッター.
     * <br>ApplicationのContextを呼び出し元に返す.
     *
     * @return Context ApplicationのContext
     * @author Tatsuya_Eshiro
     * @since 1.0
     */
    public static Context getContext() {
        return mContext;
    }
}
