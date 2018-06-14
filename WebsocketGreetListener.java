package com.ubtechinc.cruzr.user.core.socketgreet;

/**
 * Created by ubt on 2018/5/29.
 */

public interface WebsocketGreetListener {
    /**
     * 单人打招呼
     *
     * @param greetWords 打招呼语
     * @param scheme     方案
     */
    void greetToSingle(String greetWords, int scheme);

    /**
     * 多人打招呼
     *
     * @param greetWords 打招呼语
     * @param scheme     方案
     */
    void greetToAll(String greetWords, int scheme);
}
