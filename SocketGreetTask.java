package com.ubtechinc.cruzr.user.core.socketgreet;

import android.text.TextUtils;
import android.util.Log;

import com.ubtechinc.cruzr.sdk.ros.RosConstant;
import com.ubtechinc.cruzr.serverlibutil.interfaces.RemoteCommonListener;
import com.ubtechinc.cruzr.serverlibutil.interfaces.SpeechTtsListener;
import com.ubtechinc.cruzr.user.UserMgrApplication;
import com.ubtechinc.cruzr.user.core.greet.GreetUtils;
import com.ubtechinc.cruzr.user.core.greet.OnBehaviorListener;
import com.ubtechinc.cruzr.user.core.greet.OnDisplayListener;
import com.ubtechinc.cruzr.user.core.module.GreetDisplayWindow;
import com.ubtechinc.cruzr.user.core.robot.RobotController;
import com.ubtechinc.cruzr.user.data.entity.Behavior;
import com.ubtechinc.cruzr.user.utils.MyLogger;

/**
 * Created by ubt on 2018/5/30.
 */

public class SocketGreetTask implements SpeechTtsListener, RemoteCommonListener, OnDisplayListener {
    /**
     * 定义状态变量，高位表示行为模式，及其执行过程是独立的(1)还是关联的(0)；
     * 低位表示行为状态，1表示执行完成，0表示执行中
     */
    private static final int MASK_MODE = 0xf0;              // 1111 0000
    private static final int MASK_STATUS = 0x0f;            // 0000 1111

    private static final int STATUS_TTS_END = 0x01;         // 0000 0001
    private static final int STATUS_DISPLAY_END = 0x02;     // 0000 0010
    private static final int STATUS_ACTION_END = 0x04;      // 0000 0100
    private static final int STATUS_BEHAVIOR_END = 0x07;      // 0000 0111

    private static final int MODE_TTS_INDEPENDENT = 0x10;         // 0001 0000
    private static final int MODE_DISPLAY_INDEPENDENT = 0x20;     // 0010 0000
    private static final int MODE_ACTION_INDEPENDENT = 0x40;      // 0100 0000

    /**
     * 记录行为执行状态
     */
    private int mStatus;
    /**
     * 是否被取消
     */
    private boolean mCanceled;
    /**
     * 迎宾类型，这个对象主要是
     * 默认单人， 默认多人
     */
    private int mGreetingType;
    /**
     * 对应的迎宾行为
     */
    private Behavior mBehavior;
    /**
     * 记录动作执行Id
     */
    private int mRunId;
    /**
     * 显示窗口
     */
    private GreetDisplayWindow mDisplayWindow;
    /**
     * 监听器
     */
    private OnSocketBehaviorListener mListener;

    public SocketGreetTask(Behavior behavior, int type) {
        this.mStatus = 0;
        this.mCanceled = false;

        this.mBehavior = behavior;

        this.mGreetingType = type;
    }

    public void setDisplayWindow(GreetDisplayWindow window) {
        this.mDisplayWindow = window;
    }

    public void setOnBehaviorListener(OnSocketBehaviorListener listener) {
        this.mListener = listener;
    }

    public void run(String greetWords) {
        String display = mBehavior.getDisplay();
        //无屏幕显示
        if (TextUtils.isEmpty(display)) {
            Log.d("chenqiang", "display is empty and mBehavior.getText() == " + mBehavior.getText());
            MyLogger.log().d("display content is null, nothing to show.");
            mStatus = mStatus | STATUS_DISPLAY_END;
            //播报语音
            speak(greetWords, mGreetingType);
            //动作执行
            act(mBehavior.getAction());
            return;
        }

        //显示表情
        if (GreetUtils.isExpression(display)) {
            Log.d("chenqiang", "isExpression(display)  and mBehavior.getText() === " + mBehavior.getText());
            String expression = GreetUtils.getExpression(display);
            MyLogger.log().d("show expression ---> " + expression);
            RobotController.getInstance().showExpression(expression);
            //播报语音
            speak(greetWords, mGreetingType);
            //动作执行
            act(mBehavior.getAction());
            return;
        }

        //播放视频 或 图片，通过监听显示开始回调，来启动语音和动作
        display(display, mBehavior.getDuration());
    }

