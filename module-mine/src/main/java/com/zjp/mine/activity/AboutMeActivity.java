package com.zjp.mine.activity;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.zjp.aop.permission.annotation.CheckPermission;
import com.zjp.base.activity.BaseActivity;
import com.zjp.mine.R;
import com.zjp.mine.databinding.ActivityAboutMeBinding;
import com.zjp.mine.viewmodel.MineViewModel;

/**
 * Created by zjp on 2020/08/18 15:51
 */
public class AboutMeActivity extends BaseActivity<ActivityAboutMeBinding, MineViewModel> {

    @Override
    protected void initImmersionBar() {
        super.initImmersionBar();
        mImmersionBar
                .statusBarDarkFont(true)
                .init();
    }

    public static void start(Context context) {
        context.startActivity(new Intent(context, AboutMeActivity.class));
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_about_me;
    }

    @Override
    protected void initView() {
        super.initView();
        mViewDataBinding.titleview.setTitle("关于作者");
    }

    @Override
    protected void initData() {
        super.initData();

//        mViewDataBinding.ivAlipay.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View view) {
//                return false;
//            }
//        });

        mViewDataBinding.ivAlipay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                test();
            }
        });
    }

    @CheckPermission(permissions = {
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.READ_EXTERNAL_STORAGE"})
    private void test() {
    }
}
