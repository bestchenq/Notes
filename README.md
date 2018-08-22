# Notes
notes, websites ....  
位域相关介绍  
http://blog.csdn.net/xubuju/article/details/53896127


python 微信聊天机器人  
https://www.jianshu.com/p/7af30151cd50?winzoom=1.375


Android WebView 全面干货指南  
https://www.jianshu.com/p/fd61e8f4049e

在 Android 设备上搭建 Web 服务器  
https://www.jianshu.com/p/6d2f324c8f42


Java 虚拟机.md  
https://github.com/CyC2018/Interview-Notebook/blob/master/notes/Java%20%E8%99%9A%E6%8B%9F%E6%9C%BA.md

数字签名和数字证书  
https://github.com/Tencent/VasDolly/wiki/VasDolly%E5%AE%9E%E7%8E%B0%E5%8E%9F%E7%90%86

https://www.jianshu.com/p/97be3137741c  


85篇技术好文助你Android进阶  
https://www.jianshu.com/p/b336b5bb666a  
  
tag是对历史一个提交id的引用，如果理解这句话就明白了
使用git checkout tag即可切换到指定tag，例如：git checkout v0.1.0
切换到tag历史记录会处在分离头指针状态，这个是的修改是很危险的，在切换回主线时如果没有合并，之前的修改提交基本都会丢失，如果需要修改可以尝试git checkout -b branch tag创建一个基于指定tag的分支，例如：git checkout -b tset v0.1.0  这个时候就会在分支上进行开发，之后可以切换到主线合并  

Android - Activity 启动过程  
https://github.com/jeanboydev/Android-ReadTheFuckingSourceCode/blob/master/android/Android-Activity%E5%90%AF%E5%8A%A8%E8%BF%87%E7%A8%8B.md  
互联网大型公司（阿里腾讯百度等）android面试题目(有答案)  
https://www.jianshu.com/p/fb815eaf628f  

2.关于线程池的分类
2.1 FixedThreadPool  
通过Executors的newFixedThreadPool()方法创建，它是个线程数量固定的线程池，该线程池的线程全部为核心线程，它们没有超时机制且排队任务队列无限制，因为全都是核心线程，所以响应较快，且不用担心线程会被回收。  

2.2 CachedThreadPool  
通过Executors的newCachedThreadPool()方法来创建，它是一个数量无限多的线程池，它所有的线程都是非核心线程，当有新任务来时如果没有空闲的线程则直接创建新的线程不会去排队而直接执行，并且超时时间都是60s，所以此线程池适合执行大量耗时小的任务。由于设置了超时时间为60s，所以当线程空闲一定时间时就会被系统回收，所以理论上该线程池不会有占用系统资源的无用线程。  

2.3 ScheduledThreadPool  
通过Executors的newScheduledThreadPool()方法来创建，ScheduledThreadPool线程池像是上两种的合体，它有数量固定的核心线程，且有数量无限多的非核心线程，但是它的非核心线程超时时间是0s，所以非核心线程一旦空闲立马就会被回收。这类线程池适合用于执行定时任务和固定周期的重复任务。  

2.4 SingleThreadExecutor  
通过Executors的newSingleThreadExecutor()方法来创建，它内部只有一个核心线程，它确保所有任务进来都要排队按顺序执行。它的意义在于，统一所有的外界任务到同一线程中，让调用者可以忽略线程同步问题。   

LeakCanary内存检测原理   
https://yq.aliyun.com/articles/184116  



机器人迎宾websocket接口协议：
{
    "title": "greet_command",
    "timestamp": 1527493694140,
    "greet_scheme": 1,
    "msg": [
        {
            "greet_words": "xxx局长好！"
        },
        {
            "greet_words": "xxx主任好"
        }
    ]
}


协议说明：
"title": "greet_command",  指令标题  固定值greet_command
"timestamp": 1527493694140, 当前时间戳
"greet_scheme": 1,   迎宾方案，和机器人本地的迎宾方案相对应，默认为1，表示设置迎宾方案为方案一。
"msg": 是一个数组列表，当只识别到一个人的时候就对应机器人的单人迎宾，当有多个人的时候就对应机器人的多人迎宾。  

Android性能优化来龙去脉总结
https://cloud.tencent.com/developer/article/1145224     

https://linux.linuxidc.com/index.php  
linux 公社资源库  

https://www.jianshu.com/p/37c263f9886b  
TraceView的详细使用   

关于ClassLoader相关知识  
https://blog.csdn.net/briblue/article/details/54973413   


https://blog.csdn.net/sybnfkn040601/article/details/73194613   
HashMap的长度为什么设置为2的n次方  

https://mp.weixin.qq.com/s?__biz=MzI1MzYzMjE0MQ==&mid=2247484502&idx=2&sn=a60ea223de4171dd2022bc2c71e09351&scene=21#wechat_redirect  
一种极低成本的Android屏幕适配方式   
  
  2018 Android 社招面试经验：我是如何拿到京东，爱奇艺，猎豹移动，摩拜等六家 offer 的？  
  https://xiaozhuanlan.com/topic/5290483671  
    
    
    
  Java并发编程：volatile关键字解析  
  http://www.cnblogs.com/dolphin0520/p/3920373.html



