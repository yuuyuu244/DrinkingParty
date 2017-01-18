package jjj.drinkingparty;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * 使い方説明画面.
 * コードはなく画像、文字をxmlから読み取り、表示する.
 *
 * @author 江城達也
 */
public class ManualFragment extends Fragment{

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manual, null);
    }

}

