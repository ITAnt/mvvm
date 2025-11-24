package com.miekir.mvvm.core.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner

/**
 * Copyright (C), 2019-2020, Miekir
 *
 * @author Miekir
 * @date 2020/10/7 22:46
 * Description:
 *
 * View (strong)-> Presenter (strong)-> Model (strong)-> Repository
 * View <-(weak) Presenter <-(weak) Model <-(weak) Repository
 * ---------------------------------------------------------------------------------
 * 什么时候用 onSaveInstanceState，什么时候用 ViewModel，什么时候用 Fragment.setRetainInstance?
 * onSaveInstanceState 能够在两种情况下恢复信息：1. 系统配置变化，2. 应用进程被系统回收。
 * 由于 onSaveInstanceState 使用 Bundle 来将数据序列化到磁盘，因此我们在使用 onSaveInstanceState 时应当注意：
 * 不要存储大量数据，只能存储简单的数据结构及基本类型。因此我们只能存储一些必要的数据，比如用户 ID，当我们重建 Activity 时，
 * 应用可以根据恢复的用户 ID 再次去网络请求 或 查询本地数据库，来获取更多信息以恢复页面。
 *
 * ViewModel 保存的数据是在内存中，可以跨越系统配置变化，但是不能在应用进程被系统回收时依然保持数据。
 * ViewModel 可以保持更复杂的数据结构。（似乎最近又出了一个 SavedStateHandle）
 * ViewModel 完全可以替代 Fragment.setRetainInstance。事实上，ViewModel 的内部实现就调用了 Fragment.setRetainInstance。
 *
 * 正确的使用姿势应该是，onSaveInstanceState 和 ViewModel 结合使用。
 * 当系统回收应用进程后，onSaveInstanceState 中的 Bundle 不为空，开发者应当将 Bundle 传给 ViewModel。
 * ViewModel 发现自己缓存的数据为空，因此使用 Bundle 中的数据来加载页面。
 * 当应用配置改变而重建 Activity 时，onSaveInstanceState 中的 Bundle 不为空，开发者应当将 Bundle 传给 ViewModel。
 * 由于 ViewModel 自己有缓存的数据，因此最后由 ViewModel 自己决定使用缓存的数据还是 Bundle 中的数据。
 * 参考：SavedStateHandle
 * 链接：https://www.jianshu.com/p/60f2ed95b124
 * ---------------------------------------------------------------------------------
 *
 * 严格模式（次推荐，适合界面数据量变量非常多的情况）：
 * Activity持有一个（或多个，这种情况下一般为一个，因为数据一般是集中在一个ViewModel里绑定）ViewModel，
 * ViewModel里延迟初始化多个Model，ViewModel同时持有可观察的数据变量，
 * 类Presenter模式：Model单纯做耗时任务（如网络请求），被ViewModel开协程执行，做完任务后，ViewModel拿到返回值更新数据变量，联动View更新界面。
 * 终极正宗：把开协程执行这个步骤放到Model里执行，这样ViewModel会更纯粹，只有数据变量定义，但要把ViewModel实例传给Model去开协程，
 * 以及任务完成后更新ViewModel的数据变量，联动View更新界面。
 *
 * 因为Android的ViewModel自带生命周期，非常适合直接当作Model来做任务，界面关掉viewModelScope发起的协程任务也会被取消。
 * 且ViewModel的工作（即绑定界面和变量）已经由Android的DataBinding实现，我们没必要仅仅为了变量数据的定义而单独抽出一层，
 * 甚至把Model压到下一层，把ViewModel引用传下去，这样做稍显繁琐，所以这数据变量不是很多的情况下，推荐使用不严格模式。
 *
 * 不严格模式（数据变量不多的情况下，推荐）：
 * Activity持有多个ViewModel，
 * ViewModel持有可观察的数据变量，同时充当Model的角色，做完任务后，改变ViewModel的数据变量，ViewModel再通知View更新界面。
 * 当然，如果认为Model是可观察的数据实体（如WPF），则这种才是标准模式
 *
 * Presenter模式（不推荐）：
 * Activity持有多个ViewModel，
 * 其中一个ViewModel只存放可观察的数据变量，
 * 其他ViewModel充当Model角色，但持有Activity的引用，做完任务后，通过Activity的引用拿到那个存放数据变量的ViewModel引用，
 * 改变ViewModel的数据变量，ViewModel再通知View更新界面。
 *
 * 屏幕旋转场景下，刚好在Activity销毁和重建的间隙完成任务，此时数据刷新将不会在新Activity体现，建议使用LiveData刷新界面，
 * 所以业务MVVM不要在ViewModel持有Activity引用（恢复Dialog的除外）
 */
