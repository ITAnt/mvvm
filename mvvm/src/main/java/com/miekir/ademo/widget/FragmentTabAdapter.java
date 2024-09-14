//package com.miekir.mvvm.widget;
//
//import androidx.annotation.NonNull;
//import androidx.fragment.app.Fragment;
//import androidx.fragment.app.FragmentManager;
//import androidx.fragment.app.FragmentPagerAdapter;
//import androidx.fragment.app.FragmentStatePagerAdapter;
//
//import org.jetbrains.annotations.NotNull;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//
///**
// * 如非必要，不要把list传进来，参考YoFace的实现：http://blog.jianjie.life:11111/index.php/archives/1134/
// * Created by Jason on 2018/8/20.
// * 在Fragment里使用ViewPager的适配器
// * 传参：
// for (int i = 0, len = taskList.size(); i < len; i++) {
//    Fragment fragment = new ElderlyTaskFragment();
//    Bundle bundle = new Bundle();
//    bundle.putInt(ElderlyTaskFragment.KEY_INDEX, i);
//    fragment.setArguments(bundle);
//    fragmentList.add(fragment);
// }
// * fragmentList变化之后，必须紧跟notifyDataSetChanged，所以setOffscreenPageLimit要在notifyDataSetChanged
// * 之后调用，如果setOffscreenPageLimit大小为整个fragment list的size，则Activity创建的时候，会回调所有Fragment
// * 的onCreateView
// *
// * FragmentStatePagerAdapter
// * 顾名思义，我们的这个FragmentStatePagerAdapter，在我们切换不同的Fragment的时候，我们会把前面的Fragment销毁，而我们系统在销毁前，会把我们的我们Fragment的Bundle在我们的onSaveInstanceState(Bundle)保存下来。等用户切换回来的时候，我们的Fragment就会根据我们的instance state恢复出来。
// *
// * FragmentPagerAdapter（所以可能在onDestroyView后会报内存泄漏，因为没有执行onDestroy）
// * 使用这种Adapter，我们的Fragment在切换的时候，不会销毁，而只是调用事务中的detach方法，这种方法，我们只会把我们的Fragment的view销毁，而保留了以前的Fragment对象。所以通过这种方式创建的Fragment一直不会被销毁。
// *
// * 作者：coolma
// * 链接：https://www.jianshu.com/p/3d68d6ec9468
// * 来源：简书
// * 著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。
// */
//@Deprecated
//public class FragmentTabAdapter extends FragmentStatePagerAdapter {
//
//    private final List<Fragment> tabFragments;
//
//    private List<String> titles;
//
//    public FragmentTabAdapter(FragmentManager fm, @NonNull List<Fragment> fragments, @NonNull List<String> titles) {
//        // 解决懒加载的问题
//        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
//        this.tabFragments = fragments;
//        this.titles = titles;
//    }
//
//    public FragmentTabAdapter(FragmentManager fm, @NonNull List<Fragment> fragments) {
//        // 解决懒加载的问题
//        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
//        this.tabFragments = fragments;
//    }
//
//    public FragmentTabAdapter(FragmentManager fm, Fragment[] fragments) {
//        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
//        tabFragments = new ArrayList<>(Arrays.asList(fragments));
//    }
//
//    @NotNull
//    @Override
//    public Fragment getItem(int position) {
//        return tabFragments.get(position);
//    }
//
//    /**
//     * 参考：https://www.jianshu.com/p/60f2ed95b124
//     * 解决移除Fragment后刷新不起作用的问题
//     * <a href="https://stackoverflow.com/questions/10849552/update-viewpager-dynamically">解决移除Fragment后刷新不起作用的问题</a>
//     * 由于屏幕旋转、语言更改、字体大小变化导致Activity重建，Fragment也会重建，导致新new的Fragment不会执行onCreateView方法，
//     * 我们看到列表是没有数据的一堆Fragment（因为没有调用onCreateView）， 并且这些没有数据的Fragment在Activity销毁的时候不会回调onDestroy（因为没有onCreateView）
//     * -------------------------------------------------------------------------------
//     * 遇到无法阻挡的Activity重建，如显示大小、系统语言等这些无法简单通过在清单文件配置configChanges避免重建的情况，会造成部分手机显示和交互混乱（跨Fragment），
//     * 有两种解决Activity重建+Fragment重建导致Fragment数据混乱的问题：
//     * 方案①：重新拉取数据。activity onCreate判断savedInstanceState不为空就start一个新的当前页面重新拉取数据，同时finish掉旧的Activity，只在standard的启动模式下有效
//     * 方案②：重新拉取数据。在Fragment的onCreateView里判断是否需要销毁自动重建的Fragment
//     if (!ElderlyHelper.INSTANCE.getFragmentList().contains(this)) {
//        elderlyActivity.getSupportFragmentManager().beginTransaction().remove(this).commitNowAllowingStateLoss();
//        return;
//     }
//     * 方案③（推荐，适用于大多数场景）：重新拉取数据。在Activity重写protected void onSaveInstanceState(Bundle outState) {方法，不要调用super.onSaveInstanceState或传new Bundle()
//     * 虽然不会影响ViewModel的存储，但不知道是否有其他影响，但在Activity是不能使用outState保存数据了(即不能从onCreate的savedInstanceState恢复数据，即使savedInstanceState不为空)
//     @Override
//     protected void onSaveInstanceState(@NonNull Bundle outState) {
//        super.onSaveInstanceState(new Bundle());
//     }
//     * 方案④：使用旧数据（次推荐，适用于非常重要的临时界面，如涉及支付结果的界面）。onSaveInstanceState保存数据，允许重建，也就是使用方案②的做法，只不过数据从内存加载，而不是重新拉取
//     * 方案⑤：使用旧数据。数据持久化到本地，允许重建，也就是使用方案②的做法，只不过数据从磁盘加载，而不是重新拉取
//     * -------------------------------------------------------------------------------
//     * Activity重建时，需要重新拉取数据的话，数据放Activity，需要使用旧数据不重新拉取的话的话放ViewModel（如一些切换横竖屏需要加载不同xml场景无法通过配置configChanges实现），
//     * 但只记住创建fragment所需的dataList即可，每个控件的状态要管理好，最好使用DataBinding辅助
//     * 数据保存个人经验：
//     * 一、界面没有Fragment
//     * ①不保存数据，放Activity，重建刷新，对结果要求严格则从服务器校验；
//     * ②保存数据，放ViewModel，可适用大多数情况；如对结果要求严格，onSaveInstanceState保存+onRestoreInstanceState恢复
//     * 二、界面有Fragment
//     * ①不保存数据super.onSaveInstanceState(new Bundle());，list放Activity，重建刷新，对结果要求严格则从服务器校验
//     * ③保存数据，则Activity重建时的fragment list从ViewModel获取且要在具体fragment里刷新（根据index进行替换）。如对结果要求严格，onSaveInstanceState保存+onRestoreInstanceState恢复
//     * 三、不推荐持久化数据到本地，要加密、区分用户等，还要考虑清理缓存
//     * -------------------------------------------------------------------------------
//     */
//    @Override
//    public int getItemPosition(@NonNull Object object) {
//        Fragment fragment = (Fragment)object;
//        int position = tabFragments.indexOf(fragment);
//        if (position >= 0) {
//            return position;
//        } else {
//            return POSITION_NONE;
//        }
//    }
//
//    @Override
//    public int getCount() {
//        return tabFragments == null ? 0 : tabFragments.size();
//    }
//
//    @Override
//    public CharSequence getPageTitle(int position) {
//        if (titles != null && position <= titles.size()) {
//            return titles.get(position);
//        }
//        return super.getPageTitle(position);
//    }
//}
