package com.yuanfen.main.activity;

import android.content.Intent;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.alibaba.fastjson.JSON;
import com.yuanfen.common.Constants;
import com.yuanfen.common.activity.AbsActivity;
import com.yuanfen.common.adapter.RefreshAdapter;
import com.yuanfen.common.custom.CommonRefreshView;
import com.yuanfen.common.http.HttpCallback;
import com.yuanfen.common.interfaces.OnItemClickListener;
import com.yuanfen.common.utils.WordUtil;
import com.yuanfen.main.R;
import com.yuanfen.main.adapter.ActiveChooseTopicAdapter;
import com.yuanfen.main.bean.ActiveTopicBean;
import com.yuanfen.main.http.MainHttpConsts;
import com.yuanfen.main.http.MainHttpUtil;

import java.util.Arrays;
import java.util.List;

/**
 * 动态 选择话题
 */
public class ActiveChooseTopicActivity extends AbsActivity implements OnItemClickListener<ActiveTopicBean> {

    private ActiveChooseTopicAdapter mAdapter;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_active_all_topic;
    }

    @Override
    protected void main() {
        setTitle(WordUtil.getString(R.string.active_topic_04));
        CommonRefreshView refreshView = findViewById(R.id.refreshView);
        refreshView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        refreshView.setDataHelper(new CommonRefreshView.DataHelper<ActiveTopicBean>() {
            @Override
            public RefreshAdapter<ActiveTopicBean> getAdapter() {
                if (mAdapter == null) {
                    mAdapter = new ActiveChooseTopicAdapter(mContext);
                    mAdapter.setOnItemClickListener(ActiveChooseTopicActivity.this);
                }
                return mAdapter;
            }

            @Override
            public void loadData(int p, HttpCallback callback) {
                MainHttpUtil.getActiveAllTopic(p, callback);
            }

            @Override
            public List<ActiveTopicBean> processData(String[] info) {
                return JSON.parseArray(Arrays.toString(info), ActiveTopicBean.class);
            }

            @Override
            public void onRefreshSuccess(List<ActiveTopicBean> list, int listCount) {

            }

            @Override
            public void onRefreshFailure() {

            }

            @Override
            public void onLoadMoreSuccess(List<ActiveTopicBean> loadItemList, int loadItemCount) {

            }

            @Override
            public void onLoadMoreFailure() {

            }
        });
        refreshView.initData();
    }

    @Override
    protected void onDestroy() {
        MainHttpUtil.cancel(MainHttpConsts.GET_ACTIVE_ALL_TOPIC);
        super.onDestroy();
    }

    @Override
    public void onItemClick(ActiveTopicBean bean, int position) {
        Intent intent = new Intent();
        intent.putExtra(Constants.CLASS_ID, bean.getId());
        intent.putExtra(Constants.CLASS_NAME, bean.getName());
        setResult(RESULT_OK, intent);
        finish();
    }
}

