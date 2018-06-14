package com.ubtechinc.cruzr.user.core.socketgreet;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;
import com.ubtechinc.cruzr.user.core.greet.GreetUtils;
import com.ubtechinc.cruzr.user.data.dao.DBConstant;
import com.ubtechinc.cruzr.user.data.entity.Greeting;
import com.ubtechinc.cruzr.user.utils.PreferUtils;

import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by ubt on 2018/5/29.
 */

public class GreetWebSocketService extends Service implements Emitter.Listener, Runnable {

    private WebsocketGreetListener greetListener;

    // private List<SocketDataBean> commandList;
    final BlockingQueue<SocketDataBean> commandQueue = new LinkedBlockingQueue();

//    /**
//     * 服务器url地址
//     */
//    private static final String URL = "http://15.0.23.67:9099?token=1111111";

    /**
     * 服务器url地址，这个地址要做到能配置。
     */
    private static String URL = PreferUtils.getSocketUrl();

    private static final String TOKEN = "?token=1111111";
    /**
     * io socket 事件名称
     */
    private static final String SOCKET_EVENT = "ybx20180601";
    /**
     * socket对象
     */
    private Socket mSocket;

    /**
     * 当前service 状态
     */
    private boolean stoped = false;

    /**
     * 测试用的receiver
     */
    private MyBroadRec receiver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        stoped = false;
        URL = PreferUtils.getSocketUrl();
        initSocket();
        greetListener = new SocketGreetUnit(this, commandQueue);
        startGreetThread();
        // registReceiver();
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 初始化socket io
     */
    private void initSocket() {
        openSocket();
        mSocket.connect();
        mSocket.on(SOCKET_EVENT, this);
    }

    /**
     * 开始读取指令线程
     */
    private void startGreetThread() {
        Thread greetThread = new Thread(this);
        greetThread.start();
    }

    /**
     * 注册广播监听
     */
    private void registReceiver() {
        receiver = new MyBroadRec();
        IntentFilter filter = new IntentFilter();
        filter.addAction("action_send_command");
        registerReceiver(receiver, filter);
    }

    /**
     * 广播接收者
     */
    class MyBroadRec extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String command = intent.getStringExtra("command");
            Log.d("chenqiang", "receive command is " + command);
            Gson gson = new Gson();
            SocketDataBean data = gson.fromJson(command, SocketDataBean.class);
            try {
                commandQueue.put(data);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("chenqiang", "service is destroyed");
        stoped = true;
        cancel();
        //unregisterReceiver(receiver);
        restart();
    }

    @Override
    public void call(Object... objects) {
        Log.d("chenqiang", "shou dao io socket  object0 === " + objects[0].toString());
        Gson gson = new Gson();
        SocketDataBean data = gson.fromJson((String) objects[0], SocketDataBean.class);
        String greetWords = data.getMsg();
        long timeStamp = data.getTimestamp();
        Log.d("chenqiang", "receive io socket msg msg is " + greetWords + "  and time stamp is " + timeStamp);
        try {
            commandQueue.put(data);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 开始连接
     */
    private void openSocket() {
        try {
            Log.d("chenqiang", "open socket  socket is ==" + URL + TOKEN);
            mSocket = IO.socket(URL + TOKEN);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    /**
     * 取消连接
     */
    public void cancel() {
        mSocket.disconnect();
        mSocket.off(SOCKET_EVENT);
    }

    /**
     * 重启服务
     */
    private void restart() {
        startService(new Intent(this, GreetWebSocketService.class));
    }

    @Override
    public void run() {
        //识别到单人
        while (true) {
            if (SocketGreetUnit.mState == SocketGreetUnit.STATE_IDLE) {
                try {
                    SocketDataBean data = commandQueue.take();
                    if (greetListener != null && data != null) {
                        greetListener.greetToSingle(data.getMsg(), data.getGreet_scheme());
                        data = null;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
