// ビデオチャットアプリ
//
// @author  Yuki_Kikuya
// update on 2016-11-24
package jjj.drinkingparty;

import io.skyway.Peer.Browser.Canvas;
import io.skyway.Peer.Browser.MediaConstraints;
import io.skyway.Peer.Browser.MediaStream;
import io.skyway.Peer.Browser.Navigator;
import io.skyway.Peer.CallOption;
import io.skyway.Peer.MediaConnection;
import io.skyway.Peer.OnCallback;
import io.skyway.Peer.Peer;
import io.skyway.Peer.PeerError;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioManager;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;

import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Chronometer;


import static jjj.drinkingparty.MyApplication.getContext;

/**
 * 通話画面のクラス.
 *
 * @author Yuki_Kikuya
 * @see <a href = "https://nttcom.github.io/skyway/docs/#Android-mediaconnection">SkyWayドキュメント</a>
 * @see <a href = "https://github.com/nttcom/SkyWay-Android-SDK/releases/tag/v0.2.2">SkyWay Sample コード</a>
 * @since 1.0
 */
public class MediaActivity
        extends Activity implements OnClickListener {

    /**
     * ログを出力する際の基本となるクラスの単純名
     */
    private static final String TAG = MediaActivity.class.getSimpleName();

    /**
     * データ保持クラスのインスタンス（呼び出し　シングルトン）
     */
    CallData callData = CallData.getInstance();

    /**
     * 自分の設定情報を保持するPeer型
     */
    private Peer _peer;
    /**
     * メディアストリームをラップしている型
     */
    private MediaConnection _media;

    /**
     * 自分(MediaStream型のオブジェクト)
     */
    private MediaStream _msLocal;

    /**
     * 相手(MediaStream型のオブジェクト)
     */
    private MediaStream _msRemote;

    /**
     * 並行処理する際に使用する
     */
    private Handler _handler;

    /**
     * 自分のPeerId文字列
     */
    private String myId;

    /**
     * trueのときは接続中と判断、falseのときは繋がっていないと判断
     */
    private boolean _bCalling;
    MediaConstraints constraints;

    /**
     * 自分側のskyWayの映像を表示する
     */
    Canvas cLocal;

    /**
     * 相手側のskyWayの映像を表示する
     */
    Canvas cRemote;

    /**
     * videoのon/off
     */
    ImageButton videoButton;

    /**
     * 通信切断ボタン
     */
    ImageButton btnAction;

    /**
     * カメラ切り替えボタン
     */
    ImageButton switchCameraAction;

    /**
     * マイク切り替えボタン
     */
    ImageButton microphoneButton;

    /**
     * タイマーのUIに作成
     */
    Chronometer mChronometer;

    /**
     * nicknameを表示するためのTextViewのインスタンス
     */
    TextView tvOwnId;

    /**
     * 送られてきたIntentを格納するための箱
     */
    Intent intent;

    /**
     * 相手のニックネーム
     */
    String nickname;

    /**
     * 相手のPeerID
     */
    String peerId;

    /**
     * ビデオ映像のオンオフ判定
     */
    Boolean bVideo = true;

    /**
     * 音声（マイク）のオンオフの判定
     */
    Boolean bAudio = true;

    /**
     * microphoneのオンオフの判定
     */
    Boolean isMute = true;

    /**
     * 電話をかけた側か受けた側かを判定するフラグ
     */
    Boolean callFlag;

    /**
     * 全画面表示の映像を切り替えを制御するフラグ
     */
    Boolean changeCanvas = false;

    /**
     * ニックネームを表示する為のレイアウト
     */
    LinearLayout layoutNick;

    /**
     * 相手の映像表示するcanvasの一階層上にあるレイアウト
     */
    LinearLayout llPrimary;

    /**
     * ホームボタン押されたときのためのもの
     */
    MediaActivity.HomeButtonReceive homeButtonReceive;

    static AudioManager audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);

    /**
     * @author Naoki_Tsuda
     * @see <a href = "http://jp.androids.help/q14526">発信コールが行われるたびにスピーカーフォンをオンにする</a>
     * @see <a href = "http://blog.ch3cooh.jp/entry/20151001/1443661200">Androidでヘッドセット(イヤホン)の接続状態の変化を検出する</a>
     * @since 1.1
     */
    private static BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action == null) {
                return;
            }
            switch (action) {
                case Intent.ACTION_HEADSET_PLUG:
                    int state = intent.getIntExtra("state", -1);
                    if (state == 0) {
                        // ヘッドセットが装着されていない・外された
                        audioManager.setSpeakerphoneOn(true);
                    } else if (state > 0) {
                        // イヤホン・ヘッドセット(マイク付き)が装着された
                        audioManager.setSpeakerphoneOn(false);
                    }
                    break;
                case AudioManager.ACTION_AUDIO_BECOMING_NOISY:
                    // 音声経路の変更！大きな音が鳴りますよ！！
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 画面作成時の初期設定などを行う.
     * <br><ul><li>xmlファイルの指定</li>
     * <li>画面作成時の初期設定</li>
     * <li>スレッド処理のHandler取得</li>
     * <li>データ保持クラス{@linkplain CallData}からのデータ取得クラス{@linkplain MediaActivity#getData()}の呼び出し</li>
     * <li>Viewの初期化クラス{@linkplain MediaActivity#initializeViews()}の呼び出し</li></ul>
     * <br>.
     *
     * @param savedInstanceState 画面の受け渡しに必要なデータ
     * @author Yuki_Kikuya
     * @see <a href = "http://qiita.com/m2mtu/items/ac0d61e884519a4c61bb">onCreateとは</a>
     * @see <a href = "http://d.hatena.ne.jp/nkawamura/20130716/1373980954">LayoutInflaterについてまとめ</a>
     * @see <a href = "http://andante.in/i/android%E3%82%A2%E3%83%97%E3%83%AAtips/%E3%83%95%E3%83%AB%E3%82%B9%E3%82%AF%E3%83%AA%E3%83%BC%E3%83%B3%E3%81%A7%E3%83%8A%E3%83%93%E3%82%B2%E3%83%BC%E3%82%B7%E3%83%A7%E3%83%B3%E3%83%90%E3%83%BC%E3%81%BE%E3%81%A7%E6%B6%88%E3%81%9B%E3%82%8B/">フルスクリーンでナビゲーションバーまで消せる</a>
     * @since 1.0
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        // タイトルを非表示にします
        getWindow().addFlags(Window.FEATURE_NO_TITLE);
        // xmlファイルを指定
        setContentView(R.layout.activity_video_chat);

        //メインスレッドの Looper にメッセージを送るHandlerインスタンスを生成する場合
        _handler = new Handler(Looper.getMainLooper());

        //intentで受け取ったデータをフィールドに格納
        getData();
        // このクラス内のメソッドsetPeerCallback
        if (_peer == null) {
        } else {
            setPeerCallback(_peer);
        }
        // 画面遷移時に日か渡させるデータのgetter

        _bCalling = false;

        initializeViews();
        startLocalStream();

        _bCalling = false;

        if (callFlag) {
            calling(peerId);
        }
        homeButtonReceive = new MediaActivity.HomeButtonReceive();
        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        this.registerReceiver(homeButtonReceive, iFilter);

        audioManager.setSpeakerphoneOn(true);

        registerReceiver(broadcastReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
        registerReceiver(broadcastReceiver, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
    }

    /**
     * Viewの初期化を行う.
     * <br><ul><li>カウントダウンタイマーの作成</li>
     * <li>HangUpボタンの作成</li>
     * <li>カメラ切り替えボタンの作成</li>
     * <li>マイクのオンオフボタンの作成、</li>
     * <li>ビデオのオンオフボタンの作成、</li>
     * <li>nicknameの表示TextViewの作成</li></ul>.
     *
     * @author Yuki_Kikuya
     * @see <a href = "http://techbooster.jpn.org/andriod/application/3230/">タイマーの参考ページ</a>
     * @since 1.0
     */
    private void initializeViews() {

        //カウントダウンのタイマーを作成
        mChronometer = (Chronometer) findViewById(R.id.chronometer);
        mChronometer.setTextColor(Color.BLACK);
        //hang up ボタンの作成
        btnAction = (ImageButton) findViewById(R.id.btnHangUp);
        btnAction.setOnClickListener(this);

        //カメラ切り替えボタン
        switchCameraAction = (ImageButton) findViewById(R.id.switchCameraAction);
        switchCameraAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != _msLocal) {
                    Boolean result = _msLocal.switchCamera();

                    if (true == result) {
                        //Success
                    } else {
                        //Failed
                    }
                }
            }
        });
        // ビデオの初期化とオンオフの切り替え
        videoButton = (ImageButton) findViewById(R.id.videoAction);
        videoButton.setOnClickListener(this);

        layoutNick = (LinearLayout) findViewById(R.id.nickLayout);

        llPrimary = (LinearLayout) findViewById(R.id.llPrimary);
        llPrimary.setOnClickListener(this);

        LinearLayout llSecondary = (LinearLayout) findViewById(R.id.llSecondary);
        llSecondary.setOnClickListener(this);

        // マイクの初期化とオンオフの切り替え
        microphoneButton = (ImageButton) findViewById(R.id.microphone_change);
        microphoneButton.setOnClickListener(this);

        tvOwnId = (TextView) findViewById(R.id.nickname);
        //myIdは相手のPeerId
        if (null == myId) {
            tvOwnId.setText("not found id");
        } else {
            tvOwnId.setText(nickname);
        }
    }


    /**
     * permissionの確認メソッド.
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     * @author skyWaySample
     * @see <a href = "http://techbooster.org/android/application/17223/">初心者のためのM PERMISSIONS入門</a>
     * @since 1.0
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 0: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLocalStream();
                } else {
                    Toast.makeText(this, "Failed to access the camera and microphone.\nclick allow when asked for permission.", Toast.LENGTH_LONG).show();
                }
                break;

            }
        }
    }

    /**
     * 自分の映像を画面にセットする.
     * {@linkplain MediaActivity#_peer}の初期化、
     * <br>自分のの映像を画面に表示する.
     *
     * @author Yuki_Kikuya
     * @see <a href = ""></a>
     * @since 1.0
     */
    private void startLocalStream() {

        Navigator.initialize(_peer);
        constraints = new MediaConstraints();
        _msLocal = Navigator.getUserMedia(constraints);

        // 自分のカメラの映像を確保
        cLocal = (Canvas) findViewById(R.id.svSecondary);
        cLocal.addSrc(_msLocal, 0);

    }


    /**
     * 通話を相手にかけるクラス.
     * <br>Media connecting to remote peer
     * <br>selectingPeer()のなかの
     * <br>※ calling(item)	でしかcallingは呼ばれない！！.
     *
     * @param strPeerId 相手のPeerId(Remote peer)
     * @author Yuki_Kikuya
     * @since 1.0
     */
    void calling(String strPeerId) {

        /** {@linkplain MediaActivity#_peer}がnullなら{@linkplain MediaActivity#closing()}を処理しない */
        if (null == _peer) {
            return;
        }

        // 相手を呼び出す時、または、呼び出しに応答する時には、MediaStreamを提供する必要があります。
        if (null != _media) {
            _media.close();
            _media = null;
        }

        /** 設定情報 */
        CallOption option = new CallOption();
        _media = _peer.call(strPeerId, _msLocal, option);
        if (null != _media) {

            setMediaCallback(_media);
            _bCalling = true;
        }

        /**
         * ボタンを消すために、OnClickListenerにcRemoteを登録
         * @since 1.1
         */
        cRemote = (Canvas) findViewById(R.id.svPrimary);
        cLocal.addSrc(_msLocal, 0);

    }


    /**
     * Callなどのイベントをセットするクラス.
     * <p>
     * <br><br>peerオブジェクト（クラス）のonメソッドを内部に保持
     * <br>(実質はpeerクラスがpeerImplクラスを継承してるのでPeerImplクラスのonメソッド)
     * <br>media.on(event, new OnCallback(){puclic void onCallback(Object object){}});←こんな型してる
     * <br>第一引数eventはenumクラスの列挙型である
     * <br>第二引数はメソッドごと送っている.
     *
     * @param peer 自分のpeer型
     * @author Yuki_Kikuya
     * @since 1.0
     */
    private void setPeerCallback(Peer peer) {

        // !!!: Event/Call
        peer.on(Peer.PeerEventEnum.CALL, new OnCallback() {

            /**
             * Peer.onの引数内にあるデータを送るメソッド.
             * <br>CALLするときに使用する
             * <br>電話はかかってきたときに動くやつ.
             *
             * @author Yuki_Kikuya
             * @since 1.0
             * @param object
             */
            @Override
            public void onCallback(Object object) {

                // A instanceof B (AがBと同じクラスのオブジェクトのインスタンスである場合)
                if (!(object instanceof MediaConnection)) {

                    return;
                }

                _media = (MediaConnection) object;
                _media.answer(_msLocal);

                setMediaCallback(_media);

                _bCalling = true;
            }
        });

        // !!!: Event/Error
        // コールバックを登録(ERROR)
        peer.on(Peer.PeerEventEnum.ERROR, new OnCallback() {

            /**
             * Peer.onの引数内にあるデータを送るメソッド.
             * <br>コールバックを登録(ERROR)
             * <br>エラーが起こった時に動く.
             *
             * @author Yuki_Kikuya
             * @since 1.0
             * @param object
             */
            @Override
            public void onCallback(Object object) {
                errorOutput(object);
            }
        });
    }

    /**
     * MediaConnectionをセットする.
     * <br>STREAMのonメソッドが実行されれば、相手の映像を画面にセットする。
     * <br>CLOSEのonメソッドが実行されれば、接続を終了した時に動く処理を行う。
     * <br>ERRORのonメソッドが実行されれば、error処理が動く.
     *
     * @param media
     * @author Yuki_Kikuya
     * @since 1.0
     */
    void setMediaCallback(MediaConnection media) {
        media.on(MediaConnection.MediaEventEnum.STREAM, new OnCallback() {

            @Override
            public void onCallback(Object object) {

                _msRemote = (MediaStream) object;
                //キャンバスに相手の映像を追加
                cRemote = (Canvas) findViewById(R.id.svPrimary);
                cRemote.addSrc(_msRemote, 0);

                timerStart();
            }
        });

        // !!!: MediaEvent/Close
        media.on(MediaConnection.MediaEventEnum.CLOSE, new OnCallback() {

            @Override
            public void onCallback(Object object) {
                String sql = "update session set search_flag = 0, findid = null  where peerid = '" + myId + "'";
                MyTask task = new MyTask();
                task.execute(sql, "2");
                cRemote = (Canvas) findViewById(R.id.svPrimary);
                cRemote.removeSrc(_msRemote, 0);
                _msRemote = null;
                if (_media != null) {
                    if (_media.isOpen) {
                        _media.close();
                    }
                }
                destroyPeer();

            }

        });


        // !!!: MediaEvent/Error
        media.on(MediaConnection.MediaEventEnum.ERROR, new OnCallback() {

            @Override
            public void onCallback(Object object) {
                errorOutput(object);
            }
        });
        //  END: Set SkyWay peer Media connection callback
    }

    /**
     * media#onのSTREAM,CLOSE,ERRORの{@linkplain  io.skyway.Peer.OnCallback}のcallbackを、
     * <br>nullに設定する.
     *
     * @param media
     * @author Yuki_Kikuya
     * @since 1.0
     */
    // Unset media connection event callback.
    void unsetMediaCallback(MediaConnection media) {

        media.on(MediaConnection.MediaEventEnum.STREAM, null);
        media.on(MediaConnection.MediaEventEnum.CLOSE, null);
        media.on(MediaConnection.MediaEventEnum.ERROR, null);
    }

    /**
     * connectionを閉じるメソッド.
     * <br>{@linkplain MediaActivity#_media}がnullだったら、
     * <br>{@linkplain MediaConnection#close()}を実行する。
     * Closing connection.
     *
     * @author Yuki_Kikuya
     * @since 1.0
     */
    void closing() {
        if (false == _bCalling) {
            return;
        }

        // bCalling=false つなっがってないとき
        _bCalling = false;
        if (null != _media) {
            _media.close();
        }
    }


    /**
     * 時計が進むメソッド.
     *
     * @author Yuki_Kikuya
     * @see <a href = "http://techbooster.jpn.org/andriod/application/3230/">タイマー参考ページ</a>
     * @since 1.0
     */
    public void timerStart() {
        if (_handler != null) {
            _handler.post(new Runnable() {
                @Override
                public void run() {
                    mChronometer.setBase(SystemClock.elapsedRealtime());
                    mChronometer.start();
                }
            });
        } else {
        }
    }

    /**
     * 時計が止まるメソッド.
     *
     * @author Yuki_Kikuya
     * @see <a href = "http://techbooster.jpn.org/andriod/application/3230/">タイマー参考ページ</a>
     * @since 1.0
     */
    public void timerStop() {
        _handler.post(new Runnable() {
            @Override
            public void run() {

                mChronometer.stop();
                mChronometer.setBase(SystemClock.elapsedRealtime());

            }
        });
    }

    /**
     * 画面上のViewをクリックした際の処理の記述.
     * <br>audioのオンオフ処理、
     * <br>videoのオンオフ処理、
     * <br>HangUp(切断ボタン)の処理、
     * <br>自分の映像、相手の映像の切り替え処理の記述がある.
     *
     * @param v Viewクラスの型を取得
     * @author Yuki_Kikuya
     * @see <a href = "http://techbooster.org/android/application/299/">onClickの使い方</a>
     * @see <a href = "http://qiita.com/KawakawaRitsuki/items/eae62a3f75cb5dc32aa4">Androidの"OnClick"はどれを使えばいいの？</a>
     * @since 1.0
     */
    public void onClick(View v) {
        // audioのオン・オフを切り替える
        switch (v.getId()) {
            case R.id.microphone_change:
                onClickMicChange();
                break;
            // videoのオン・オフを切り替える
            case R.id.videoAction:
                onClickvideoAction();
                break;
            // Hung Up した時の処理
            // 通話をストップして、画面を遷移させる
            case R.id.btnHangUp:
                onClickBtnHangUp();
                break;
            //自分の映像（小さい窓）をクリックすると相手の映像（大きい方）と映像が入れ替わる処理
            case R.id.llSecondary:
                onClickLlSecondary();
                break;
            // 画面全体押したとき、ボタンとニックネーム表示部分を削除する
            case R.id.llPrimary:
                onClickLlPrimary();
                break;

            default:

        }

    }

    /**
     * micActionボタンを押したときの処理.
     * <br>マイクのオンオフを切り替える処理.
     *
     * @author Yuki_Kikuya
     * @since 1.1
     */
    void onClickMicChange() {
        if (isMute == true) {

            _msLocal.setEnableAudioTrack(0, false);
            microphoneButton.setActivated(true);
            isMute = false;

        } else {

            _msLocal.setEnableAudioTrack(0, true);
            microphoneButton.setActivated(false);
            isMute = true;

        }

    }

    /**
     * videoActionボタンを押したときの処理.
     * <br>自分の映像のオンオフを切り替える処理.
     *
     * @author Yuki_Kikuya
     * @since 1.1
     */
    void onClickvideoAction() {
        if (bVideo == true) {

            _msLocal.setEnableVideoTrack(0, false);
            videoButton.setActivated(true);
            bVideo = false;

        } else {

            _msLocal.setEnableVideoTrack(0, true);
            videoButton.setActivated(false);
            bVideo = true;

        }
        constraints.videoFlag = false;

    }

    /**
     * HungUpボタンを押したときの処理.
     * <br>接続終了ボタンを押したとき接続を切る処理.
     *
     * @author Yuki_Kikuya
     * @since 1.1
     */
    void onClickBtnHangUp() {
        // アプリが画面遷移した時にPeerを削除する必要がある
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(getContext().getString(R.string.hangUp));
        // アラートダイアログの肯定ボタンがクリックされた時に呼び出されるコールバックリスナーを登録します
        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                closing();
            }
        });
        alertDialogBuilder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alertDialogBuilder.show();
    }

    /**
     * 自分の映像を押したときの処理.
     * <br>自分の映像を押したとき、自分の画面と相手の画面を切り替える処理.
     *
     * @author Yuki_Kikuya
     * @since 1.1
     */
    void onClickLlSecondary() {
        if (!changeCanvas) {

            cLocal.removeSrc(_msLocal, 0);
            cRemote.removeSrc(_msRemote, 0);
            cLocal.addSrc(_msRemote, 0);
            cRemote.addSrc(_msLocal, 0);
            changeCanvas = true;

        } else {

            cLocal.removeSrc(_msRemote, 0);
            cRemote.removeSrc(_msLocal, 0);
            cLocal.addSrc(_msLocal, 0);
            cRemote.addSrc(_msRemote, 0);
            changeCanvas = false;

        }

    }

    /**
     * 相手の映像（全体）を押したときの処理.
     * <br>ボタン、自分の映像以外の領域を押したとき、ボタンを隠す処理.
     *
     * @author Yuki_Kikuya
     * @since 1.1
     */
    void onClickLlPrimary() {
        if (microphoneButton.getVisibility() == View.VISIBLE) {
            // 表示なら非表示する
            videoButton.setVisibility(View.INVISIBLE);
            btnAction.setVisibility(View.INVISIBLE);
            switchCameraAction.setVisibility(View.INVISIBLE);
            layoutNick.setVisibility(View.INVISIBLE);
            microphoneButton.setVisibility(View.INVISIBLE);

        } else if (microphoneButton.getVisibility() == View.INVISIBLE) {
            // 非表示されている時に表示に
            videoButton.setVisibility(View.VISIBLE);
            btnAction.setVisibility(View.VISIBLE);
            switchCameraAction.setVisibility(View.VISIBLE);
            layoutNick.setVisibility(View.VISIBLE);
            microphoneButton.setVisibility(View.VISIBLE);

        }
    }


    /**
     * データ保持クラス（オブジェクト）、intentからのデータ取得.
     * <br>画面遷移時に日か渡させるデータのgetter
     *
     * @author Yuki_Kikuya
     * @since 1.0
     */
    public void getData() {
        intent = getIntent();
        callFlag = intent.getBooleanExtra("flag", false);
        nickname = callData.getYourName();

        // 相手のId
        peerId = callData.getYourId();

        // 自分のId
        // myId = MainActivity.MenuFragment.myId;
        myId = callData.getMyId();
        _peer = callData.getPeer();
    }

    /**
     * 画面遷移を行うメソッド.
     * {@linkplain MainActivity}に飛ばす.
     *
     * @author Yuki_Kikuya
     * @since 1.0
     */
    public void goActivity() {
        callData.setBackFlag(true);
        this.finish();
    }

    /**
     * バックボタンが押されたときの処理.
     * <br>通話終了確認アラートダイアログを表示する.
     *
     * @param keyCode 何のボタンか判別するもの
     * @param event
     * @return バックキーが押されたらtrue
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setMessage(getContext().getString(R.string.hangUp));
            // アラートダイアログの肯定ボタンがクリックされた時に呼び出されるコールバックリスナーを登録します
            alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    closing();
                }
            });
            alertDialogBuilder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            alertDialogBuilder.show();
            return true;
        } else {
            return false;
        }
    }


    /**
     * 終了するときに、Peerを初期化するためのメソッド.
     * <br>相手の映像({@linkplain MediaActivity#_msRemote})をnullに、
     * <br>自分の映像({@linkplain MediaActivity#_msLocal})をnullにする。
     * <br>そして、{@linkplain MediaActivity#_media}をnullにする.
     *
     * @author Yuki_Kikuya
     * @since 1.0
     */
    private void destroyPeer() {
        if (null != _msRemote) {
            // 相手の映像を取得
            cRemote.removeSrc(_msRemote, 0);
            if (null != _msRemote) {
                _msRemote.close();
                _msRemote = null;
            }
        }
        if (null != _msLocal) {
            // 自分の映像を取得
            cLocal = (Canvas) findViewById(R.id.svSecondary);
            cLocal.removeSrc(_msLocal, 0);
            if (null != _msLocal) {
                _msLocal.close();
                _msLocal = null;
            }
        }
        if (null != _media) {
            if (_media.isOpen) {
                _media.close();
            }

            unsetMediaCallback(_media);
            _media = null;
        }

        // 映像を取得するためのクラスです。終了化を行います。
//		Navigator.terminate();
        _handler = null;
        goActivity();
    }

    /**
     * エラーのアラートダイアログを出力するメソッド.
     * <br> PeerErrorが起きた時のダイアログを画面に出す.
     *
     * @param object
     * @author Yuki_Kikuya
     * @since 1.0
     */
    private void errorOutput(Object object) {
        PeerError error = (PeerError) object;
        String strMessage = "" + getString(R.string.error);
        String strLabel = getString(android.R.string.ok);
        Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                        alertDialogBuilder.setMessage(strMessage);
                        // アラートダイアログの肯定ボタンがクリックされた時に呼び出されるコールバックリスナーを登録します
                        alertDialogBuilder.setPositiveButton(strLabel,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        closing();
                                    }
                                });

                        alertDialogBuilder.show();
                    }
                });
            }
        }).start();
    }

    public class HomeButtonReceive extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            closing();
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        // Disable Sleep and Screen Lock
        Window wnd = getWindow();
        wnd.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        wnd.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Set volume control stream type to WebRTC audio.
        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
    }

    @Override
    protected void onPause() {
        // Set default volume control stream type.
        setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
        super.onPause();

    }

    @Override
    protected void onStop() {
        _handler = null;
        // Enable Sleep and Screen Lock
        Window wnd = getWindow();
        wnd.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        wnd.clearFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        audioManager.setSpeakerphoneOn(false);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        // _listPeerIds = null;
        _handler = null;
        unregisterReceiver(homeButtonReceive);
        unregisterReceiver(broadcastReceiver);
//		 Activityクラス（superクラス）のonDstroyメソッド
        super.onDestroy();
    }
}