`LocalBroadcastManager` 用来管理 `APP` 内部广播的发送和接收，即：发出的广播只能在应用程序内部进行传递，并且广播接收者也只能接收到来自此应用程序的广播。

`BroadcastReceiver` 的通信机制是 `Binder`，而 `LocalBroadcastManager` 的实现机制则是内存缓存中维护的 `ReceiverRecord` 和 `BroadcastRecord` 映射表。

~~~java
public final class LocalBroadcastManager {
    private static final String TAG = "LocalBroadcastManager";
    private static final boolean DEBUG = false;
    private final Context mAppContext;
    
    private final HashMap<BroadcastReceiver, ArrayList<LocalBroadcastManager.ReceiverRecord>> mReceivers = new HashMap();
    
    private final HashMap<String, ArrayList<LocalBroadcastManager.ReceiverRecord>> mActions = new HashMap();
    
    private final ArrayList<LocalBroadcastManager.BroadcastRecord> mPendingBroadcasts = new ArrayList();
    
    static final int MSG_EXEC_PENDING_BROADCASTS = 1;
    private final Handler mHandler;
    private static final Object mLock = new Object();
    private static LocalBroadcastManager mInstance;
    ...
~~~

核心用法：

1. 调用 `LocalBroadcastManager.getInstance(Context context)` 获取到单例实例；
2. 调用 `localBroadcastManager.registerReceiver(BroadcastReceiver receiver, IntentFilter filter)` 注册广播接收者；
3. 调用 `localBroadcastManager.sendBroadcast(@NonNull Intent intent)` 发送广播；
4. 调用 `localBroadcastManager.unregisterReceiver(@NonNull BroadcastReceiver receiver)` 取消注册广播接收者。

相比较 `BroadcastReceiver` 来说，`LocalBroadcastManager` 使用 `Handler` 实现了应用内的通信，利用 `IntentFilter` 的 match 功能，提供了消息的发布和接收功能，当然更加高效，安全性也更高。

另外，现在 `BroadcastReceiver` 不再推荐使用静态注册方式，Android 8.0 之后限制了绝大部分广播只能使用动态注册。

