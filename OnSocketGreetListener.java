package com.ubtechinc.cruzr.user.core.socketgreet;

import com.ubtechinc.cruzr.user.data.entity.Greeting;

/**
 * Created by ubt on 2018/6/2.
 */

public interface OnSocketGreetListener {
    /**
     * 迎宾执行错误回调
     * @param greeting 迎宾内容
     * @param count 识别到的人数
     * @param message 错误原因
     */
    void onGreetError(Greeting greeting, int count, String message);
    /**
     * 迎宾结束回调
     * @param greeting 迎宾内容
     * @param count 识别到的人数
     */
    void onGreetEnd(Greeting greeting, int count);
}
