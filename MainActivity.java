package jjj.drinkingparty;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import io.skyway.Peer.Browser.Navigator;
import io.skyway.Peer.Peer;


/**
 * メイン画面のActivity作成.
 * <br><ul>
 *     <li>コール画面、プロフィール画面、使い方説明画面の生成</li>
 *     <li>DBへのIDの登録</li>
 *     <li>DBからIDの削除</li>
 * </ul>.
 *
 * @author 津田
 * @version 1.0
 * @since 1.0
 */
public class MainActivity extends FragmentActivity {
    /** SQL文の保持する文字列 */
    String sql;
    /** SQLつなぐ際の非同期の処理を行うクラス */
    MyTask task;
    /** DB接続後のデータを渡すArrayList */
    static ArrayList<String> callbacklist = new ArrayList<>();
    /** 非同期処理を待っとく奴 */
    static CountDownLatch cdl;
    /** データ保持クラスのインスタンスを取得 */
    CallData callData = CallData.getInstance();
    /** 自分のIDを設定する */
    String myId = callData.getMyId();
    /** ホームボタンとか履歴ボタンとか押したときのインスタンス変数 */
    HomeButtonReceive homeButtonReceive;

    @Override
    protected void onCreate(Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_main);
        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(
                new jjj.drinkingparty.MyFragmentPagerAdapter(
                        getSupportFragmentManager()));

        viewPager.setCurrentItem(1);

        homeButtonReceive = new HomeButtonReceive();
        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        this.registerReceiver(homeButtonReceive, iFilter);
    }

    /**
     * ホームボタンを押したときDBを削除する処理.
     * <br>ホームボタンが押されたとき、
     * <br>DBの自分のIDが一致する行を削除する.
     *
     * @version 1.0
     * @since 1.0
     *
     */
    public class HomeButtonReceive extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent arg1){
            callData = CallData.getInstance();
            myId = callData.getMyId();
            sql = "delete from session where peerid = '" + myId + "'";
            task = new MyTask();
            task.execute(sql, "2");
        }
    }

    @Override
    protected void onStop(){
        super.onStop();

    }

    /**
     * 二度目以降にMainに入ってきたときの処理.
     * <table border=1>
     *     <tr>
     *         <th>通話画面から入ったとき</th><td>通話終了ダイアログの表示</td>
     *     </tr>
     *     <tr>
     *         <th><ul><li>ホームボタンから戻り</li><br><li>履歴ボタンからの戻り</li><li>その他</li></ul></th>
     *         <td>DBに自分のIDが登録されているか確認し、
     *         <br>登録されていなければ、DBに自分のIDを登録する</td>
     *     </tr>
     *     </table>.
     *
     * @version 1.0
     * @since 1.0
     * @see CallData#myId
     *
     */
    @Override
    public void onRestart(){
        super.onRestart();
        callData = CallData.getInstance();
        myId = callData.getMyId();

        callData=CallData.getInstance();
        boolean backFlag = callData.getBackFlag();
        //通話画面から戻ってきたときif文内の処理に入る
        if (backFlag) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setMessage(MyApplication.getContext().getString(R.string.hangup));
            // アラートダイアログの肯定ボタンがクリックされた時に呼び出されるコールバックリスナーを登録します
            alertDialogBuilder.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
            alertDialogBuilder.show();
            callData.setBackFlag(false);
        } else {
            cdl = new CountDownLatch(1);
            sql = "select count(*) from session where peerid = '" + myId + "'";
            task = new MyTask(MainActivity.this);
            task.execute(sql, "4", "count(*)");
            try {
                cdl.await();
            } catch (InterruptedException e) {
            }
            if (Integer.parseInt(callbacklist.get(0)) == 0) {
                SharedPreferences sp = getSharedPreferences("pref", Context.MODE_PRIVATE);
                //Preferenceから自分が登録している名前を取得する
                String name = sp.getString("name", null);
                //Preferenceから自分が登録している性別を取得する
                int sex = sp.getInt("sex", 0);
                sql = "insert into session(peerid,name,sex) values('" + myId + "','" + name + "'," + sex + ")";
                task = new MyTask(MainActivity.this);
                task.execute(sql, "1");
            }
        }
    }

    /**
     * DBデータのArrayListを読み込むセッター.
     *
     * @param list DBデータを格納したArrayList
     */
    public static void changeList(ArrayList<String> list){
        callbacklist = list;
    }

    @Override
    public void onResume(){
        super.onResume();

    }

    @Override
    public void onPause(){
        super.onPause();

    }


    /**
     * DBから自分のIDを削除
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        callData = CallData.getInstance();
        myId = callData.getMyId();
        sql = "delete from session where peerid = '" + myId + "'";
        task = new MyTask();
        task.execute(sql, "2");
        unregisterReceiver(homeButtonReceive);
        destroypeer();

    }

    /**
     * 確立したPeerを削除するメソッド.
     */
    void destroypeer(){
        Navigator.terminate();
        callData = CallData.getInstance();
        Peer _peer = callData.getPeer();
        if (null != _peer) {
            unsetPeerCallback(_peer);

            if (false == _peer.isDisconnected) {
                _peer.disconnect();

            }

            if (false == _peer.isDestroyed) {
                _peer.destroy();
            }

            _peer = null;
        }
    }
    //Unset peer callback
    void unsetPeerCallback(Peer peer)
    {
        peer.on(Peer.PeerEventEnum.OPEN, null);
        peer.on(Peer.PeerEventEnum.CONNECTION, null);
        peer.on(Peer.PeerEventEnum.CALL, null);
        peer.on(Peer.PeerEventEnum.CLOSE, null);
        peer.on(Peer.PeerEventEnum.DISCONNECTED, null);
        peer.on(Peer.PeerEventEnum.ERROR, null);
    }
}