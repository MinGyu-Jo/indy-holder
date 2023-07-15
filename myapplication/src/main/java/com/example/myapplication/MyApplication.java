package com.example.myapplication;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.hyperledger.indy.sdk.pool.Pool;
import org.hyperledger.indy.sdk.wallet.Wallet;

import kotlin.Pair;
import kr.co.bdgen.indywrapper.IndyWrapper;
import kr.co.bdgen.indywrapper.config.PoolConfig;
import kr.co.bdgen.indywrapper.config.WalletConfig;

public class MyApplication extends Application {
    private final String WALLET_PREFERENCES = "WALLET_PREFERENCES";
    private final String PREF_KEY_DID = "PREF_KEY_DID";
    private final String PREF_KEY_VER_KEY = "PREF_KEY_VER_KEY";
    private final String PREF_KEY_MASTER_SECRET = "PREF_KEY_Master_SECERT";

    private static Wallet wallet;

    public static Wallet getWallet() {
        return wallet;
    }

    public String getDid() {
        String did = null;
        SharedPreferences pref = getSharedPreferences(WALLET_PREFERENCES, MODE_PRIVATE);
        did = pref.getString(PREF_KEY_DID, "");
        return did;
    }

    public String getMasterSecret() {
        String masterSecret = null;
        SharedPreferences pref = getSharedPreferences(WALLET_PREFERENCES, MODE_PRIVATE);
        masterSecret = pref.getString(PREF_KEY_MASTER_SECRET, "");
        return masterSecret;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //wallet setting
        //1. indy library 초기화
        IndyWrapper.init(this);

        //2 . ledger 사용을 위한 pool config 설정
        String poolName = PoolConfig.getPoole(this);
        try {
            // PoolConfig.getPoole(this);

            //3. ledger 환경 사용을 위해 pool ledger 열기
            Pool.openPoolLedger(poolName, "{}").get();

            //4. wallet 생성
            WalletConfig.createWallet(this).get();

            //5. wallet 열기
            wallet = WalletConfig.openWallet().get();

            //6.did, verKey 생성
            Pair<String, String> didAndVerKey = WalletConfig.createDid(wallet).get();

            //7. master secret 생성
            String masterSecret = WalletConfig.createMasterSecret(wallet, null);

            //8. 생성한 did, verKey, masterSecretId 저장
            SharedPreferences prefs = getSharedPreferences(WALLET_PREFERENCES, MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(PREF_KEY_DID, didAndVerKey.getFirst());
            editor.putString(PREF_KEY_VER_KEY, didAndVerKey.getSecond());
            editor.putString(PREF_KEY_MASTER_SECRET, masterSecret);
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("[INDY ERROR!]", e.getMessage(), e);
        }
    }
}

// 230714 PM 3에 추가된 MyApplication 자바 클래스 파일