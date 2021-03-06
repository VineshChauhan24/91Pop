package com.dante.ui.porn91video.author;

import com.dante.data.model.UnLimit91PornItem;
import com.dante.ui.BaseView;

import java.util.List;

/**
 * @author flymegoc
 * @date 2018/1/8
 */

public interface AuthorView extends BaseView {
    void loadMoreDataComplete();

    void loadMoreFailed();

    void noMoreData();

    void setMoreData(List<UnLimit91PornItem> unLimit91PornItemList);

    void setData(List<UnLimit91PornItem> data);
}
