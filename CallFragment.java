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
import android.util.Log;
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

import static android.content.ContentValues.TAG;

/**
 * CALL画面（通話開始画面）.
 * <br>相手のIDを発見するか、された場合に通話画面に遷移を行う.
 *
 * @author 津田
 */
public class CallFragment extends Fragment implements View.OnClickListener {

    /** 自分のPeer */
    private Peer            _peer;
    PeerOption options;
    /** 自分のpeerid */
    private String   myId;
    /** MyTaskに受け渡すSQL文を格納する変数 */
    private String sql;
    /** ランダム行数取得用の変数 */
    private Random random = new Random();
    /** 自分のニックネームを格納するための変数 */
    private String name = null;
    /** 相手のニックネームを格納するための変数 */
    private String yourName = null;
    /** 自分の性別を格納するための変数 */
    private int sex = 0;
    /** 相手のpeeridを格納するための変数 */
    private String yourId;
    /** Preferenceを取得するための変数 */
    private SharedPreferences sp;
    /** MyTaskからlistを取得するための格納用 */
    private static ArrayList<String> callbacklist = new ArrayList<>();
    /** ダイアログ表示用 */
    private ProgressDialog dialog;
    /** プログレスダイアログのキャンセルボタン */
    Button cancelButton;
    /** スレッド用変数 */
    private Thread thread;
    /** MyTaskとの同期処理をとるための変数 */
    static CountDownLatch cdl;
    /** MyTaskクラスのインスタンス生成 */
    MyTask task;
    /**
     * 通話をかけた側かかけられた側か(かけた側:true、かけられた側:false)
     * ※この画面上では、（DBを見つけた側:true、見つけられた側:false)
     *
     * <br><table border=1><br>
     * <tr><th></th><th>true</th><th>false</th></tr>
     * <tr><td>通話において</td><td>かけた側</td><td>かけられた側</td></tr>
     * <tr><td>DBにおいて(FindID)</td><td>IDを書き込んだ側</td><td>IDを書き込まれた側</td><tr></table>
     */
    Boolean flag;
    /** フラグメントにおいて戻り値に返すときに代入するやつ */
    View view;
    /**
     *  相手発見フラグ.
     *  <br>相手を発見したら,trueになりwhileを抜ける
     */
    boolean search_flag = true;
    /** データを保持するためのクラス（オブジェクト）. */
    CallData callData = CallData.getInstance();

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
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO},0);
        }else{
        }

        view = inflater.inflate(R.layout.fragment_call, container, false);
        return view;
    }

    /**
     * カメラとマイクの許可をもらえているかの処理.
     * もらえていない場合にはToastを表示する.
     *
     *
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 0: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                }else{
                    Toast.makeText(getContext(),MyApplication.getContext().getString(R.string.denial),Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    /**
     * _peerを引数としてもらい、myIdを取得するとともにデータベースに書き込む.
     *
     * @param peer 自分のpeer型
     */
    private void setPeerCallback(Peer peer)
    {
        // !!!: Event/Open
        peer.on(Peer.PeerEventEnum.OPEN, new OnCallback()
        {
            @Override
            public void onCallback(Object object)
            {
                Log.d(TAG, "[On/Open]");

                if (object instanceof String)
                {
                    myId = (String) object;
                    callData.setMyId(myId);
                    sp = getContext().getSharedPreferences("pref",Context.MODE_PRIVATE);
                    name = sp.getString("name", null);                                   //Preferenceから自分が登録している名前を取得する
                    sex = sp.getInt("sex", 0);                                            //Preferenceから自分が登録している性別を取得する

                    cdl = new CountDownLatch(1);
                    sql = "insert into session(peerid,name,sex) values('"+myId+"','"+name+"',"+sex+")";
                    task = new MyTask(CallFragment.this);
                    task.execute(sql,"1");
                    try {
                        cdl.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * Activityが画面に表示されるときに動く
     * <br>ボタン生成を行いXMLとの連結を行いクリックリスナーを追加する
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

    /**  ボタンが押されたときに動く  */
    public void onClick(View v){
        yourId = null;
        yourName = null;
        sp = getContext().getSharedPreferences("pref",Context.MODE_PRIVATE);
        name = sp.getString("name", null);                                   //Preferenceから自分が登録している名前を取得する
        sex = sp.getInt("sex", 0);                                            //Preferenceから自分が登録している性別を取得する
        /*  もし名前か性別を登録していなかった場合はToastを表示し以降の処理を行わない  */
        if(name == null || name.length() == 0 || sex == 0){
            Toast.makeText(getContext(),MyApplication.getContext().getString(R.string.notRegis),Toast.LENGTH_SHORT).show();
            //画面をホームに戻す処理
            ViewPager viewPager = (ViewPager) getActivity().findViewById(R.id.pager);
            viewPager.setCurrentItem(0);
            return;
        }

        switch(v.getId()){
            //CALL TO MANボタンが押された時
            case R.id.ctom:
                cdl = new CountDownLatch(1);
                sql = "update session set search_flag = 1 where peerid = '" + myId + "'";
                task = new MyTask(CallFragment.this);
                task.execute(sql,"2");
                try {
                    cdl.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                dialogDisplay(1);
                break;
            //CALL TO WOMANボタンが押された時
            default:
                cdl = new CountDownLatch(1);
                sql = "update session set search_flag = 2 where peerid = '" + myId + "'";
                task = new MyTask(CallFragment.this);
                task.execute(sql,"2");
                try {
                    cdl.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                dialogDisplay(2);
                break;
        }

    }

    /**
     * プログレスダイアログを表示.
     *
     * @param selectSex 性別(1:男性 2:女性)
     */
    void dialogDisplay(int selectSex){
        dialog = new ProgressDialog(getContext());
        dialog.setCanceledOnTouchOutside(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        if(selectSex == 1) {
            dialog.setTitle("Call to Man");
        }else{
            dialog.setTitle("Call to Woman");
        }
        dialog.setMessage(MyApplication.getContext().getString(R.string.search));
        dialog.setCancelable(false);
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.d(TAG,"dialogcancel");
                        search_flag = false;
                        sql = "update session set search_flag = 0 where peerid = '" + myId + "'";
                        task = new MyTask(CallFragment.this);
                        task.execute(sql,"2");
                        dialog.cancel();
                        thread.interrupt();
                    }
                });
        dialog.show();

        cancelButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Thread.sleep(3000);
                }catch (Exception e){
                }
                try{
                    selectingPeer(selectSex);
                }catch (Exception e){
                    Log.d(TAG,"thread"+e);
                }
                handler.sendEmptyMessage(0);

            }
        });
        thread.start();
    }

    /**
     * ダイアログを閉じてchangeActivity()に移る.
     *
     * @see CallFragment#changeActivity()
     */
    private Handler handler = new Handler(){
        public void handleMessage(Message msg) {
            dialog.dismiss();
            changeActivity();
        }
    };

    /**
     * ランダムに通話相手を検索.
     *
     */
    void selectingPeer(int selectSex) {
        while(search_flag){
            /* 自分のpeerIdが登録されたか検索 */
            cdl = new CountDownLatch(1);
            sql = "select findid from session where peerid = '" + myId + "'";
            task = new MyTask(CallFragment.this);
            task.execute(sql, "3", "findid");
            try {
                cdl.await();
            } catch (InterruptedException e) {
            }
            if (callbacklist.get(0) == null) {
                if(selectSex == 1) {
                    cdl = new CountDownLatch(1);
                    sql = "select count(*) from session where sex = " + selectSex + " and search_flag = "+sex+" and findid is null and peerid <> '" + myId + "'";
                    task = new MyTask(CallFragment.this);
                    task.execute(sql, "4", "count(*)");
                    try {
                        cdl.await();
                    } catch (InterruptedException e) {
                    }
                }else{
                    cdl = new CountDownLatch(1);
                    sql = "select count(*) from session where sex = " + selectSex + " and search_flag = "+sex+" and findid is null and peerid <> '" + myId + "'";
                    task = new MyTask(CallFragment.this);
                    task.execute(sql, "4", "count(*)");
                    try {
                        cdl.await();
                    } catch (InterruptedException e) {
                    }
                }

                int count = Integer.parseInt(callbacklist.get(0));

                if(count == 0){
                    continue;
                }else {
                    int ran = random.nextInt(count);
                    cdl = new CountDownLatch(1);
                    sql = "select * from session  where sex = " + selectSex + " and search_flag = "+sex+" and findid is null and peerid <> '" + myId + "' limit 1 offset " + ran + "";
                    task = new MyTask(CallFragment.this);
                    task.execute(sql, "6");
                    try {
                        cdl.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    yourId = callbacklist.get(0);
                    yourName = callbacklist.get(1);
                    cdl = new CountDownLatch(1);
                    sql = "update session set findid = '" + yourId + "'  where peerid = '" + myId + "'";
                    task = new MyTask(CallFragment.this);
                    task.execute(sql, "2");
                    try {
                        cdl.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    sql = "update session set findid = '" + myId + "'  where peerid = '" + yourId + "'";
                    task = new MyTask(CallFragment.this);
                    task.execute(sql, "2");
                    try {
                        cdl.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    flag = false;
                    break;
                }
            } else {
                yourId = callbacklist.get(0);
                cdl = new CountDownLatch(1);
                sql = "select name from session where peerid = '" + yourId + "'";
                task = new MyTask(CallFragment.this);
                task.execute(sql, "3", "name");
                try {
                    cdl.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                yourName = callbacklist.get(0);
                flag = true;
                break;
            }
        }
        search_flag = true;
    }

    /**
     * 通話画面に遷移する.
     * <br>CallDataにデータを登録する
     * @see MediaActivity
     */
    void changeActivity() {
        Log.d(TAG,""+yourName+myId+yourId);
        if (yourName != null && myId != null && yourId != null) {
            Intent intent = new Intent(getActivity().getApplication(), MediaActivity.class);
            callData.setMyId(myId);
            callData.setYourId(yourId);
            callData.setYourName(yourName);

            intent.putExtra("flag",flag);
            yourId = null;
            yourName = null;
            startActivity(intent);
        }else{
            Toast.makeText(getContext(),MyApplication.getContext().getString(R.string.notFound),Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * ArrayList Listのセッター.
     * DBから取得したでデータをセットする.
     *
     * @param list DBから取得したでデータ
     */
    public static void changeList(ArrayList<String> list){
        callbacklist = list;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle saveInstanceState){
        super.onActivityCreated(saveInstanceState);
    }

    @Override
    public void onResume(){
        super.onResume();
        System.gc();
    }

    @Override
    public void onPause(){
        super.onPause();
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
    }

    @Override
    public void onStop(){
        super.onStop();
    }

}