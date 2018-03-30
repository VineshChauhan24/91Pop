package com.dante.ui.porn91forum.browse91porn;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.orhanobut.logger.Logger;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.dante.R;
import com.dante.custom.TastyToast;
import com.dante.data.model.Forum91PronItem;
import com.dante.data.model.HostJsScope;
import com.dante.data.model.Porn91ForumContent;
import com.dante.ui.MvpActivity;
import com.dante.ui.images.viewimage.PictureViewerActivity;
import com.dante.utils.AppCacheUtils;
import com.dante.utils.AppUtils;
import com.dante.utils.StringUtils;
import com.dante.utils.constants.Keys;

import java.util.ArrayList;
import java.util.Stack;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.pedant.SafeWebViewBridge.InjectedChromeClient;

/**
 * @author flymegoc
 */
public class Browse91PornActivity extends MvpActivity<Browse91View, Browse91Presenter> implements Browse91View, SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = Browse91PornActivity.class.getSimpleName();
    @Inject
    protected Browse91Presenter browse91Presenter;
    @BindView(R.id.webview)
    WebView mWebView;
    @BindView(R.id.swipe_layout)
    SwipeRefreshLayout swipeLayout;
    @BindView(R.id.fab_function)
    FloatingActionButton fabFunction;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    boolean isNightModel;
    private Forum91PronItem forum91PronItem;
    private ArrayList<String> imageList;
    private Stack<Long> historyIdStack;
    private HostJsScope.OnImageClick onImageClick = new HostJsScope.OnImageClick() {
        @Override
        public void onClick(String imageUrl) {
            Intent intent = new Intent(Browse91PornActivity.this, PictureViewerActivity.class);
            intent.putExtra(Keys.KEY_INTENT_PICTURE_VIEWER_CURRENT_IMAGE_POSITION, imageList.indexOf(imageUrl));
            intent.putStringArrayListExtra(Keys.KEY_INTENT_PICTURE_VIEWER_IMAGE_ARRAY_LIST, imageList);
            startActivityWithAnimotion(intent);
        }
    };

    public void initAppBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (null != toolbar) {
            setSupportActionBar(toolbar);
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            if (toolbar != null) {
                toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onBackPressed();
                    }
                });
                toolbar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mWebView.scrollTo(0, 0);
                    }
                });
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse91_porn);
        ButterKnife.bind(this);
        initAppBar();
        isNightModel = dataManager.isOpenNightMode();
        historyIdStack = new Stack<>();
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setAppCachePath(AppCacheUtils.getRxCacheDir(context).getAbsolutePath());
        mWebView.setWebChromeClient(
                new InjectedChromeClient("HostApp", HostJsScope.class)
        );
        mWebView.setBackgroundColor(0);
        mWebView.setBackgroundResource(0);
        mWebView.setVisibility(View.INVISIBLE);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.contains("tid=")) {
                    int starIndex = url.indexOf("tid=");
                    String tidStr = StringUtils.subString(url, starIndex + 4, starIndex + 10);
                    if (!TextUtils.isEmpty(tidStr) && TextUtils.isDigitsOnly(tidStr)) {
                        Long id = Long.parseLong(tidStr);
                        presenter.loadContent(id, isNightModel);
                        historyIdStack.push(id);
                    } else {
                        Logger.t(TAG).d(tidStr);
                        showMessage("暂不支持直接打开此链接", TastyToast.INFO);
                    }
                }
                return true;
            }
        });
        AppUtils.setColorSchemeColors(context, swipeLayout);
        forum91PronItem = (Forum91PronItem) getIntent().getSerializableExtra(Keys.KEY_INTENT_BROWSE_FORUM_91_PORN_ITEM);
        presenter.loadContent(forum91PronItem.getTid(), isNightModel);
        historyIdStack.push(forum91PronItem.getTid());
        imageList = new ArrayList<>();
        boolean needShowTip = dataManager.isViewPorn91ForumContentShowTip();
        if (needShowTip) {
//            showTipDialog();
        }

        fabFunction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOpenNewForum();
            }
        });
    }

    private void showOpenNewForum() {
        final QMUIDialog.EditTextDialogBuilder builder = new QMUIDialog.EditTextDialogBuilder(this);
        builder.setTitle("打开新帖子");
        builder.setPlaceholder("请输入帖子Tid");
        builder.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.addAction("打开", new QMUIDialogAction.ActionListener() {
            @Override
            public void onClick(QMUIDialog dialog, int index) {
                String tidStr = builder.getEditText().getText().toString().trim();
                if (!TextUtils.isEmpty(tidStr) && TextUtils.isDigitsOnly(tidStr) && tidStr.length() <= 6) {
                    Long id = Long.parseLong(tidStr);
                    presenter.loadContent(id, isNightModel);
                    historyIdStack.push(id);
                    dialog.dismiss();
                } else {
                    showMessage("请输入正确的帖子tid", TastyToast.INFO);
                }
            }
        });
        builder.addAction("返回", new QMUIDialogAction.ActionListener() {
            @Override
            public void onClick(QMUIDialog dialog, int index) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void showTipDialog() {
        QMUIDialog.MessageDialogBuilder builder = new QMUIDialog.MessageDialogBuilder(this);
        builder.setTitle("温馨提示");
        builder.setMessage("1. 如果你看不到网页内容或者报错了，说明当前页面结构还不支持解析\n" +
                "2. 在你网速慢或者图片较大或者服务器响应慢的情况下，你会只看到少少的几行文字，这是正常的，你可以多刷新几次试试\n" +
                "3. 你可以点击某一张图进入浏览图片模式\n" +
                "4. 目前不支持查看评论及部分网页未适配屏幕宽度");
        builder.addAction("我知道了", new QMUIDialogAction.ActionListener() {
            @Override
            public void onClick(QMUIDialog dialog, int index) {
                dataManager.setViewPorn91ForumContentShowTip(false);
                dialog.dismiss();
            }
        });
        builder.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        HostJsScope.setOnImageClick(onImageClick);
    }

    @Override
    protected void onStop() {
        super.onStop();
        HostJsScope.removeOnImageClick();
    }

    @NonNull
    @Override
    public Browse91Presenter createPresenter() {
        getActivityComponent().inject(this);
        return browse91Presenter;
    }

    @Override
    public void showLoading(boolean pullToRefresh) {
        swipeLayout.setRefreshing(pullToRefresh);
    }

    @Override
    public void showContent() {
        swipeLayout.setRefreshing(false);
    }

    @Override
    public void showMessage(String msg, int type) {
        super.showMessage(msg, type);
    }

    @Override
    public void showError(String message) {
        swipeLayout.setRefreshing(false);
        showMessage(message, TastyToast.ERROR);
    }

    @Override
    public void loadContentSuccess(Porn91ForumContent porn91ForumContent) {
        imageList.clear();
        imageList.addAll(porn91ForumContent.getImageList());
        //mWebView.loadUrl("about:blank");;
        toolbar.setTitle(forum91PronItem.getTitle());
        String html = AppUtils.buildHtml(AppUtils.buildTitle(forum91PronItem.getTitle(), forum91PronItem.getAuthor(), forum91PronItem.getAuthorPublishTime()) + porn91ForumContent.getContent(), isNightModel);
        mWebView.loadDataWithBaseURL("", html, "text/html", "utf-8", null);
        //mWebView.loadData(StringEscapeUtils.escapeHtml4(porn91ForumContent.getContent()), "text/html", "UTF-8");
        mWebView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onRefresh() {
        presenter.loadContent(forum91PronItem.getTid(), isNightModel);
    }

    @Override
    protected void onDestroy() {
        if (mWebView != null) {

            // 如果先调用destroy()方法，则会命中if (isDestroyed()) return;这一行代码，需要先onDetachedFromWindow()，再
            // destory()
            ViewParent parent = mWebView.getParent();
            if (parent != null) {
                ((ViewGroup) parent).removeView(mWebView);
            }

            mWebView.stopLoading();
            // 退出时调用此方法，移除绑定的服务，否则某些特定系统会报错
            mWebView.getSettings().setJavaScriptEnabled(false);
            mWebView.clearHistory();
            mWebView.loadUrl("about:blank");
            mWebView.clearView();
            mWebView.removeAllViews();
            mWebView.destroy();

        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (!historyIdStack.empty()) {
            historyIdStack.pop();
        }
        if (!historyIdStack.empty()) {
            presenter.loadContent(historyIdStack.peek(), isNightModel);
            return;
        }
        super.onBackPressed();
    }
}