    /**
     * 屏幕显示，若设置了屏幕显示，则其保持最后一项结束
     */
    private void display(String display, long duration) {

        //显示视频，独立显示，视频播放完成后结束
        if (GreetUtils.isVideo(display)) {
            mStatus = mStatus | MODE_DISPLAY_INDEPENDENT;
            String path = GreetUtils.getVideoPath(display);
            MyLogger.log().d("play video ---> " + path);
            if (mDisplayWindow != null) {
                mDisplayWindow.setOnDisplayListener(this);
                mDisplayWindow.playVideo(path);
            }
            return;
        }

        //显示图片，关联显示,其他行为结束后，延时duration后结束
        if (GreetUtils.isImage(display)) {
            String path = GreetUtils.getImagePath(display);
            MyLogger.log().d("show picture ---> " + path + " duration = " + duration + "ms");
            if (mDisplayWindow != null) {
                mDisplayWindow.setOnDisplayListener(this);
                mDisplayWindow.showImage(path);
            }
            //若只有图片显示,即刻启动延时退出
            if (TextUtils.isEmpty(mBehavior.getText()) && TextUtils.isEmpty(mBehavior.getAction())) {
                if (mDisplayWindow != null) {
                    mDisplayWindow.dismissImageDelayed(mBehavior.getDuration());
                }
            }
        }
    }

    @Override
    public synchronized void onDisplayStart(String path) {
        MyLogger.log().d("onDisplayStart ---> " + path + " mCanceled = " + mCanceled);
        if (mCanceled) {
            return;
        }
        //隐藏表情
        RobotController.getInstance().hideExpression();
        //播报语音
        speak(mBehavior.getText(), mGreetingType);
        //动作执行
        act(mBehavior.getAction());
    }

    @Override
    public void onDisplayEnd() {
        MyLogger.log().d("onDisplayEnd ---> " + mBehavior.getDisplay());
        sync(STATUS_DISPLAY_END);
    }

    @Override
    public void onDisplayError(String message) {
        MyLogger.log().d("onDisplayError ---> " + mBehavior.getDisplay());
        sync(STATUS_DISPLAY_END);
    }

    /**
     * 语音播报
     */
    private void speak(String content, int type) {

        //语音只存在独立模式
        mStatus = mStatus | MODE_TTS_INDEPENDENT;
        //未设置语音，认为播报完成
        if (TextUtils.isEmpty(content)) {
            MyLogger.log().d("speech content is null");
            mStatus = mStatus | STATUS_TTS_END;
            return;
        }
        //迎宾播报的tts
        String tts = content;
        MyLogger.log().d("speech tts is ---> " + tts);
        //开始播报语音
        RobotController.getInstance().speakThenDo(tts, this);
    }

    @Override
    public void onAbort() {
        MyLogger.log().d("tts:" + mBehavior.getText() + " onAbort()");
        if (STATUS_TTS_END == (mStatus & STATUS_TTS_END)) {
            return;
        }
        sync(STATUS_TTS_END);
    }

    @Override
    public void onEnd() {
        MyLogger.log().d("tts:" + mBehavior.getText() + " onEnd()");
        if (STATUS_TTS_END == (mStatus & STATUS_TTS_END)) {
            return;
        }
        sync(STATUS_TTS_END);
    }

    /**
     * 执行动作
     */
    private void act(String action) {

        //未设置动作
        if (TextUtils.isEmpty(action)) {
            MyLogger.log().d("action is null");
            mStatus = mStatus | STATUS_ACTION_END;
            return;
        }

        //随机动作
        if (GreetUtils.isRandomAction(action)) {

            String randomAction = RobotController.getInstance().getRandomAction();
            mRunId = RobotController.getInstance().run(randomAction, this);
            if (mRunId <= 0) {
                MyLogger.log().e("run random action " + randomAction + " fail.");
                mStatus = mStatus | STATUS_ACTION_END;
                return;
            }
            MyLogger.log().d("record run random action:" + randomAction + " sectionId = " + mRunId);
            return;
        }

        //独立动作
        mStatus = mStatus | MODE_ACTION_INDEPENDENT;
        mRunId = RobotController.getInstance().run(action, this);
        if (mRunId <= 0) {
            MyLogger.log().e("run action " + action + " fail.");
            mStatus = mStatus | STATUS_ACTION_END;
        }
        MyLogger.log().d("record run behavior action" + action + " sectionId = " + mRunId);
    }

