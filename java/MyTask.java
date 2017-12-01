package jjj.drinkingparty;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import static jjj.drinkingparty.MyApplication.getContext;


/**
 * 非同期でデータベースに接続するクラス.
 *
 * @author Naoki_Tsuda
 * @version 1.1
 * @see android.app.Activity
 * @see android.content.Context
 * @see android.content.SharedPreferences
 * @see android.os.AsyncTask
 * @see android.support.v4.app.Fragment
 * @see java.sql.Connection
 * @see java.sql.DriverManager
 * @see java.sql.ResultSet
 * @see java.sql.Statement
 * @see java.util.ArrayList
 * @see java.util.concurrent.CountDownLatch
 * @see jjj.drinkingparty.MyApplication
 * @since 1.0
 */
public class MyTask extends AsyncTask<Object, Void, ArrayList> {
    /**
     * データベースから取り出したデータ保持リスト
     */
    ArrayList<String> list;
    /**
     * FragmentのJavaのやつから入ってきたときのやつ
     */
    Fragment frag = null;
    /**
     * ActivityのJavaのやつから入ってきたときのやつ
     */
    Activity act = null;

    /**
     * 受け渡されたFragmentをこのクラスのFragmentとしてセットする.
     *
     * @param frag 受け渡されたFragment
     * @author Naoki_Tsuda
     * @since 1.0
     */
    public MyTask(Fragment frag) {
        this.frag = frag;
    }

    /**
     * 受け渡されたActivityをこのクラスのActivityとしてセットする.
     *
     * @param act 受け渡されたActivity
     * @author Naoki_Tsuda
     * @since 1.0
     */
    public MyTask(Activity act) {
        this.act = act;
    }

    /**
     * コンストラクタ.
     * <br>fragの初期化.
     *
     * @since 1.0
     */
    public MyTask() {
        frag = null;
    }

    /**
     * CountDownLatchのインスタンス
     */
    CountDownLatch cdl;
    /**
     * SharedPreferencesのインスタンス
     */
    SharedPreferences sp = getContext().getSharedPreferences("pref", Context.MODE_PRIVATE);
    /**
     * IPアドレス
     */
    String IP = sp.getString("IP", "0");

    /**
     * 一番最初にはいるやつや by author(tsuda).
     *
     * @param value
     * @return
     */
    protected ArrayList doInBackground(Object... value) {
        // DB接続と書き込み
        ResultSet rs;
        if (frag != null) {
            cdl = CallFragment.cdl;
        } else if (act != null) {
            cdl = MainActivity.cdl;
        }
        list = new ArrayList<>();

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection("jdbc:mysql://" + IP + ":3306/drinkingparty", "root", "root");
            Statement stmt = conn.createStatement();
            String sql = (String) value[0];
            switch ((String) value[1]) {
                case "1":
                    stmt.execute(sql);
                    break;
                case "2":
                    stmt.executeUpdate(sql);
                    break;
                case "3":
                    rs = stmt.executeQuery(sql);
                    rs.next();
                    list.add(rs.getString((String) value[2]));
                    break;
                case "4":
                    rs = stmt.executeQuery(sql);
                    rs.next();
                    list.add(String.valueOf(rs.getInt((String) value[2])));
                    break;
                default:
                    rs = stmt.executeQuery(sql);
                    rs.next();
                    list.add(rs.getString("peerid"));
                    list.add(rs.getString("name"));
                    break;
            }
            stmt.close();
            conn.close();

        } catch (Exception e) {
        }

        if (frag != null) {
            CallFragment.changeList(list);
            cdl.countDown();
        }
        if (act != null) {
            MainActivity.changeList(list);
            cdl.countDown();
        }

        return list;
    }

}
