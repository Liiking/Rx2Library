package com.http.httpdemo;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import com.google.gson.Gson;
import com.http.httpdemo.http.ApiManager;
import com.http.httpdemo.http.subscribers.SubscriberListener;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends BaseActivity {

    String GET_CONTENT_LIST = "onebox/exchange/query";
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.textView);
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                apiTest();
            }
        });
    }

    private void apiTest() {
        Map<String, String> p = new HashMap<>();
        p.put("type", "shishang");
        p.put("key", "095e3c7a2288d20bb664fd12c6c57a57");
        ApiManager.getInstance()
                .requestGet(getDisposableFlag(), GET_CONTENT_LIST, Object.class, p, new SubscriberListener<Object>() {
                    @Override
                    public void onNext(Object obj) {
                        textView.setText(new Gson().toJson(obj));
                    }
                });
    }

}
