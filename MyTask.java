package jjj.drinkingparty;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import static android.content.ContentValues.TAG;


/**
 * Created by 152111 on 2016/11/22.
 */

public class MyTask extends AsyncTask<Object, Void, ArrayList>
{

    ArrayList<String> list;
    Fragment frag = null;
    Activity act = null;
    public MyTask(Fragment frag){
        this.frag = frag;
    }
    public MyTask(Activity act) { this.act = act; }
    public MyTask(){
        frag = null;
    }

    CountDownLatch cdl;

    /**
     *
     *
     * @param value
     * @return
     */
    protected ArrayList doInBackground(Object... value) {
        // DB接続と書き込み
        ResultSet rs;
        if(frag != null) {
            cdl = CallFragment.cdl;
        }else if(act != null){
            cdl = MainActivity.cdl;
        }
        list = new ArrayList<>();

        try{
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn= DriverManager.getConnection("jdbc:mysql://10.15.121.14:3306/drinkingparty","root","root");
            Statement stmt=conn.createStatement();
            String sql = (String)value[0];
            switch((String)value[1]){
                case "1":
                    stmt.execute(sql);
                    break;
                case "2":
                    stmt.executeUpdate(sql);
                    break;
                case "3":
                    rs = stmt.executeQuery(sql);
                    rs.next();
                    list.add(rs.getString((String)value[2]));
                    break;
                case "4":
                    rs = stmt.executeQuery(sql);
                    rs.next();
                    list.add(String.valueOf(rs.getInt((String)value[2])));
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

        }catch(Exception e){
            Log.d(TAG,"Errorr"+e);
        }

        if(frag != null) {
            CallFragment.changeList(list);
            cdl.countDown();
        }
        if(act != null) {
            MainActivity.changeList(list);
            cdl.countDown();
        }

        return list;
    }

}
