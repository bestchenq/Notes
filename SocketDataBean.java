package com.ubtechinc.cruzr.user.core.socketgreet;

/**
 * Created by ubt on 2018/5/29.
 */

public class SocketDataBean {
//    {
//        "title": "greet_command",
//            "timestamp": 1527493694140,
//            "greet_scheme": 1,
//            "msg": "xxx好"
//    }
    private String title;
    private long timestamp;
    private int greet_scheme;
    private String msg;

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setGreet_scheme(int greet_scheme) {
        this.greet_scheme = greet_scheme;
    }

    public int getGreet_scheme() {
        return greet_scheme;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }
}
