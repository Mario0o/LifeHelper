# ANR检测和捕获
#### 目录介绍
- 01.基础概念介绍
- 02.常见思路和做法
- 03.Api调用说明
- 04.遇到的坑分析
- 05.其他问题说明



### 01.基础概念说明
#### 1.1 WatchDog卡顿监控
- 如何监控UI卡顿
    - 启动一个卡顿检测线程，该线程定期的向UI线程发送一条延迟消息，执行一个标志位加1的操作，如果规定时间内，标志位没有变化，则表示产生了卡顿。如果发生了变化，则代表没有长时间卡顿，我们重新执行延迟消息即可。
- 具体的原理说明
    - 具体的原理和实现方法很简单：不断向UI线程发送Message，每隔一段时间检查一次刚刚发送的Message是否被处理，如果没有被处理，则说明这段时间主线程被卡住了。


#### 1.2 WatchDog的优缺点
- 优缺点
    - 优点：简单，稳定，结果论，可以监控到各种类型的卡顿
    - 缺点：轮询不优雅，不环保，有不确定性，随机漏报
- 间隔时间设置
    - 这种方法的轮询的时间间隔选择很重要，又让人左右为难，轮询的时间间隔越小，对性能的负面影响就越大，而时间间隔选择的越大，漏报的可能性也就越大。
- 如何理解性能负面影响
    - 前者很容易理解，UI线程要不断处理我们发送的Message，必然会影响性能和功耗。


#### 1.3 如何理解漏报数据
- 时间间隔选择了4秒
    - 事实上，之前是想要通过这种方案来监控ANR，当然，这并不严谨。来分析一下
- 举一个例子
    - 每隔4秒，向主线程发送一个消息。下面是轮训的过程
    - 0秒 ---- 4秒 ---- 8秒 ---- 12秒 ---- 16秒
    - 现在有一个5秒的卡顿发生在第2秒，结束在第7秒，这种情况无论是在0-4秒的周期内，还是4-8秒的周期内，都有一段时间是不卡顿的，消息都可以被处理掉，这种情况自然就无法被监控到。
- 计算监控成功率
    - 计算公式：p = x/a - 1 ；注意条件（a<= x <= 2a）
    - 上面案例计算 ： p = 5/4 - 1 = 0.25 ; 如果轮询间隔设置为4秒，发现一个5秒的卡顿的概率仅为25%。
- 修改轮训间隔时间
    - 默认轮询间隔为5秒，如果有一个8秒的卡顿（8秒已经很容易产生ANR），被发现的概率也只有8/5-1=60%
    - 从这个概率公式还可以发现，对于一个固定的轮询间隔，只有卡顿时间大于两倍的轮询间隔，才能百分之百被监控到。
- 思考把间隔时间缩短
    - 每隔2秒，向主线程发送一个消息。下面是轮训的过程
    - 0秒 -- 2秒 -- 4秒 -- 6秒 -- 8秒 -- 10秒 -- 12秒
    - 现在有一个6秒的卡顿发生在第1秒，结束在第7秒，那么这个在在2-4，和4-6区间可以捕获到。



### 02.常见思路和做法
#### 2.1 核心思路
- ANR-WatchDog机制原理不复杂，它内部启动了一个子线程，定时通过主线程Handler发送Message，然后定时去检查Message的处理结果。
- 通俗来说就是利用了Android系统MessageQueue队列的排队处理特性。通过主线程Handler发送消息到MessageQueue队列，5秒去看下这个Message有没有被消费，如果消费了则代表没有卡顿，如果没有，则代表有卡顿，当然这个5秒是可调节的。
                                          



### 03.Api调用说明



### 04.遇到的坑分析
#### 4.1 WatchDog监控弊端
- 1.它会漏报情况，举个例子，比如我们以5秒未响应作为卡顿阈值，如果我们发送监听Message的时间在上一个消息处理的第2-5秒之间，那这种就会产生漏报。
- 2.监听间隔越小，系统开销越大。
- 3.即便监听到了，不好区分卡顿链路，无法提供准确的卡顿堆栈。




### 05.其他问题说明



### 参考博客
- anr日志生成与捕获方式分析
    - https://www.jianshu.com/p/7fa9080cb97e

