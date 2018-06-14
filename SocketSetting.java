package com.ubtechinc.cruzr.user.core.socketgreet;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import com.ubtechinc.cruzr.user.R;
import com.ubtechinc.cruzr.user.utils.PreferUtils;

public class SocketSetting extends AppCompatActivity {

    private EditText sockUrlEdt;

    private String oldUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_socket_setting);
        initView();
    }

    private void initView() {
        sockUrlEdt = (EditText) findViewById(R.id.socket_url_edt);
        oldUrl = PreferUtils.getSocketUrl();
        sockUrlEdt.setText(oldUrl);
    }

    @Override
    protected void onStop() {
        super.onStop();
        String newUrl = sockUrlEdt.getText().toString();
        String portNumStr = newUrl.substring(newUrl.lastIndexOf(":") + 1);
        int portNum = 0;
        try {
            portNum = Integer.parseInt(portNumStr);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!oldUrl.equals(newUrl) && portNum <= 65535) {
            PreferUtils.writeSocketUrl(newUrl);
            stopSocketService();
        }
    }

    /**
     * 关闭服务
     *
     * @return
     */
    private boolean stopSocketService() {
        return stopService(new Intent(this, GreetWebSocketService.class));
    }

}
