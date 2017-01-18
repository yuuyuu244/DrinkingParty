package jjj.drinkingparty;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/**
 * プロフィールの画面のクラス.
 *
 * @author 江城達也
 */
public class ProfFragment extends Fragment implements View.OnClickListener {
    /** ログを出力する際の基本となるクラスの単純名 */
    public static final String TAG = ProfFragment.class.getSimpleName();
    /** ニックネーム用 */
    String name = null;
    /** 性別用（０＝未設定、１＝男、２＝女） */
    int sex = 0;
    /** 自分のニックネーム登録 */
    EditText editText;
    /** 性別選択 */
    Spinner spinner;
    /** 決定(Enter)ボタン */
    Button button;
    /** プリファレンスの使用するときのクラス */
    SharedPreferences preferences;
    /** SQL文を保持する文字列 */
    String sql;
    /** SQLつなぐ際の非同期の処理を行うクラス */
    MyTask task;
    /** Viewを操作するクラス */
    View view;

    /**
     * プロフィール作成画面の初期設定.
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_prof, container, false);
        view.requestFocus();
        view.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_MOVE){
                    view.requestFocus();
                }
                return true;
            }
        });
        return view;
    }

    /**
     * キーボードのフォーカス処理.
     * <br>ENTERボタンの作成、
     * <br>性別選択スピナーの作成
     * @see <a href="http://stackoverflow.com/questions/3928071/setting-a-spinner-onclicklistener-in-android"></a>
     */
    @Override
    public void onStart() {
        super.onStart();

        editText = (EditText) getActivity().findViewById(R.id.editText);
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener(){
            @Override
            public void onFocusChange(View v, boolean hasFocus){
                // EditTextのフォーカスが外れた場合
                if(hasFocus == false){
                    // ソフトキーボードを非表示にする
                    InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
        });

        button = (Button) getActivity().findViewById(R.id.enter);
        button.setOnClickListener(this);

        spinner = (Spinner) getActivity().findViewById(R.id.sexSpinner);
        spinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    spinner.setFocusable(true);
                    spinner.setFocusableInTouchMode(true);
                    spinner.requestFocus();
                }
                return false;
            }
        });
    }

    /**
     * プロフィールを登録するボタンを押した際の処理.
     * <br>プリファレンスにプロフィール入力項目を書き込む.
     *
     * @param v
     */
    public void onClick(View v) {
        //プリファレンス書き込み準備
        button.setFocusable(true);
        button.setFocusableInTouchMode(true);
        button.requestFocus();

        preferences = getContext().getSharedPreferences("pref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        try {
            //プリファレンスにプロフィール入力項目を書き込み
            editor.putString("name", editText.getText().toString());
            editor.putInt("sex", spinner.getSelectedItemPosition());
            editor.commit();

        } catch (Exception e) {
        }
        //登録検知メッセージ
        if (editText.getText().toString() == null ||
                editText.getText().toString().length() == 0 || spinner.getSelectedItemPosition() == 0) {
            button.setEnabled(false);
            new Handler().postDelayed(new Runnable(){
                public void run(){
                    button.setEnabled(true);
                }
            }, 2000L);
            Toast.makeText(getContext(), MyApplication.getContext().getString(R.string.saveError), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), MyApplication.getContext().getString(R.string.save), Toast.LENGTH_SHORT).show();
            /*  自分の性別とニックネームをデータベースに書き込む　*/
            name = preferences.getString("name", null);
            sex = preferences.getInt("sex", 0);
            CallData callData = CallData.getInstance();
            sql = "update session set name = '" + name + "', sex = " + sex + " where peerid = '" + callData.getMyId() + "'";
            task = new MyTask(ProfFragment.this);
            task.execute(sql, "2");
            //画面をホームに戻す処理
            ViewPager viewPager = (ViewPager) getActivity().findViewById(R.id.pager);
            viewPager.setCurrentItem(1);
        }
        button.setFocusable(false);
        button.setFocusableInTouchMode(false);
    }

    /**
     * プリファレンスの設定情報を画面に登録する.
     * 
     */
    public void onResume(){
        super.onResume();

        ArrayAdapter<CharSequence> myadapter = ArrayAdapter.createFromResource(
                getContext(), R.array.sex_list, R.layout.myspinner);
        myadapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinner.setAdapter(myadapter);

        //プリファレンス読み込み準備
        preferences = getContext().getSharedPreferences("pref", Context.MODE_PRIVATE);
        //起動時、プロフィールの設定項目取得＆セット
        try {
            editText.setText( preferences.getString("name", name), TextView.BufferType.NORMAL);
            spinner.setSelection(preferences.getInt("sex", sex));
        } catch (NullPointerException e) {
        }
    }


}