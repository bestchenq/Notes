package com.ubtechinc.cruzr.user.core.socketgreet;

import android.text.TextUtils;

import com.ubtechinc.cruzr.user.core.greet.GreetUtils;
import com.ubtechinc.cruzr.user.core.greet.OnGreetListener;
import com.ubtechinc.cruzr.user.core.module.GreetDisplayWindow;
import com.ubtechinc.cruzr.user.core.module.WindowContext;
import com.ubtechinc.cruzr.user.core.robot.RobotController;
import com.ubtechinc.cruzr.user.data.entity.Behavior;
import com.ubtechinc.cruzr.user.data.entity.Greeting;
import com.ubtechinc.cruzr.user.utils.MyLogger;

import java.util.List;

/**
 * Created by ubt on 2018/5/30.
 */

public class SocketGreeter implements OnSocketBehaviorListener {
    /**
     * 来宾人数
     */
    private int mCustomerCount;

    /**
     * 对应的迎宾配置
     */
    private Greeting mGreeting;
    /**
     * 对应的迎宾行为列表
     */
    private List<Behavior> mBehaviorList;
    /**
     * 当前迎宾行为索引
     */
    private int mCurrentBehaviorIndex;
    /**
     * 当前迎宾行为执行任务
     */
    private SocketGreetTask mCurrentTask;
    /**
     * 迎宾监听器
     */
    private OnSocketGreetListener mListener;
    /**
     * 创建Window的上下文环境
     */
    private WindowContext mWindowContext;
    /**
     * 显示窗口,控制其生命周期
     */
    private GreetDisplayWindow mDisplayWindow;

    private String mGreetWords;

    public SocketGreeter(WindowContext windowContext) {
        this.mWindowContext = windowContext;
        // this.mGreetWords = greetWords;
    }

    public void setOnGreetListener(OnSocketGreetListener listener) {
        this.mListener = listener;
    }

    /**
     * 向来宾打招呼
     *
     * @param greeting 迎宾内容
     * @param count    来宾人数
     */
    public synchronized void greet(Greeting greeting, String greetWords, int count) {
        this.mCustomerCount = count;
        this.mGreeting = greeting;
        this.mGreetWords = greetWords;

        if (greeting == null) {
            MyLogger.log().e("greet fail, error: greeting is null.");
            if (mListener != null) {
                mListener.onGreetError(null, count, "greeting is null");
            }
            return;
        }

        //获取合适的迎宾行为列表
        mBehaviorList = GreetUtils.getBehaviorList(greeting);
        if (mBehaviorList == null || mBehaviorList.size() <= 0) {
            MyLogger.log().e("greet fail, error: get behavior list is null.");
            if (mListener != null) {
                mListener.onGreetError(greeting, count, "get behavior list is null");
            }
            return;
        }

        //决定是否创建显示Window
        for (Behavior behavior : mBehaviorList) {
            //需要播放图片或视频
            if (GreetUtils.isImage(behavior.getDisplay()) || GreetUtils.isVideo(behavior.getDisplay())) {
                mDisplayWindow = new GreetDisplayWindow(mWindowContext);
                break;
            }
        }

        this.mCurrentBehaviorIndex = 0;
        createTaskAndRun(mBehaviorList.get(mCurrentBehaviorIndex), mGreeting.getType(), mDisplayWindow, greetWords);
    }

    /**
     * 执行指定行为
     */
    private void createTaskAndRun(Behavior behavior, int type, GreetDisplayWindow window, String greetWords) {
        mCurrentTask = new SocketGreetTask(behavior, type);
        mCurrentTask.setDisplayWindow(window);
        mCurrentTask.setOnBehaviorListener(this);
        MyLogger.log().d("create no." + (mCurrentBehaviorIndex + 1) + " behavior task and run.");
        mCurrentTask.run(greetWords);
    }

    public synchronized void stop() {
        if (mCurrentTask == null) {
            return;
        }
        mCurrentTask.cancel();
    }

    /**
     * 清空屏幕显示
     */
    private void clearDisplay() {
        if (mDisplayWindow != null) {
            mDisplayWindow.clear();
        }
        RobotController.getInstance().hideExpression();
    }

    /**
     * 统一管控屏幕显示
     */
    @Override
    public synchronized void onBehaviorEnd(SocketGreetTask task) {
        //更新当前行为索引
        mCurrentBehaviorIndex++;

        //任务被取消
        if (task.isCanceled()) {
            MyLogger.log().d("greet end, No." + mCurrentBehaviorIndex + " behavior have been canceled.");
            clearDisplay();
            if (mListener != null) {
                mListener.onGreetEnd(mGreeting, mCustomerCount);
            }
            return;
        }

        //所有行为执行完毕
        if (mCurrentBehaviorIndex >= mBehaviorList.size()) {
            MyLogger.log().d("greet end, " + mBehaviorList.size() + " behaviors have been ran.");
            clearDisplay();
            if (mListener != null) {
                mListener.onGreetEnd(mGreeting, mCustomerCount);
            }
            return;
        }

        //下一条无屏幕显示，清除当前显示
        Behavior behavior = mBehaviorList.get(mCurrentBehaviorIndex);
        if (TextUtils.isEmpty(behavior.getDisplay())) {
            clearDisplay();
        }

        //执行下一条
        createTaskAndRun(behavior, mGreeting.getType(), mDisplayWindow, mGreetWords);
    }

}
