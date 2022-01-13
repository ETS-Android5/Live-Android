package com.yuanfen.main.views;

import android.content.Context;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSON;
import com.yuanfen.common.CommonAppConfig;
import com.yuanfen.common.adapter.RefreshAdapter;
import com.yuanfen.common.custom.CommonRefreshView;
import com.yuanfen.common.event.FollowEvent;
import com.yuanfen.common.http.HttpCallback;
import com.yuanfen.live.views.AbsUserHomeViewHolder;
import com.yuanfen.main.R;
import com.yuanfen.main.adapter.ActiveAdapter;
import com.yuanfen.main.bean.ActiveBean;
import com.yuanfen.main.event.ActiveCommentEvent;
import com.yuanfen.main.event.ActiveDeleteEvent;
import com.yuanfen.main.event.ActiveLikeEvent;
import com.yuanfen.main.http.MainHttpConsts;
import com.yuanfen.main.http.MainHttpUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Arrays;
import java.util.List;

public class ActiveHomeViewHolder extends AbsUserHomeViewHolder {

    private CommonRefreshView mRefreshView;
    private ActiveAdapter mAdapter;
    private String mToUid;
    private ActionListener mActionListener;

    public ActiveHomeViewHolder(Context context, ViewGroup parentView, String toUid) {
        super(context, parentView, toUid);
    }

    @Override
    protected void processArguments(Object... args) {
        if (args.length > 0) {
            mToUid = (String) args[0];
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_active_home;
    }

    @Override
    public void init() {
        mRefreshView = findViewById(R.id.refreshView);
        if(!TextUtils.isEmpty(mToUid)&&mToUid.equals(CommonAppConfig.getInstance().getUid())){
            mRefreshView.setEmptyLayoutId(R.layout.view_no_data_active_home_3);
        }else{
            mRefreshView.setEmptyLayoutId(R.layout.view_no_data_active_home_2);
        }
        mRefreshView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mRefreshView.setDataHelper(new CommonRefreshView.DataHelper<ActiveBean>() {
            @Override
            public RefreshAdapter<ActiveBean> getAdapter() {
                if (mAdapter == null) {
                    mAdapter = new ActiveAdapter(mContext);
                }
                return mAdapter;
            }

            @Override
            public void loadData(int p, HttpCallback callback) {
                MainHttpUtil.getActiveHome(mToUid, p, callback);
            }

            @Override
            public List<ActiveBean> processData(String[] info) {
                return JSON.parseArray(Arrays.toString(info), ActiveBean.class);
            }

            @Override
            public void onRefreshSuccess(List<ActiveBean> list, int listCount) {

            }

            @Override
            public void onRefreshFailure() {

            }

            @Override
            public void onLoadMoreSuccess(List<ActiveBean> loadItemList, int loadItemCount) {

            }

            @Override
            public void onLoadMoreFailure() {

            }
        });
        EventBus.getDefault().register(this);
    }

    @Override
    public void loadData() {
        if (isFirstLoadData()) {
            mRefreshView.initData();
        }
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        if (mAdapter != null) {
            mAdapter.release();
        }
        mActionListener = null;
        MainHttpUtil.cancel(MainHttpConsts.GET_HOME_ACTIVE);
        super.onDestroy();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFollowEvent(FollowEvent e) {
        if (mAdapter != null && e != null) {
            mAdapter.onFollowChanged(e.getToUid(), e.getIsAttention());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onActiveDeleted(ActiveDeleteEvent e) {
        if (mAdapter != null && e != null) {
            mAdapter.onActiveDeleted(e.getActiveId());
            if (mActionListener != null) {
                mActionListener.onVideoDelete(1);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onActiveCommentEvent(ActiveCommentEvent e) {
        if (mAdapter != null && e != null) {
            mAdapter.onCommentNumChanged(e.getActiveId(), e.getCommentNum());
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onActiveLikeEvent(ActiveLikeEvent e) {
        if (mAdapter != null && e != null) {
            mAdapter.onLikeChanged(e.getFrom(), e.getActiveId(), e.getLikeNum(), e.getIsLike());
        }
    }


    public interface ActionListener {
        void onVideoDelete(int deleteCount);
    }

    public void setActionListener(ActionListener actionListener) {
        mActionListener = actionListener;
    }


}
