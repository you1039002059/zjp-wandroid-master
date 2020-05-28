package com.zjp.home.fragment;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.blankj.utilcode.util.ToastUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemChildClickListener;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.chad.library.adapter.base.listener.OnLoadMoreListener;
import com.google.android.material.appbar.AppBarLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;
import com.youth.banner.config.BannerConfig;
import com.youth.banner.config.IndicatorConfig;
import com.youth.banner.indicator.CircleIndicator;
import com.youth.banner.util.BannerUtils;
import com.zjp.base.fragment.BaseFragment;
import com.zjp.base.viewmodel.BaseViewModel;
import com.zjp.common.page.PageInfo;
import com.zjp.common.router.RouterFragmentPath;
import com.zjp.common.utils.CustomItemDecoration;
import com.zjp.home.R;
import com.zjp.home.adapter.HomeArticleListAdapter;
import com.zjp.home.adapter.HomeHeadBannerAdapter;
import com.zjp.home.bean.ArticleEntity;
import com.zjp.home.bean.BannerEntity;
import com.zjp.home.databinding.HomeFragmentHomeBinding;
import com.zjp.home.databinding.ProjectFragmentBinding;
import com.zjp.home.viewmodel.HomeViewModel;

import java.util.List;

@Route(path = RouterFragmentPath.Home.PAGER_HOME)
public class HomeFragment extends BaseFragment<HomeFragmentHomeBinding, HomeViewModel> {

    private HomeArticleListAdapter articleListAdapter;
    private PageInfo pageInfo;
    private boolean isLoading = true;

    @Override
    protected void initImmersionBar() {
        super.initImmersionBar();
        mImmersionBar
                .statusBarDarkFont(true)
                .init();
    }

    @Override
    public int getLayoutId() {
        return R.layout.home_fragment_home;
    }

    @SuppressLint("NewApi")
    @Override
    protected void initView() {
        super.initView();

        pageInfo = new PageInfo();
        setLoadSir(mViewDataBinding.rootview);
        mViewDataBinding.recy.setLayoutManager(new LinearLayoutManager(getActivity()));
        mViewDataBinding.recy.addItemDecoration(new CustomItemDecoration(getActivity(),
                CustomItemDecoration.ItemDecorationDirection.VERTICAL_LIST, R.drawable.linear_split_line));
        articleListAdapter = new HomeArticleListAdapter(null);
        mViewDataBinding.recy.setAdapter(articleListAdapter);

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            mViewDataBinding.cl.setElevation(10f);
//            mViewDataBinding.llRadius.setElevation(20f);
//            mViewDataBinding.recy.setNestedScrollingEnabled(false);
//        }

        //解决swiperefresh与scrollview滑动冲突问题
        mViewDataBinding.appBar.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            /**
             *   verticalOffset == 0 时候也就是Appbarlayout完全展开的时候，事件交给swiperefresh
             */
//            mViewDataBinding.swipe.setEnabled(verticalOffset == 0);
        });

        mViewDataBinding.nestscrollview.getViewTreeObserver().addOnScrollChangedListener(() -> {
            /**
             * 原理如上
             */
//            mViewDataBinding.swipe.setEnabled(mViewDataBinding.nestscrollview.getScrollY() == 0);
        });

        mViewDataBinding.nestscrollview.setOnScrollChangeListener((View.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            float alpha = 0f;
            if (scrollY > 0) {
                mViewDataBinding.ivSearch.setEnabled(true);
                alpha = (float) scrollY / (float) 300;
            } else {
                mViewDataBinding.ivSearch.setEnabled(false);
            }
            mViewDataBinding.cl.setAlpha(alpha);
        });

        mViewDataBinding.refresh.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                loadData();
            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                pageInfo.reset();
                loadData();
            }
        });

        loadData();
    }

    @Override
    protected void initData() {
        super.initData();
        mViewModel.mBannerListMutable.observe(this, bannerEntities -> {
            if (mViewDataBinding.refresh.getState().isOpening) {
                mViewDataBinding.refresh.finishRefresh();
                mViewDataBinding.refresh.finishLoadMore();
            }

            if (bannerEntities != null && bannerEntities.size() > 0) {
                mViewDataBinding.banner.setAdapter(new HomeHeadBannerAdapter(bannerEntities));
                mViewDataBinding.banner.setIndicator(new CircleIndicator(getActivity()));
                mViewDataBinding.banner.setIndicatorGravity(IndicatorConfig.Direction.RIGHT);
                mViewDataBinding.banner.setIndicatorMargins(new IndicatorConfig.Margins(0, 0,
                        BannerConfig.INDICATOR_MARGIN, (int) BannerUtils.dp2px(40)));
            }
        });

        mViewModel.mArticleListMutable.observe(this, datasBeans -> {
            if (mViewDataBinding.refresh.getState().isOpening) {
                mViewDataBinding.refresh.finishRefresh();
            }
            if (isLoading)
                showContent();
            articleListAdapter.setList(datasBeans);
            pageInfo.nextPage();
            isLoading = false;
        });

        mViewModel.mArticleMutable.observe(this, articleEntity -> {
            if (mViewDataBinding.refresh.getState().isOpening) {
                mViewDataBinding.refresh.finishLoadMore();
            }
            if (null != articleEntity) {
                List<ArticleEntity.DatasBean> entityDatas = articleEntity.getDatas();
                if (null != entityDatas && entityDatas.size() > 0) {
                    articleListAdapter.addData(entityDatas);
                    pageInfo.nextPage();
                }
            }
        });

        articleListAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                ToastUtils.showShort("啦啦啦啦");
            }
        });
    }

    private void loadData() {
        if (pageInfo.isFirstPage()) {
            mViewModel.getBanner();
            mViewModel.getArticleMultiList(pageInfo.page);
        } else {
            mViewModel.getArticleList(pageInfo.page);
        }
    }

    @Override
    protected void onRetryBtnClick() {

    }
}
