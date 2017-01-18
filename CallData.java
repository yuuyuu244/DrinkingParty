package jjj.drinkingparty;

import java.io.Serializable;

import io.skyway.Peer.Peer;

/**
 * データを保持するためのクラス（オブジェクト）.
 * <br>画面間でのデータの受け渡し時に使用する。
 * <br>Created by 152123 on 2016/12/20.
 *
 * @author Yuki_Kikuya
 */

public class CallData implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 自クラスのインスタンス生成 */
    private static final CallData instance  = new CallData();
    /** 自分のPeerId */
    private String myId;
    /** 相手のPeerId */
    private String yourId;
    /** 相手のニックネーム */
    private String yourName;
    /** 自分のPeerを保持 */
    private Peer peer;
    /** 画面が２度目に生成されたか判定するフラグ */
    private boolean backFlag =false;

    /** デフォルトコンストラクタ */
    private CallData (){
    }

    /**
     * 自インスタンスを返すゲッターメソッド.
     *
     * @return 自インスタンス
     */
    public static CallData getInstance() {
        return instance;
    }

    /**
     * 自分のIDを返すゲッターメソッド.
     *
     * @return 自分のID
     */
    public String getMyId() {
        return myId;
    }

    /**
     * 相手のIDを返すゲッターメソッド.
     *
     * @return 相手のID
     */
    public String getYourId(){
        return yourId;
    }

    /**
     * 相手のニックネームを返すメソッド.
     *
     * @return 相手のニックネーム
     */
    public String getYourName() {
        return yourName;
    }

    /**
     * 自分のPeerを返すメソッド.
     *
     * @return 自分のPeer
     */
    public Peer getPeer() {
        return peer;
    }

    /**
     * 画面戻りフラグを返すメソッド.
     *
     * @return 画面戻りフラグ
     */
    public boolean getBackFlag(){
        return backFlag;
    }

    /**
     * 自分のIDをセットするメソッド.
     *
     * @param myId 自分のID
     */
    public void setMyId(String myId) {
        this.myId = myId;
    }

    /**
     * 相手のIDをセットするメソッド.
     *
     * @param yourId 相手のID
     */
    public void setYourId(String yourId) {
        this.yourId = yourId;
    }

    /**
     * 相手のニックネームをセットするメソッド.
     *
     * @param yourName 相手の名前
     */
    public void setYourName(String yourName) {
        this.yourName = yourName;
    }

    /**
     * 自分のPeerをセットするメソッド.
     *
     * @param peer 自分のPeer
     */
    public void setPeer(Peer peer) {
        this.peer = peer;
    }

    /**
     * 画面戻りフラグをセットするメソッド.
     *
     * @param backFlag 画面戻り判定フラグ
     */
    public void setBackFlag(boolean backFlag) {
        this.backFlag = backFlag;
    }
}

