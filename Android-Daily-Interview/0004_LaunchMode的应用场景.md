为什么要有启动模式？因为在 Android 中，启动一个 Activity 有时需要创建一个新的对象，有时需要复用已有的对象。**Android 系统引入 Activity 栈的概念【活动栈】，便于管理应用进程中的 Activity 的行为**。

Activity 的行为，涉及到自身启动时的模式。Activity 的启动模式【LaunchMode】有 4 种：

* Standard ：**标准**模式

  含义：每次启动一个 Activity 就会创建一个新的实例。

  注意：使用 ApplicationContext 去启动 standard 模式 Activity 就会报错。因为 standard 模式的 Activity 会**默认进入启动它所属 Activity 的活动栈**，但是由于非 Activity 的 Context 没有所谓的活动栈。

  生命周期：每次被创建的实例 Activity 的生命周期符合典型情况，它的 onCreate、onStart、onResume 都会被调用。

  应用场景：应用与大多数的Activity。

* SingleTop：**栈顶复用**模式

  含义：分两种处理情况：需要创建的 Activity 已经处于栈顶时，此时会直接复用栈顶的 Activity，不会再创建新的 Activity；若须要创建的 Activity 不处于栈顶，此时会又一次创建一个新的 Activity 入栈，同 Standard 模式一样。

  生命周期：若情况一中栈顶的 Activity 被直接复用时，它的 onCreate、onStart 不会被系统调用，因为它并没有发生改变。可是一个新的方法 onNewIntent 会被回调（Activity被正常创建时不会回调此方法）。

  应用场景：假设你在当前的 Activity 中又要启动同类型的 Activity，此时建议将此类型 Activit y的启动模式指定为SingleTop，能够降低 Activity 的创建，节省内存！比如，App **用户收到几条好友请求的推送消息**，需要用户点击推送通知进入到请求者个人信息页，将信息页设置为 SingleTop 模式就可以**增强复用性**。

* SingleTask：**栈内复用**模式

  含义：若须要创建的 Activity 已经处于栈中时，此时不会创建新的 Activity，而是将存在栈中的 Activity 上面的其他 Activity 所有销毁，使它成为栈顶。

  生命周期：同 SingleTop 模式中的情况一同样。仅仅会又一次回调 Activity 中的 onNewIntent 方法。

  应用场景：保持我们应用开启后仅仅有一个 Activity 的实例。最典型的样例就是应用中展示的主页（Home页）。假设用户在主页跳转到其他页面，运行多次操作后想返回到主页，即应用中的返回首页按钮功能。

* SingleInstance：**全局单例**模式

  含义：是全局单例模式，是一种加强的 SingleTask 模式。它除了具有它所有特性外，还加强了一点：具有此模式的 Activity 仅仅能单独位于一个活动栈中。

  应用场景：这个经常使用于系统中的应用，比如 Launch、锁屏键的应用等等，整个系统中仅仅有一个！所以在我们的应用中一般不会用到。如闹钟的提醒页面，当你在 A 应用中看视频时，闹钟响了，你点击闹钟提醒通知后进入提醒详情页面，然后点击返回就再次回到 A 的视频页面，这样就**不会过多干扰到用户先前的操作**了。