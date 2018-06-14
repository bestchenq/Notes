package com.ubtechinc.cruzr.user.core.socketgreet;

import android.content.Context;

import com.ubtechinc.cruzr.user.core.greet.GreetUtils;
import com.ubtechinc.cruzr.user.core.module.WindowContext;
import com.ubtechinc.cruzr.user.data.dao.DBConstant;
import com.ubtechinc.cruzr.user.data.entity.Greeting;

import java.util.concurrent.BlockingQueue;

/**
 * Created by ubt on 2018/5/29.
 */

public class SocketGreetUnit extends WindowContext implements WebsocketGreetListener, OnSocketGreetListener {
    //打招呼的的具体实例
    private SocketGreeter mGreeter;

    public static final int STATE_IDLE = 0x00;
    public static final int STATE_RECOGNIZING = 0x01;
    public static final int STATE_GREETING = 0x02;

    private BlockingQueue<SocketDataBean> taskQueue;
    /**
     * 记录当前状态
     */
    public static int mState;

    public SocketGreetUnit(Context context, BlockingQueue<SocketDataBean> taskQueue) {
        super(context);
        this.taskQueue = taskQueue;
    }

    @Override
    public void greetToSingle(String greetWords, int scheme) {
        Greeting greeting = GreetUtils.getDefaultGreeting(DBConstant.ITEM_TYPE_DEFAULT_SINGLE_USER);
        if (mState == STATE_IDLE) {
            startGreeting(greeting, greetWords, 1);
        }
    }

    @Override
    public void greetToAll(String greetWords, int scheme) {
        Greeting greeting = GreetUtils.getDefaultGreeting(DBConstant.ITEM_TYPE_DEFAULT_MULTI_USERS);
    }

    /**
     * 开始迎宾
     */
    private void startGreeting(Greeting greeting, String greetWords, int count) {
        sync(STATE_GREETING);
        mGreeter = new SocketGreeter(this);
        mGreeter.setOnGreetListener(this);
        mGreeter.greet(greeting, greetWords, count);
    }

    @Override
    public void onGreetError(Greeting greeting, int count, String message) {
        sync(STATE_IDLE);
    }

    @Override
    public void onGreetEnd(Greeting greeting, int count) {
        sync(STATE_IDLE);
    }

    /**
     * 同步内部状态
     */
    private synchronized void sync(int state){
        mState = state;
    }
}
