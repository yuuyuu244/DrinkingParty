package jjj.drinkingparty;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import io.skyway.Peer.OnCallback;
import io.skyway.Peer.Peer;
import io.skyway.Peer.PeerOption;

/**
 * CALL画面（通話開始画面）.
 * <br>相手のIDを発見するか、された場合に通話画面に遷移を行う.
 *
 * @author Naoki_Tsuda
 * @version 1.1
 * @see android.Manifest
 * @see android.app.ProgressDialog
 * @see android.content.Context
 * @see android.content.DialogInterface
 * @see android.content.Intent
 * @see android.content.SharedPreferences
 * @see android.content.pm.PackageManager
 * @see android.os.Bundle
 * @see android.os.Handler
 * @see android.os.Message
 * @see android.support.v4.app.ActivityCompat
 * @see android.support.v4.app.Fragment
 * @see android.support.v4.content.ContextCompat
 * @see android.support.v4.view.ViewPager
 * @see android.view.LayoutInflater
 * @see android.view.View
 * @see android.view.ViewGroup
 * @see android.widget.Button
 * @see android.widget.Toast
 * @see java.util.ArrayList
 * @see java.util.Random
 * @see java.util.concurrent.CountDownLatch
 * @see io.skyway.Peer.OnCallback
 * @see io.skyway.Peer.Peer
 * @see io.skyway.Peer.PeerOption
 * @since 1.0
 */
public class CallFragment extends Fragment implements View.OnClickListener {

    /**
     * 自分のPeer
     */
    private Peer _peer;
    PeerOption options;
    /**
     * 自分のpeerid
     */
    private String myId;
    /**
     * MyTaskに受け渡すSQL文を格納する変数
     */
    private String sql;
    /**
     * ランダム行数取得用の変数
     */
    private Random random = new Random();
    /**
     * 自分のニックネームを格納するための変数
     */
    private String name = null;
    /**
     * 相手のニックネームを格納するための変数
     */
    private String yourName = null;
    /**
     * 自分の性別を格納するための変数
     */
    private int sex = 0;
    /**
     * 相手のpeeridを格納するための変数
     */
    private String yourId;
    /**
     * Preferenceを取得するための変数
     */
    private SharedPreferences sp;
    /**
     * MyTaskからlistを取得するための格納用
     */
    private static ArrayList<String> callbacklist = new ArrayList<>();
    /**
     * ダイアログ表示用
     */
    private ProgressDialog dialog;
    /**
     * スレッド用変数
     */
    private Thread thread;
    /**
     * MyTaskとの同期処理をとるための変数
     */
    static CountDownLatch cdl;
    /**
     * MyTaskクラスのインスタンス生成
     */
    MyTask task;
    /**
     * 通話をかけた側かかけられた側か(かけた側:true、かけられた側:false)
     * ※この画面上では、（DBを見つけた側:true、見つけられた側:false)
     * <p>
     * <br><table border=1><br>
     * <tr><th></th><th>true</th><th>false</th></tr>
     * <tr><td>通話において</td><td>かけた側</td><td>かけられた側</td></tr>
     * <tr><td>DBにおいて(FindID)</td><td>IDを書き込んだ側</td><td>IDを書き込まれた側</td><tr></table>
     */
    Boolean flag;
    /**
     * フラグメントにおいて戻り値に返すときに代入するやつ
     */
    View view;
    /**
     * 相手発見フラグ.
     * <br>相手を発見したら,trueになりwhileを抜ける
     */
    boolean search_flag = true;
    /**
     * データを保持するためのクラス（オブジェクト）.
     */
    CallData callData = CallData.getInstance();

    /**
     * このフラグメントが読み込まれた際に動くメソッド.
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     * @author Naoki_Tsuda
     * @since 1.1
     */
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        Context context = getActivity().getApplicationContext();
        //////////////////  START: Initialize SkyWay Peer ////////////////////

        // Please check this page. >> https://skyway.io/ds/
        options = new PeerOption();

        //Enter your API Key.
        options.key = "1ff9d941-8820-453b-a35f-eef5b5d75321";
        //Enter your registered Domain.
        options.domain = "aaaaabbbbbccccczzzzz.co.jp";

        _peer = new Peer(context, options);
        callData.setPeer(_peer);

        setPeerCallback(_peer);

