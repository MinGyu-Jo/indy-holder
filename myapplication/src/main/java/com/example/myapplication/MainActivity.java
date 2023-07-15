package com.example.myapplication;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import kr.co.bdgen.indywrapper.data.payload.OfferPayload;
import kr.co.bdgen.indywrapper.repository.CredentialRepository;
import kr.co.bdgen.indywrapper.repository.IssuingRepository;

public class MainActivity extends AppCompatActivity {
    private OfferPayload offer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView text = (TextView) findViewById(R.id.txt_main);
        String credJsonArray = getCredential();
        text.setText(credJsonArray);

        //deeplink 처리
        // deeplink = indy://holder ?secret=blarblar
        // scheme = indy
        // host = holder
        // queryParameter = secret

        Uri data = getIntent().getData();
        if (data == null)
            return;
        String secret = data.getQueryParameter("secret");
        Log.d("[SUCCESS]", secret);

        //1. offer 받기
        IssuingRepository repository = new IssuingRepository();
        repository.reuqestOffer(
                secret,
                offerPayload -> {
                    Log.d("[SUCCESS]", offerPayload.getCredDefJson() + "\n" + offerPayload.getCredOfferJson());
                    offer = offerPayload;

                    //2. request and issue credential
                    MyApplication context = ((MyApplication) getApplicationContext());
                    repository.requestCredential(

                            context.getWallet(),
                            context.getDid(),
                            context.getMasterSecret(),
                            secret,
                            offer,
                            (credentialInfo, issuePayLoad) -> {
                                Log.d(
                                        "[SUCCESS]",
                                        credentialInfo.getCredReqMetadataJson() +
                                                "\n" +
                                                credentialInfo.getCredReqJson() +
                                                "\n" +
                                                credentialInfo.getCredDefJson() +
                                                "\n" +
                                                issuePayLoad.getCredentialJson()

                                );

                                //3. store credential
                                repository.storeCredential(
                                        MyApplication.getWallet(),
                                        // non-static 문제 해결방법은 MyApplication 대신 context로 변경
                                        credentialInfo,
                                        issuePayLoad,
                                        cred -> {
                                            Log.i("[SUCCESS]", "credential = " + cred);
                                            return null;
                                        },
                                        error -> {
                                            Log.e("[ERROR", error.getMessage(), error);
                                            return null;
                                        }
                                );
                                return null;
                            },
                            throwable -> {
                                Log.e("[ERROR]", throwable.getMessage(), throwable);
                                return null;
                            }
                    );

                    return null;
                },
                error -> {
                    Log.e("[ERROR", error.getMessage(), error);
                    return null;
                }
        );

        /*
        terminal emulator 실행 code
        $
        $ cd $ ADB_HOME
        $ adb shell am start
            -W -a android.intent.action.VIEW
            -d "indy://holder?secret=test1;
         */


    }

    ;
    /*
     * 저장한 credential 정보를 받아오기 위한 함수
     * @return credential json array를 담은 raw data
     */

    private String getCredential() {
        String credential;
        CredentialRepository credentialRepository = new CredentialRepository();
        credential = credentialRepository.getRawCredentials(
                MyApplication.getWallet(),
                "{}" // credentional 저장 기능 구현
        );
        return credential;
    }
}
