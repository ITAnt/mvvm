/**
 * 在配合navigation使用的情况下，不要使用Fragment参数的构造方法，会有内存泄漏
 * https://blog.csdn.net/ByteDanceTech/article/details/120052166
 */
//class DataAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle): FragmentStateAdapter(fragmentManager, lifecycle) {
//
//    override fun getItemCount(): Int {
//        return 3
//    }
//
//    override fun createFragment(position: Int): Fragment {
//        // 构造子页面
//        val fragment = when (position) {
//            0 -> FirstFragment()
//            1 -> SecondFragment()
//            2 -> SnapFragment()
//            else -> SnapFragment()
//        }
//        return fragment
//    }
//}
/*
解决ViewPager2 设置 Adapter 导致的 Fragment 重建问题：
val viewPager2: ViewPager2 = ......
model.getContentList.observe(viewLifecycleOwner) {
    if（viewPager2.adapter == null）{
        val adapter: FragmentStateAdapter = ......
        adapter.data = it
        viewPager2.adapter = adapter
    } else {
        viewPager2.adapter.data = it
    }

    adapter.notifyDataSetChanged()
}*/