        //request permissions
        //カメラとマイクの許可を得る処理
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, 0);
        } else {
        }
        return inflater.inflate(R.layout.fragment_call, container, false);
    }

    /**
     * カメラとマイクの許可をもらえているかの処理.
     * もらえていない場合にはToastを表示する.
     *
     * @param
     * @author Naoki_Tsuda
     * @since 1.1
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 0: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    Toast.makeText(getContext(), MyApplication.getContext().getString(R.string.denial), Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    /**
     * _peerを引数としてもらい、myIdを取得するとともにデータベースに書き込む.
     *
     * @param peer 自分のpeer型
     * @author Naoki_Tsuda
     * @since 1.0
     */
    private void setPeerCallback(Peer peer) {
        // !!!: Event/Open
        peer.on(Peer.PeerEventEnum.OPEN, new OnCallback() {
            @Override
            public void onCallback(Object object) {
                if (object instanceof String) {
                    myId = (String) object;
                    callData.setMyId(myId);
                    sp = getContext().getSharedPreferences("pref", Context.MODE_PRIVATE);
                    // Preferenceから自分が登録している名前を取得する
                    name = sp.getString("name", null);
                    // Preferenceから自分が登録している性別を取得する
                    sex = sp.getInt("sex", 0);
                    //DBに自分のID、名前、性別を入れる
                    sql = "insert into session(peerid,name,sex) values('" + myId + "','" + name + "'," + sex + ")";
                    callMyTask(sql, "1");
                }
            }
        });
    }

    /**
     * Activityが画面に表示されるときに動く.
     * <br>ボタン生成を行いXMLとの連結を行いクリックリスナーを追加する.
     *
     * @author Naoki_Tsuda
     * @since 1.1
     */
    @Override
    public void onStart() {
        Button CtoM = (Button) getView().findViewById(R.id.ctom);
        CtoM.setEnabled(true);
        Button CtoW = (Button) getView().findViewById(R.id.ctow);
        CtoW.setEnabled(true);
        CtoM.setOnClickListener(this);
        CtoW.setOnClickListener(this);
        super.onStart();
    }

    /**
     * ボタンが押されたときに動く.
     * <br>CALL TO MANボタン,CALL TO WOMANボタンを押した際に動く.
     *
     * @author Naoki_Tsuda
     * @since 1.1
     */
    public void onClick(View v) {
        yourId = null;
        yourName = null;
        sp = getContext().getSharedPreferences("pref", Context.MODE_PRIVATE);
        // Preferenceから自分が登録している名前を取得する
        name = sp.getString("name", null);
        // Preferenceから自分が登録している性別を取得する
        sex = sp.getInt("sex", 0);
        //  もし名前か性別を登録していなかった場合はToastを表示し以降の処理を行わない
        if (name == null || name.length() == 0 || sex == 0) {
            Toast.makeText(getContext(), MyApplication.getContext().getString(R.string.notRegis), Toast.LENGTH_SHORT).show();
            //画面をホームに戻す処理
            ViewPager viewPager = (ViewPager) getActivity().findViewById(R.id.pager);
            viewPager.setCurrentItem(1);
            return;
        }

        int callSex = 0;
        switch (v.getId()) {
            // CALL TO MANボタンが押された時
            // DBのsearch_flagを1(男性検索)に更新する
            case R.id.ctom:
                sql = "update session set search_flag = 1 where peerid = '" + myId + "'";
                callSex = 1;
                break;

            // CALL TO WOMANボタンが押された時
            // DBのsearch_flagを2(女性検索)に更新する
            default:
                sql = "update session set search_flag = 2 where peerid = '" + myId + "'";
                callSex = 2;
                break;
        }
        callMyTask(sql, "2");
        dialogDisplay(callSex);
    }

    /**
     * プログレスダイアログを表示.
     *
     * @param selectSex 性別(1:男性 2:女性)
     * @author Naoki_Tsuda
     * @since 1.1
     */
    void dialogDisplay(int selectSex) {
        dialog = new ProgressDialog(getContext());
        dialog.setCanceledOnTouchOutside(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        if (selectSex == 1) {
            dialog.setTitle("Call to Man");
        } else {
            dialog.setTitle("Call to Woman");
        }
        dialog.setMessage(MyApplication.getContext().getString(R.string.search));
        dialog.setCancelable(true);
        // キャンセルボタン設定
        // 押された場合、検索を中断しダイアログを消す
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
                new android.content.DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        sql = "update session set search_flag = 0 where peerid = '" + myId + "'";
                        callMyTask(sql, "2");
                        search_flag = false;
                        dialog.dismiss();
                    }
                });
        Button b = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                b.setEnabled(false);
            }
        }, 3000L);
        b.setEnabled(true);
        dialog.show();

        // search_flagがtrueの間検索を続けるスレッド
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                search_flag = true;
                try {
                    Thread.sleep((random.nextInt(5) * 1000) + 1000);
                } catch (Exception e) {
                }
                while (search_flag) {
                    selectingPeer(selectSex);
                }
                handler.sendEmptyMessage(0);
            }
        });
        thread.start();
    }

    /**
     * ダイアログを閉じてchangeActivity()に移る.
     *
     * @author Yuki_Kikuya
     * @author Naoki_Tsuda
     * @see CallFragment#changeActivity()
     * @since 1.0
     */
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            dialog.dismiss();
            changeActivity();
        }
    };

    /**
     * ランダムに通話相手を検索.
     *
     * @author Naoki_Tsuda
     * @since 1.1
     */
    void selectingPeer(int selectSex) {
        // 自分のpeerIdが登録されたか検索
        sql = "select findid from session where peerid = '" + myId + "'";
        callMyTask(sql, "3", "findid");
        if (callbacklist.get(0) == null) {
            // 自分のIDが登録されていなかった場合
            // 検索性別の相手の人数をカウントする
            if (selectSex == 1) {
                sql = "select count(*) from session where sex = " + selectSex + " and search_flag = " + sex + " and findid is null and peerid <> '" + myId + "'";
            } else {
                sql = "select count(*) from session where sex = " + selectSex + " and search_flag = " + sex + " and findid is null and peerid <> '" + myId + "'";
            }
            callMyTask(sql, "4", "count(*)");
            int count = Integer.parseInt(callbacklist.get(0));

            // もし件数が0ならreturn、0でなければ検索件数の中からランダムで相手選択
            if (count == 0) {
                return;
            } else {
                int ran = random.nextInt(count);
                // ランダムに検索性別の相手を選択
                sql = "select * from session  where sex = " + selectSex + " and search_flag = " + sex + " and findid is null and peerid <> '" + myId + "' limit 1 offset " + ran + "";
                callMyTask(sql, "6");
                yourId = callbacklist.get(0);
                yourName = callbacklist.get(1);
                // 自分のfindIDに相手のpeerIDを登録
                sql = "update session set findid = '" + yourId + "'  where peerid = '" + myId + "'";
                callMyTask(sql, "2");
                // 相手のfindIDに自分のpeerIDを登録
                sql = "update session set findid = '" + myId + "'  where peerid = '" + yourId + "'";
                callMyTask(sql, "2");
                flag = false;
                search_flag = false;
                return;
            }
        } else {
            // 自分のIDが登録されていた場合
            yourId = callbacklist.get(0);
            sql = "select name from session where peerid = '" + yourId + "'";
            callMyTask(sql, "3", "name");
            yourName = callbacklist.get(0);
            flag = true;
            search_flag = false;
            return;
        }
    }

    /**
     * MyTaskを呼び出すメソッド.
     *
     * @param getSql 　DB検索SQL文
     * @param num    呼出し後switch用
     * @author Naoki_Tsuda
     * @see MyTask
     * @since 1.1
     */
    void callMyTask(String getSql, String num) {
        cdl = new CountDownLatch(1);
        sql = getSql;
        task = new MyTask(CallFragment.this);
        task.execute(sql, num);
        try {
            cdl.await();
        } catch (InterruptedException e) {
        }
    }

    /**
     * MyTaskを呼び出すメソッド（オーバーロード）.
     *
     * @param getSql 　DB検索SQL文
     * @param num    呼出し後switch用
     * @param s      　臨機応変にSQL文変更用
     * @author Naoki_Tsuda
     * @see MyTask
     * @since 1.1
     */
    void callMyTask(String getSql, String num, String s) {
        cdl = new CountDownLatch(1);
        sql = getSql;
        task = new MyTask(CallFragment.this);
        task.execute(sql, num, s);
        try {
            cdl.await();
        } catch (InterruptedException e) {
        }
    }

    /**
     * 通話画面に遷移する.
     * <br>CallDataにデータを登録する.
     *
     * @author Naoki_Tsuda
     * @see MediaActivity
     * @see CallData
     * @since 1.1
     */
    void changeActivity() {
        if (yourName != null && myId != null && yourId != null) {
            Intent intent = new Intent(getActivity().getApplication(), MediaActivity.class);
            callData.setMyId(myId);
            callData.setYourId(yourId);
            callData.setYourName(yourName);

            intent.putExtra("flag", flag);
            yourId = null;
            yourName = null;
            startActivity(intent);
        } else {
            Toast.makeText(getContext(), MyApplication.getContext().getString(R.string.notFound), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * ArrayList Listのセッター.
     * DBから取得したでデータをセットする.
     *
     * @param list DBから取得したでデータ
     * @author Naoki_tsuda
     * @since 1.0
     */
    public static void changeList(ArrayList<String> list) {
        callbacklist = list;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle saveInstanceState) {
        super.onActivityCreated(saveInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        System.gc();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

}