    /**
     * 当前回调只返回最后一个动作的结果
     */
    @Override
    public synchronized void onResult(int sectionId, int status, String message) {

        MyLogger.log().d("run action sectionId = " + sectionId + " status = " + status + " message = " + message);

        //一个动作正常结束
        if (RosConstant.Action.ACTION_FINISHED == status) {

            //这是一个随机动作
            if ((mStatus & MODE_ACTION_INDEPENDENT) != MODE_ACTION_INDEPENDENT) {
                //屏幕显示
                if (((mStatus & STATUS_DISPLAY_END) != STATUS_DISPLAY_END) ||
                        //或语音播报 未结束
                        ((mStatus & STATUS_TTS_END) != STATUS_TTS_END)) {
                    //继续执行动作
                    String randomAction = RobotController.getInstance().getRandomAction();
                    mRunId = RobotController.getInstance().run(randomAction, this);
                    //动作执行失败
                    if (mRunId <= 0) {
                        MyLogger.log().e("run random action " + randomAction + " fail.");
                        mStatus = mStatus | STATUS_ACTION_END;
                        return;
                    }
                    MyLogger.log().d("record run random action:" + randomAction + " sectionId = " + mRunId);
                    return;
                }
            }

            //独立动作执行结束
            sync(STATUS_ACTION_END);
            return;
        }

        //动作执行失败，不再执行
        if (status >= RosConstant.Action.ACTION_BE_IMPEDED) {
            sync(STATUS_ACTION_END);
        }
    }