//abstract class BaseViewModel<V : IView?> : ViewModel()

///**
// * 安全地执行方法，供Java使用
// * @param consumer 要执行的操作
// */
//private fun post(consumer: Consumer<in V>) {
//    Optional.ofNullable(getView()).ifPresent(consumer);
//}


///**
// * 使用：val viewModel by viewModel { DataViewModel(1) }
// */
//inline fun <reified VM : ViewModel> AppCompatActivity.viewModel(
//    noinline factory: () -> VM,
//): Lazy<VM> = viewModels { ParamViewModelFactory(factory) }

/**
 * 构造函数带参数的ViewModel
 */
class ParamViewModelFactory<VM : ViewModel>(
    private val factory: () -> VM,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = factory() as T
}

/**
 * 延迟加载Presenter【推荐】，由于初始化Presenter与使用总是在主线程进行，可使用 LazyThreadSafetyMode.NONE优化性能
 * 模式不会有任何线程安全的保证以及相关的开销
 * 使用：
 * private val mPresenter: AppPresenter by lazy()
 * 或者：
 * private val mPresenter by lazy<AppListActivity, AppPresenter>()
 * 如果不同功能（P不同）的ViewModel使用同一个名称，第二个ViewModel初始化的时候会让第一个ViewModel 的onCleared被调用一次，finish的时候会再调一次
 * 如果key相同，则指向同一个对象，默认和by viewModels返回对象是同一个
 *
 * @param factory 传入的构造函数，适用于在需要在构造函数附带参数的ViewModel，一个例子：
 * private val viewModel by viewModel({ MainViewModel("hello world") })
 * 比modelClass.getConstructor(params::class.java).newInstance(params)更安全且灵活
 *
 * 有一个全局变量，引用了Activity中的一个view（如GeckoView，且如果不为空不重新初始化该全局变量），Activity销毁之后，
 * 重新打开，再次使用那个全局变量去加载界面，加载完成后，回调给Activity，实际上是回调给第一个Activity，
 * 此时会出现各种异常，如lifecycleScope.launch不能执行，ViewModel里启动协程失败等。（无论是本例还是官方的by viewmodels都无法避免）
 */
inline fun <reified P : ViewModel> IView.viewModel(noinline factory: (() -> P)? = null, key: String? = null) = lazy(/*LazyThreadSafetyMode.NONE*/) {
    if (this !is ViewModelStoreOwner) {
        throw RuntimeException("当前类必须是ViewModelStoreOwner的子类")
    }
    if (factory == null) {
        val presenter: P = if (key == null) {
            ViewModelProvider(this as ViewModelStoreOwner).get(P::class.java)
        } else {
            ViewModelProvider(this as ViewModelStoreOwner).get(key, P::class.java)
        }
        presenter
    } else {
        val presenter: P = if (key == null) {
            ViewModelProvider(this as ViewModelStoreOwner, ParamViewModelFactory(factory)).get(P::class.java)
        } else {
            ViewModelProvider(this as ViewModelStoreOwner, ParamViewModelFactory(factory)).get(key, P::class.java)
        }
        presenter
    }
}


