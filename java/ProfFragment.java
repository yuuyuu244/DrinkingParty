package jjj.drinkingparty;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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

import com.google.gson.Gson;

import org.json.JSONArray;

import java.util.ArrayList;

/**
 * プロフィールの画面のクラス.
 *
 * @author 江城達也
 * @version 1.1
 * @see android.app.AlertDialog
 * @see android.content.Context
 * @see android.content.DialogInterface
 * @see android.content.Intent
 * @see android.content.SharedPreferences
 * @see android.os.Bundle
 * @see
 * @since 1.0
 */
public class ProfFragment extends Fragment implements View.OnClickListener {
    /**
     * ログを出力する際の基本となるクラスの単純名
     */
    public static final String TAG = ProfFragment.class.getSimpleName();
    /**
     * ニックネーム用
     */
    String name = null;
    /**
     * 性別用（０＝未設定、１＝男、２＝女）
     */
    int sex = 0;
    /**
     * 自分のニックネーム登録
     */
    EditText editText;
    /**
     * 性別選択
     */
    Spinner spinner;
    /**
     * 決定(Enter)ボタン
     */
    Button button;
    /**
     * プリファレンスの使用するときのクラス
     */
    SharedPreferences preferences;
    /**
     * SQL文を保持する文字列
     */
    String sql;
    /**
     * SQLつなぐ際の非同期の処理を行うクラス
     */
    MyTask task;
    /**
     * Viewを操作するクラス
     */
    View view;
    /**
     * プリファレンス編集用エディター
     */
    SharedPreferences.Editor editor;
    /**
     * preference登録用
     */
    String json;
    /**
     * IPアドレス格納用配列
     */
    ArrayList<String> list = new ArrayList();
    /**
     * JSON変換用
     */
    Gson gson = new Gson();
    /**
     * スピナー用adapter
     */
    ArrayAdapter<String> adapter;
    /**
     * スピナー用ダイアログ
     */
    AlertDialog alertDialog;
    /**
     * IP履歴スピナー選択項目指定用
     */
    int selectedIndex = 0;
    /**
     * IP履歴ダイアログ
     */
    String title = null;
    /**
     * restartフラグ
     */
    boolean isRestart = false;

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
        preferences = getContext().getSharedPreferences("pref", Context.MODE_PRIVATE);
        editor = preferences.edit();
        adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_single_choice);
        view = inflater.inflate(R.layout.fragment_prof, container, false);
        view.requestFocus();
        view.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
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
     *
     * @see <a href="http://stackoverflow.com/questions/3928071/setting-a-spinner-onclicklistener-in-android"></a>
     */
    @Override
    public void onStart() {
        super.onStart();

        editText = (EditText) getActivity().findViewById(R.id.editText);
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // EditTextのフォーカスが外れた場合
                if (hasFocus == false) {
                    // ソフトキーボードを非表示にする
                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
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

        try {
            //プリファレンスにプロフィール入力項目を書き込み
            editor.putString("name", editText.getText().toString());
            editor.putInt("sex", spinner.getSelectedItemPosition());
            editor.commit();

        } catch (Exception e) {
        }
        if (editText.getText().toString().equals("tsudaisgodfather") && spinner.getSelectedItemPosition() == 0) {
            //IPアドレス入力用テキスト
            EditText editView = new EditText(getContext());
            new AlertDialog.Builder(getContext())
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setTitle(MyApplication.getContext().getString(R.string.address))
                    .setView(editView)
                    .setCancelable(true)
                    .setNeutralButton(MyApplication.getContext().getString(R.string.history), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_single_choice);
                            if (preferences.getString("list", null) != null) {
                                /** preferenceにIPアドレス履歴が存在する場合、そのデータをArrayListに格納する */
                                json = preferences.getString("list", null);
                                list = gson.fromJson(json, ArrayList.class);
                                for (int i = 0; i < list.size(); i++) {
                                    adapter.add(list.get(i));
                                }
                                title = MyApplication.getContext().getString(R.string.historyTitle);
                            } else {
                                title = (MyApplication.getContext().getString(R.string.noHistoryTitle));
                            }
                            // AlertDialogで選択肢を表示
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setTitle(title);
                            builder.setSingleChoiceItems(adapter, selectedIndex, onDialogClickListener);
                            builder.setNeutralButton(MyApplication.getContext().getString(R.string.allDelete), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    if (list.size() == 0) {
                                        Toast.makeText(getContext(), MyApplication.getContext().getString(R.string.noHistory), Toast.LENGTH_SHORT).show();
                                    } else {
                                        /** 履歴が存在する場合、preferenceに登録されている履歴を削除する */
                                        editor.remove("list");
                                        adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_single_choice);
                                        list = new ArrayList<String>();
                                        Toast.makeText(getContext(), MyApplication.getContext().getString(R.string.allDeleteOK), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            alertDialog = builder.create();
                            alertDialog.show();
                        }
                    })
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            /** 入力IPアドレスがすでにpreferenceに登録されているかを判定し、もしされていなければ追加される */
                            String IP = editView.getText().toString();
                            boolean isEqualIP = false;
                            if (preferences.getString("list", null) != null) {
                                json = preferences.getString("list", null);
                                list = gson.fromJson(json, ArrayList.class);
                                for (int i = 0; i < list.size(); i++) {
                                    if (list.get(i).equals(IP)) {
                                        isEqualIP = true;
                                        break;
                                    }
                                }
                            }
                            if (isEqualIP) {
                                Toast.makeText(getContext(), MyApplication.getContext().getString(R.string.sameIP), Toast.LENGTH_SHORT).show();
                            } else {
                                list.add(IP);
                                json = gson.toJson(list);
                                editor.putString("list", json);
                                editor.putString("IP", IP);
                                editor.commit();
                                Toast.makeText(getContext(), preferences.getString("IP", "") + MyApplication.getContext().getString(R.string.registIP), Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        }
                    })
                    .show();
        }
        //登録検知メッセージ
        else if (editText.getText().toString() == null ||
                editText.getText().toString().length() == 0 || spinner.getSelectedItemPosition() == 0) {
            button.setEnabled(false);
            new Handler().postDelayed(new Runnable() {
                public void run() {
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
            viewPager.setCurrentItem(0);
        }
        button.setFocusable(false);
        button.setFocusableInTouchMode(false);
    }

    /**
     * スピナーダイアログから選択されたアイテムを保存し、ダイアログを消す
     */
    private DialogInterface.OnClickListener onDialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            // AlertDialogで選択された内容を保持
            selectedIndex = which;
            editor.putString("IP", adapter.getItem(which));
            editor.commit();
            Toast.makeText(getContext(), adapter.getItem(which) + MyApplication.getContext().getString(R.string.registIP), Toast.LENGTH_SHORT).show();
            alertDialog.dismiss();
        }
    };


    /**
     * プリファレンスの設定情報を画面に登録する.
     */
    public void onResume() {
        super.onResume();

        ArrayAdapter<CharSequence> myadapter = ArrayAdapter.createFromResource(
                getContext(), R.array.sex_list, R.layout.myspinner);
        myadapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinner.setAdapter(myadapter);

        //プリファレンス読み込み準備
        preferences = getContext().getSharedPreferences("pref", Context.MODE_PRIVATE);
        //起動時、プロフィールの設定項目取得＆セット
        try {
            editText.setText(preferences.getString("name", name), TextView.BufferType.NORMAL);
            spinner.setSelection(preferences.getInt("sex", sex));
        } catch (NullPointerException e) {
        }
    }

}