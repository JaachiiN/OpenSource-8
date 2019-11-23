Handler 引起的**内存泄露**相关**知识点和疑问**：
1. Java 中内部类；
2. Java 中 强引用、软引用、弱引用、虚引用；
3. andorid.os.Handler 的使用，以及如何避免内存泄露。

下面是正文：

在 Android 系统中，Handler 是一个**消息发送和处理机制的核心组件**之一，与之配套的其他主要**组件**还有 Looper 和 Message，MessageQueue。其中，Message 和 Runnable 是消息的载体，MessageQueue 是消息等待的队列。Looper 则负责从队列中取出消息。

android.os.Handler 整个**组件的作用**在于：

* 让一个行为（Message 和 Runnable）在其他线程中执行，实现线程间的通信；

  在子线程中拿到主线程的 Handler 实例，并使用该实例发送消息。以此达到了子线程给其他线程发送消息的目的。

* 安排调度消息和可执行的 Runnable，调度时间可以是立即执行，或者延迟执行。

那**为什么**在使用 Handler 时会导致**内存泄漏**呢？拿一个简单的实例来举例子：

~~~java
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    // 采用匿名内部类的方式创建 Handler 实例
    private Handler mainHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (MSG_WHAT == msg.what) {
                LogUtil.trace("主线程获取 Message");
            }
        }
    };
    ...
~~~

上述代码的问题在于：创建 Handler 实例时，采用了**匿名内部类**的写法【特征：覆写了 `void handleMessage(Message msg)`】，是 MainActivity 这个实例的内部类。

> 什么内存泄漏？
>
> 指程序在申请内存后，无法释放已申请的内存空间。一次内存泄漏危害可以忽略，但内存泄漏堆积的后果会很严重。无论多少内存，迟早会被占光（也就是经常说的**内存泄漏最终会导致内存溢出！**）。
>
> 产生该现象的原理：无用对象（不再使用的对象）持续占用内存或无用对象的内存得不到及时释放，从而造成的内存空间的浪费称为内存泄漏。
>
> 具体表现：长生命周期的对象持有短生命周期的对象的引用就很可能发生内存泄漏。尽管短生命周期对象已经不再需要，但是因为长生命周期对象持有它的应用而导致不能被回收。

在 Java 中，关于内部类有一个特点：**非静态的内部类**和**匿名内部类**都会隐式地持有外部类的对象。所以，当前场景下，mainHandler 实例持有了 MainActivity 对象。

下列情形下会导致内存泄露：MainActivity 生命周期结束，垃圾回收器却迟迟不能回收该 Activity 占用的内存。**原因出在 mainHandler 实例还引用着 MainActivity 对象，而 mainHandler 则被其所在线程的 MessageQueue 引用着**。

> 解释：Handler 实例持有了 MainActivity 对象，Message 这持有了 Handler 对象，MessageQueue 中包含了各种各样的 Message！
>
> 若 Handler 实例不再持有 MainActivity 对象，也就找到了解决办法。

从上面的场景，可以得到以下相应的**解决方案**：

* 保证 Activity 在生命周期结束时，Handler 所在线程的 MessageQueue 不再持有 Handler 对象。一个很常见的实例：Handler 用于发送延时 Message。因此在 Activity 结束时，手动将消息队列清空 `workThread.getWorkThreadHandler().removeCallbacksAndMessages(null);` 这也就是指明了内存泄露的根本原因：**Activity 组件生命周期结束时，仍然存在 Message 还未被处理**。此时 `Activity` 已经执行了整个生命周期方法。
* 让 Handler 不再持有 MainActivity 的实例——将 Handler 实例声明为 static 或者自定义静态内部类！但随之而来的是：在 `void handleMessage(Message msg)` 中不能再调用外部的非静态方法。总体来看，这并不是一个好办法！
* 在上述方法的基础上，为了能够调用外部的实例方法，需传递一个弱引用进来。
* 自定义 Handler 类，并放入一个单独的顶层类文件中。

针对**弱引用**的实例，可使用下述代码：

