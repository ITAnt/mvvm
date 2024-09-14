//package com.miekir.common.utils;
//
//import androidx.lifecycle.ViewModelProvider;
//import androidx.lifecycle.ViewModelStoreOwner;
//
//import com.miekir.mvp.presenter.BasePresenter;
//import com.miekir.mvp.presenter.Presenter;
//import com.miekir.mvp.view.base.IView;
//
//import java.lang.reflect.Field;
//
///**
// * Copyright (C), 2019-2020, Miekir
// *
// * @author Miekir
// * @date 2020/11/14 13:32
// * Description:
// */
//public class ViewHelper {
//    private ViewHelper() {
//    }
//
//    /**
//     * 初始化添加注解的变量
//     * final Class<? extends IView> getClass，可以代表接口的实现类
//     */
//    public static void initVariables(final ViewModelStoreOwner owner, final IView iView) {
//        // 这里可以获取到子类的成员变量
//        Field[] fields = iView.getClass().getDeclaredFields();
//        for (Field field : fields) {
//            // 获取变量上面的注解类型
//            Presenter presenterAnnotation = field.getAnnotation(Presenter.class);
//            if (presenterAnnotation == null) {
//                continue;
//            }
//
//            try {
//                field.setAccessible(true);
//                // 父类引用指向子类对象
//                Class<? extends BasePresenter<?>> type = (Class<? extends BasePresenter<?>>) field.getType();
//                //BasePresenter<V> presenter = type.newInstance();
//                // Activity重建后，依然可以通过context获取之前的ViewModel(presenter)
//                BasePresenter presenter = new ViewModelProvider(owner).get(type);
//                field.set(iView, presenter);
//                presenter.attachView(iView);
//            } catch (IllegalAccessException e) {
//                e.printStackTrace();
//            } catch (ClassCastException e) {
//                e.printStackTrace();
//                throw new RuntimeException(Presenter.class.getName() + "注解修饰的类必须继承自：" + BasePresenter.class.getName());
//            }
//        }
//    }
//}