    /**
     * 同步结束状态
     *
     * @param status 同步的状态
     */
    private synchronized void sync(int status) {

        if (STATUS_TTS_END != status && STATUS_DISPLAY_END != status && STATUS_ACTION_END != status) {
            return;
        }

        mStatus = mStatus | status;

        // ------>  播报、屏幕显示、动作均结束
        if ((mStatus & STATUS_BEHAVIOR_END) == STATUS_BEHAVIOR_END) {
            MyLogger.log().d("behavior task is " + (mCanceled ? "canceled..." : "end..."));
            //解绑监听器
            RobotController.getInstance().unregisterRemoteCommonListener();
            if (mListener != null) {
                //注释 陈强
                mListener.onBehaviorEnd(this);
            }
            return;
        }

        //任务被强制取消，不需要关联停止
        if (mCanceled) {
            return;
        }

        // ------> 语音播报结束
        if (STATUS_TTS_END == status) {

            //独立动作未结束
            if ((mStatus & MODE_ACTION_INDEPENDENT) == MODE_ACTION_INDEPENDENT &&
                    (mStatus & STATUS_ACTION_END) != STATUS_ACTION_END) {
                return;
            }

            //独立显示（视频）未结束
            if ((mStatus & MODE_DISPLAY_INDEPENDENT) == MODE_DISPLAY_INDEPENDENT &&
                    (mStatus & STATUS_DISPLAY_END) != STATUS_DISPLAY_END) {
                return;
            }

            //关联动作未结束
            if ((mStatus & STATUS_ACTION_END) != STATUS_ACTION_END) {
                //不关注其执行结果，通过变更动作模式为独立动作，保证某个（失败：上一个 或 成功：复位）动作结束回调时会记录结束。
                mRunId = RobotController.getInstance().run(RobotController.ACTION_RESET, this);
                MyLogger.log().d("record run reset action sectionId = " + mRunId);
                mStatus = mStatus | MODE_ACTION_INDEPENDENT;
                return;
            }

            //关联显示未结束，更新状态未结束
            if ((mStatus & STATUS_DISPLAY_END) != STATUS_DISPLAY_END) {
                String display = mBehavior.getDisplay();
                //表情
                if (GreetUtils.isExpression(display)) {
                    //为保证显示切换的流畅性，只改变状态，当下个显示任务占据屏幕后，才关闭此次显示
                    //RobotController.getInstance().hideExpression();
                    sync(STATUS_DISPLAY_END);
                    return;
                }

                //延时结束图片显示
                if (mDisplayWindow != null) {
                    mDisplayWindow.dismissImageDelayed(mBehavior.getDuration());
                }
                return;
            }

            return;
        }

        // ------> 屏幕显示结束
        if (STATUS_DISPLAY_END == status) {

            //TTS未结束
            if ((mStatus & STATUS_TTS_END) != STATUS_TTS_END) {
                return;
            }

            //独立动作未结束
            if ((mStatus & MODE_ACTION_INDEPENDENT) == MODE_ACTION_INDEPENDENT &&
                    (mStatus & STATUS_ACTION_END) != STATUS_ACTION_END) {
                return;
            }

            //关联动作未结束
            if ((mStatus & STATUS_ACTION_END) != STATUS_ACTION_END) {
                mRunId = RobotController.getInstance().run(RobotController.ACTION_RESET, this);
                MyLogger.log().d("record run reset action sectionId = " + mRunId);
                mStatus = mStatus | MODE_ACTION_INDEPENDENT;
                return;
            }
            return;
        }

        // ------> 动作执行结束
        //TTS未结束
        if ((mStatus & STATUS_TTS_END) != STATUS_TTS_END) {
            return;
        }

        //独立显示未结束
        if ((mStatus & MODE_DISPLAY_INDEPENDENT) == MODE_DISPLAY_INDEPENDENT &&
                (mStatus & STATUS_DISPLAY_END) != STATUS_DISPLAY_END) {
            return;
        }

        //关联显示未结束，更新状态
        if ((mStatus & STATUS_DISPLAY_END) != STATUS_DISPLAY_END) {
            String display = mBehavior.getDisplay();

            //表情
            if (GreetUtils.isExpression(display)) {
                //为保证显示切换的流畅性，在下次任务启动后才关闭此次显示
                //RobotController.getInstance().hideExpression();
                sync(STATUS_DISPLAY_END);
                return;
            }

            //延时结束图片显示
            if (mDisplayWindow != null) {
                mDisplayWindow.dismissImageDelayed(mBehavior.getDuration());
            }
        }
    }

    public synchronized void cancel() {

        if (mCanceled) {
            return;
        }
        mCanceled = true;

        if ((mStatus & STATUS_ACTION_END) != STATUS_ACTION_END) {
            //无同步回调，需手动设置
            sync(STATUS_ACTION_END);
            //该接口会默认解绑回调
            mRunId = RobotController.getInstance().run(RobotController.ACTION_RESET);
            MyLogger.log().d("stop action: mRunId = " + mRunId);
        }

        if ((mStatus & STATUS_TTS_END) != STATUS_TTS_END) {
            sync(STATUS_TTS_END);
            RobotController.getInstance().shutUp();
            MyLogger.log().d("stop speech tts.");
        }

        if ((mStatus & STATUS_DISPLAY_END) != STATUS_DISPLAY_END) {
            String display = mBehavior.getDisplay();

            if (GreetUtils.isExpression(display)) {
                MyLogger.log().d("hide expression: display = " + display);
                //RobotController.getInstance().hideExpression();
                //无同步回调，需手动设置
                sync(STATUS_DISPLAY_END);
            } else if (GreetUtils.isImage(display)) {
                MyLogger.log().d("dismiss image: display = " + display);
                mDisplayWindow.dismissImageDelayed(0);
            } else if (GreetUtils.isVideo(display)) {
                MyLogger.log().d("stop video: display = " + display);
                mDisplayWindow.stopVideo();
            }
        }
    }

    public boolean isCanceled() {
        return mCanceled;
    }
}