~~~java
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private final int MSG_WHAT = 0xFF;

    private WorkThread workThread;
    private MainHandler mainHandler;

    private static class MainHandler extends android.os.Handler {

        private final WeakReference<MainActivity> mActivity;

        public MainHandler(MainActivity mainActivity) {
            // 创建一个关于 MainActivity 实例的弱引用
            mActivity = new WeakReference<>(mainActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            MainActivity mainActivity = mActivity.get();
            if (mainActivity != null) {
                // do something

                // 可以在 static 域中，通过弱引用的方式访问到外部变量和方法
                if (mainActivity.MSG_WHAT == msg.what) {
                    LogUtil.trace("主线程获取 Message");
                }
            } else {
                // do nothing
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.findViewById(R.id.btn_send_runnable).setOnClickListener(this);
        this.findViewById(R.id.btn_send_message).setOnClickListener(this);

        mainHandler = new MainHandler(MainActivity.this);
    }
    ...
~~~

关于 Java 中**强引用、软引用、弱引用和虚引用**的概念：

| 强引用（Strong Reference） | 默认引用，如果一个对象具有强引用，垃圾回收器绝不会回收该引用所指向的内存空间。JVM 宁愿抛出 OutOfMemory 内存溢出异常使程序终止，也不会释放该部分内存来解决内存不足的问题。 |
| :------------------------: | ------------------------------------------------------------ |
|  软引用（SoftReference）   | 如果内存足够，则不会回收；反之，如果内存不足，这会回收这部分引用所指向的内存空间。 |
|  弱引用（WeakReference）   | 在垃圾回收器一旦发现只有弱引用的对象，不管当前内存空间足够与否，都会回收它的内存。在 android.os.Handler 导致的内存泄漏解决方案中使用！ |
| 虚引用（PhantomReference） | 如果一个对象仅有虚引用，那么它就和没有任何引用一样，在任何时候都可能被垃圾回收器回收。 |

另外，**针对抽取出单独类的方式**，有如下示例代码：

~~~java
package com.zebra.os;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.lang.ref.WeakReference;

/**
 * @Description: 自定义封装的 Handler 类
 * @Author: yaya
 * @CreateDate: 2019/8/5 20:58
 */
public class WeakReferenceHandler extends Handler {
	// Handler.Callback 的弱引用
    private WeakReference<Callback> mWeakReference;

    public WeakReferenceHandler(Callback callback) {
        mWeakReference = new WeakReference<>(callback);
    }

    public WeakReferenceHandler(Looper looper, Callback callback) {
        super(looper);
        mWeakReference = new WeakReference<>(callback);
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);

        if (mWeakReference != null && mWeakReference.get() != null) {
            Callback callback = mWeakReference.get();
            callback.handleMessage(msg);
        }
    }
}

~~~

以及相应的使用方法：

~~~java
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private final int MSG_WHAT = 0xFF;

    private WorkThread workThread;

    // 在主线程中创建 Callback 实例，使用的是匿名内部类，引用了外部的 MainActivity 实例
    private Handler.Callback mainCallback = new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            if (MSG_WHAT == msg.what) {
                LogUtil.trace("主线程获取 Message");
            }

            return true;
        }
    };

    // 此处的 Handler 实际是主线程的 Handler
    private Handler mainHandler = new WeakReferenceHandler(mainCallback);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.findViewById(R.id.btn_send_runnable).setOnClickListener(this);
        this.findViewById(R.id.btn_send_message).setOnClickListener(this);
    }
    ...
~~~

由于 mainCallback 持有的是 MainActivity 对象， WeakReferenceHandler 使用 mainCallback 创建了 Handler 实例，而且是**弱引用方式**。一旦 MainActivity 生命周期结束，垃圾回收器发现  WeakReferenceHandler 实例对 mainCallback 仅是一种弱引用，这会回收这部分内存。

由于是弱引用，当该类需要被回收时，可以直接被回收掉。那如果不使用上述的解决方案，是不是一定会出现内存泄漏？答案是不一定会存在内存泄露的：

1. 如果 Handler 中没有耗时操作，任务完成也就是释放了对象持有的 Activity 引用；
2. 那如果 Handler 中是有一个死循环（MessageQueue 中等待执行的 Message 正排着队呢！），就会存在内存泄露。

针对于内存泄露，可以使用 LeakCanary 工具检测。
