package com.dante.ui.download;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;

import com.orhanobut.logger.Logger;
import com.dante.R;
import com.dante.adapter.DownloadFragmentAdapter;
import com.dante.ui.BaseAppCompatActivity;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author flymegoc
 */
public class DownloadActivity extends BaseAppCompatActivity {

    private static final String TAG = DownloadActivity.class.getSimpleName();
    @Inject
    protected DownloadingFragment downloadingFragment;
    @Inject
    protected FinishedFragment finishedFragment;
    @BindView(R.id.download_viewpager)
    ViewPager downloadViewpager;
    @BindView(R.id.download_tab)
    TabLayout downloadTab;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @Inject
    List<Fragment> fragmentList;

    @Inject
    DownloadFragmentAdapter downloadAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        ButterKnife.bind(this);
        getActivityComponent().inject(this);
        initToolBar(toolbar);

        fragmentList.add(downloadingFragment);
        fragmentList.add(finishedFragment);

        downloadAdapter.setData(fragmentList);

        downloadViewpager.setAdapter(downloadAdapter);
        downloadTab.setupWithViewPager(downloadViewpager);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logger.t(TAG).d("onDestroy()");
    }
}
