package com.yuanfen.live.views;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import androidx.recyclerview.widget.LinearLayoutManager;
import  androidx.recyclerview.widget.RecyclerView;;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.opensource.svgaplayer.SVGAImageView;
import com.yuanfen.common.CommonAppConfig;
import com.yuanfen.common.Constants;
import com.yuanfen.common.bean.GoodsBean;
import com.yuanfen.common.bean.LevelBean;
import com.yuanfen.common.bean.UserBean;
import com.yuanfen.common.glide.ImgLoader;
import com.yuanfen.common.http.CommonHttpConsts;
import com.yuanfen.common.http.CommonHttpUtil;
import com.yuanfen.common.http.HttpCallback;
import com.yuanfen.common.interfaces.CommonCallback;
import com.yuanfen.common.interfaces.OnItemClickListener;
import com.yuanfen.common.utils.DialogUitl;
import com.yuanfen.common.utils.DpUtil;
import com.yuanfen.common.utils.RouteUtil;
import com.yuanfen.common.utils.ScreenDimenUtil;
import com.yuanfen.common.utils.StringUtil;
import com.yuanfen.common.utils.WordUtil;
import com.yuanfen.common.views.AbsViewHolder;
import com.yuanfen.live.R;
import com.yuanfen.live.activity.LiveActivity;
import com.yuanfen.live.activity.LiveAnchorActivity;
import com.yuanfen.live.activity.LiveAudienceActivity;
import com.yuanfen.live.adapter.LiveChatAdapter;
import com.yuanfen.live.adapter.LiveUserAdapter;
import com.yuanfen.live.bean.GlobalGiftBean;
import com.yuanfen.live.bean.LiveBuyGuardMsgBean;
import com.yuanfen.live.bean.LiveChatBean;
import com.yuanfen.live.bean.LiveDanMuBean;
import com.yuanfen.live.bean.LiveEnterRoomBean;
import com.yuanfen.live.bean.LiveGiftPrizePoolWinBean;
import com.yuanfen.live.bean.LiveLuckGiftWinBean;
import com.yuanfen.live.bean.LiveReceiveGiftBean;
import com.yuanfen.live.bean.LiveUserGiftBean;
import com.yuanfen.live.custom.LiveLightView;
import com.yuanfen.live.custom.TopGradual;
import com.yuanfen.live.dialog.LiveUserDialogFragment;
import com.yuanfen.live.http.LiveHttpConsts;
import com.yuanfen.live.http.LiveHttpUtil;
import com.yuanfen.live.presenter.LiveDanmuPresenter;
import com.yuanfen.live.presenter.LiveEnterRoomAnimPresenter;
import com.yuanfen.live.presenter.LiveGiftAnimPresenter;
import com.yuanfen.live.presenter.LiveLightAnimPresenter;

import java.lang.ref.WeakReference;
import java.util.List;

import pl.droidsonroids.gif.GifImageView;

/**
 * Created by cxf on 2018/10/9.
 * 直播间公共逻辑
 */

public class LiveRoomViewHolder extends AbsViewHolder implements View.OnClickListener {

    private int mOffsetY;
    private ViewGroup mRoot;
    private ImageView mAvatar;
    private ImageView mLevelAnchor;
    private TextView mName;
    private TextView mID;
    private View mBtnFollow;
    private TextView mVotesName;//映票名称
    private TextView mVotes;
    private TextView mGuardNum;//守护人数
    private RecyclerView mUserRecyclerView;
    private RecyclerView mChatRecyclerView;
    private LiveUserAdapter mLiveUserAdapter;
    private LiveChatAdapter mLiveChatAdapter;
    private View mBtnRedPack;
    private String mLiveUid;
    private String mStream;
    private LiveLightAnimPresenter mLightAnimPresenter;
    private LiveEnterRoomAnimPresenter mLiveEnterRoomAnimPresenter;
    private LiveDanmuPresenter mLiveDanmuPresenter;
    private LiveGiftAnimPresenter mLiveGiftAnimPresenter;
    private LiveRoomHandler mLiveRoomHandler;
    private HttpCallback mRefreshUserListCallback;
    private HttpCallback mTimeChargeCallback;
    protected int mUserListInterval;//用户列表刷新时间的间隔
    private GifImageView mGifImageView;
    private SVGAImageView mSVGAImageView;
    private ViewGroup mLiveGiftPrizePoolContainer;
    private TextView mLiveTimeTextView;//主播的直播时长
    private long mAnchorLiveTime;//主播直播时间
    private Handler mAnchorTimeHandler;
//    private TextView mPrizePoolLevel;//奖池等级
//    private View mPrizePoolGuang;
    private View mGroupGoods;
    private ImageView mGoodsThumb;
    private TextView mGoodsName;
    private TextView mGoodsPrice;
    private GoodsBean mShowGoodsBean;//直播间当前展示的商品
    private LiveRoomBtnViewHolder mBtnViewHolder;

    public LiveRoomViewHolder(Context context, ViewGroup parentView, GifImageView gifImageView, SVGAImageView svgaImageView, ViewGroup liveGiftPrizePoolContainer) {
        super(context, parentView);
        mGifImageView = gifImageView;
        mSVGAImageView = svgaImageView;
        mLiveGiftPrizePoolContainer = liveGiftPrizePoolContainer;
        View group_1 = findViewById(R.id.group_1);
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) group_1.getLayoutParams();
        params.topMargin = ScreenDimenUtil.getInstance().getStatusBarHeight() + DpUtil.dp2px(5);
        group_1.requestLayout();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_live_room;
    }

    @Override
    public void init() {
        mRoot = (ViewGroup) findViewById(R.id.root);
        mAvatar = (ImageView) findViewById(R.id.avatar);
        mLevelAnchor = (ImageView) findViewById(R.id.level_anchor);
        mName = (TextView) findViewById(R.id.name);
        mID = (TextView) findViewById(R.id.id_val);
        mBtnFollow = findViewById(R.id.btn_follow);
        mVotesName = (TextView) findViewById(R.id.votes_name);
        mVotes = (TextView) findViewById(R.id.votes);
        mGuardNum = (TextView) findViewById(R.id.guard_num);
        //用户头像列表
        mUserRecyclerView = (RecyclerView) findViewById(R.id.user_recyclerView);
        mUserRecyclerView.setHasFixedSize(true);
        mUserRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        mLiveUserAdapter = new LiveUserAdapter(mContext);
        mLiveUserAdapter.setOnItemClickListener(new OnItemClickListener<UserBean>() {
            @Override
            public void onItemClick(UserBean bean, int position) {
                showUserDialog(bean.getId());
            }
        });
        mUserRecyclerView.setAdapter(mLiveUserAdapter);
        //聊天栏
        mChatRecyclerView = (RecyclerView) findViewById(R.id.chat_recyclerView);
        mChatRecyclerView.setHasFixedSize(true);
        mChatRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mChatRecyclerView.addItemDecoration(new TopGradual());
        mLiveChatAdapter = new LiveChatAdapter(mContext);
        mLiveChatAdapter.setOnItemClickListener(new OnItemClickListener<LiveChatBean>() {
            @Override
            public void onItemClick(LiveChatBean bean, int position) {
                showUserDialog(bean.getId());
            }
        });
        mChatRecyclerView.setAdapter(mLiveChatAdapter);
        mVotesName.setText(CommonAppConfig.getInstance().getVotesName());

        mGroupGoods = findViewById(R.id.group_goods);
        mGoodsThumb = findViewById(R.id.goods_thumb);
        mGoodsName = findViewById(R.id.goods_name);
        mGoodsPrice = findViewById(R.id.goods_price);
        findViewById(R.id.btn_goods_detail).setOnClickListener(this);

        mBtnFollow.setOnClickListener(this);
        mAvatar.setOnClickListener(this);
        findViewById(R.id.btn_votes).setOnClickListener(this);
        findViewById(R.id.btn_guard).setOnClickListener(this);
        mBtnRedPack = findViewById(R.id.btn_red_pack);
        mBtnRedPack.setOnClickListener(this);
        if (mContext instanceof LiveAudienceActivity) {
            if (!((LiveActivity) mContext).isVoiceChatRoom()) {
                mRoot.setOnClickListener(this);
            }
        } else {
            mLiveTimeTextView = (TextView) findViewById(R.id.live_time);
            mLiveTimeTextView.setVisibility(View.VISIBLE);
        }
        mLightAnimPresenter = new LiveLightAnimPresenter(mContext, mParentView);
        mLiveEnterRoomAnimPresenter = new LiveEnterRoomAnimPresenter(mContext, mContentView);
        mLiveRoomHandler = new LiveRoomHandler(this);
        mRefreshUserListCallback = new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0) {
                    if (mLiveUserAdapter != null) {
                        JSONObject obj = JSON.parseObject(info[0]);
                        List<LiveUserGiftBean> list = JSON.parseArray(obj.getString("userlist"), LiveUserGiftBean.class);
                        mLiveUserAdapter.refreshList(list);
                    }
                }
            }
        };
        mTimeChargeCallback = new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (mContext instanceof LiveAudienceActivity) {
                    final LiveAudienceActivity liveAudienceActivity = (LiveAudienceActivity) mContext;
                    if (code == 0) {
                        liveAudienceActivity.roomChargeUpdateVotes();
                    } else {
                        if (mLiveRoomHandler != null) {
                            mLiveRoomHandler.removeMessages(LiveRoomHandler.WHAT_TIME_CHARGE);
                        }
                        liveAudienceActivity.pausePlay();
                        if (code == 1008) {//余额不足
                            liveAudienceActivity.setCoinNotEnough(true);
                            DialogUitl.showSimpleDialog(mContext, WordUtil.getString(R.string.live_coin_not_enough), false,
                                    new DialogUitl.SimpleCallback2() {
                                        @Override
                                        public void onConfirmClick(Dialog dialog, String content) {
                                            RouteUtil.forwardMyCoin(mContext);
                                        }

                                        @Override
                                        public void onCancelClick() {
                                            liveAudienceActivity.exitLiveRoom();
                                        }
                                    });
                        }
                    }
                }
            }
        };


        if (((LiveActivity) mContext).isVoiceChatRoom()) {
            View btnCloseVoice = findViewById(R.id.btn_close_voice);
            btnCloseVoice.setVisibility(View.VISIBLE);
            btnCloseVoice.setOnClickListener(this);
        }
    }


    /**
     * 显示直播间左上角按钮
     *
     * @param showPan        是否显示转盘
     * @param prizePoolLevel 奖池等级
     */
    public void showBtn(boolean showPan, int prizePoolLevel) {
        if (mBtnViewHolder == null) {
            mBtnViewHolder = new LiveRoomBtnViewHolder(mContext, (ViewGroup) findViewById(R.id.btn_container), showPan, prizePoolLevel);
            mBtnViewHolder.addToParent();
            mBtnViewHolder.subscribeActivityLifeCycle();
//            mPrizePoolLevel = mBtnViewHolder.getPrizePoolLevel();
//            mPrizePoolGuang = mBtnViewHolder.getPrizePoolGuang();
//            if (mLiveGiftAnimPresenter != null) {
//                mLiveGiftAnimPresenter.setPrizePoolView(mPrizePoolLevel, mPrizePoolGuang);
//            }
        }
        mBtnViewHolder.showPrizeLevel(prizePoolLevel);
    }


    /**
     * 显示主播头像
     */
    public void setAvatar(String url) {
        if (mAvatar != null) {
            ImgLoader.displayAvatar(mContext, url, mAvatar);
        }
    }

    /**
     * 显示主播等级
     */
    public void setAnchorLevel(int anchorLevel) {
        if (mLevelAnchor != null) {
            LevelBean levelBean = CommonAppConfig.getInstance().getAnchorLevel(anchorLevel);
            if (levelBean != null) {
                ImgLoader.display(mContext, levelBean.getThumbIcon(), mLevelAnchor);
            }
        }
    }

    /**
     * 显示用户名
     */
    public void setName(String name) {
        if (mName != null) {
            mName.setText(name);
        }
    }

    /**
     * 显示房间号
     */
    public void setRoomNum(String roomNum) {
        if (mID != null) {
            mID.setText(roomNum);
        }
    }

    /**
     * 显示直播标题
     */
    public void setTitle(String title) {
        if (!TextUtils.isEmpty(title)) {
            if (mLiveGiftAnimPresenter == null) {
                mLiveGiftAnimPresenter = new LiveGiftAnimPresenter(mContext, mContentView, mGifImageView, mSVGAImageView, mLiveGiftPrizePoolContainer);
            }
            mLiveGiftAnimPresenter.showLiveTitleAnim(title);
        }
    }


    /**
     * 显示是否关注
     */
    public void setAttention(int attention) {
        if (mBtnFollow != null) {
            if (attention == 0) {
                if (mBtnFollow.getVisibility() != View.VISIBLE) {
                    mBtnFollow.setVisibility(View.VISIBLE);
                }
            } else {
                if (mBtnFollow.getVisibility() == View.VISIBLE) {
                    mBtnFollow.setVisibility(View.GONE);
                }
            }
        }
    }

    /**
     * 显示刷新直播间用户列表
     */
    public void setUserList(List<LiveUserGiftBean> list) {
        if (mLiveUserAdapter != null) {
            mLiveUserAdapter.refreshList(list);
        }
    }

    /**
     * 显示主播映票数
     */
    public void setVotes(String votes) {
        if (mVotes != null) {
            mVotes.setText(votes);
        }
    }

    /**
     * 显示主播守护人数
     */
    public void setGuardNum(int guardNum) {
        if (mGuardNum != null) {
            if (guardNum > 0) {
                mGuardNum.setText(guardNum + WordUtil.getString(R.string.ren));
            } else {
                mGuardNum.setText(R.string.main_list_no_data);
            }
        }
    }

    public void setLiveInfo(String liveUid, String stream, int userListInterval) {
        mLiveUid = liveUid;
        mStream = stream;
        mUserListInterval = userListInterval;
    }

    /**
     * 守护信息发生变化
     */
    public void onGuardInfoChanged(LiveBuyGuardMsgBean bean) {
        setGuardNum(bean.getGuardNum());
        setVotes(bean.getVotes());
        if (mLiveUserAdapter != null) {
            mLiveUserAdapter.onGuardChanged(bean.getUid(), bean.getGuardType());
        }
    }

    /**
     * 设置红包按钮是否可见
     */
    public void setRedPackBtnVisible(boolean visible) {
        if (mBtnRedPack != null) {
            if (visible) {
                if (mBtnRedPack.getVisibility() != View.VISIBLE) {
                    mBtnRedPack.setVisibility(View.VISIBLE);
                }
            } else {
                if (mBtnRedPack.getVisibility() == View.VISIBLE) {
                    mBtnRedPack.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.root) {
            light();
        }
        if (!canClick()) {
            return;
        }
        if (i == R.id.avatar) {
            showAnchorUserDialog();

        } else if (i == R.id.btn_follow) {
            follow();

        } else if (i == R.id.btn_votes) {
            openContributeWindow();

        } else if (i == R.id.btn_guard) {
            ((LiveActivity) mContext).openGuardListWindow();

        } else if (i == R.id.btn_red_pack) {
            ((LiveActivity) mContext).openRedPackListWindow();

        } else if (i == R.id.btn_goods_detail) {
            forwardGoods();
        } else if (i == R.id.btn_close_voice) {
            if (mContext instanceof LiveAnchorActivity) {
                ((LiveAnchorActivity) mContext).closeLive();
            } else if (mContext instanceof LiveAudienceActivity) {
                ((LiveAudienceActivity) mContext).onBackPressed();
            }
        }
    }

    /**
     * 查看直播间当前商品
     */
    private void forwardGoods() {
        if (mContext != null && mContext instanceof LiveAudienceActivity) {
            ((LiveAudienceActivity) mContext).liveGoodsFloat();
        }
        if (mShowGoodsBean != null) {
            if (mShowGoodsBean.getType() == 1) {
                RouteUtil.forwardGoodsDetailOutSide(mContext, mShowGoodsBean.getId(), false);
            } else {
                RouteUtil.forwardGoodsDetail(mContext, mShowGoodsBean.getId(), false, mLiveUid);
            }
        }
    }

    /**
     * 关注主播
     */
    private void follow() {
        if (TextUtils.isEmpty(mLiveUid)) {
            return;
        }
        CommonHttpUtil.setAttention(mLiveUid, new CommonCallback<Integer>() {
            @Override
            public void callback(Integer isAttention) {
                if (isAttention == 1) {
                    ((LiveActivity) mContext).sendSystemMessage(
                            CommonAppConfig.getInstance().getUserBean().getUserNiceName() + WordUtil.getString(R.string.live_follow_anchor));
                }
            }
        });
    }

    /**
     * 用户进入房间，用户列表添加该用户
     */
    public void insertUser(LiveUserGiftBean bean) {
        if (mLiveUserAdapter != null) {
            mLiveUserAdapter.insertItem(bean);
        }
    }

    /**
     * 用户进入房间，添加僵尸粉
     */
    public void insertUser(List<LiveUserGiftBean> list) {
        if (mLiveUserAdapter != null) {
            mLiveUserAdapter.insertList(list);
        }
    }

    /**
     * 用户离开房间，用户列表删除该用户
     */
    public void removeUser(String uid) {
        if (mLiveUserAdapter != null) {
            mLiveUserAdapter.removeItem(uid);
        }
    }

    /**
     * 刷新用户列表
     */
    private void refreshUserList() {
        if (!TextUtils.isEmpty(mLiveUid) && mRefreshUserListCallback != null && mLiveUserAdapter != null) {
            LiveHttpUtil.cancel(LiveHttpConsts.GET_USER_LIST);
            LiveHttpUtil.getUserList(mLiveUid, mStream, mRefreshUserListCallback);
            startRefreshUserList();
        }
    }

    /**
     * 开始刷新用户列表
     */
    public void startRefreshUserList() {
        if (mLiveRoomHandler != null) {
            mLiveRoomHandler.sendEmptyMessageAtTime(LiveRoomHandler.WHAT_REFRESH_USER_LIST, getNextTime(mUserListInterval > 0 ? mUserListInterval : 60000));
        }
    }

    /**
     * 请求计时收费的扣费接口
     */
    private void requestTimeCharge() {
        if (!TextUtils.isEmpty(mLiveUid) && mTimeChargeCallback != null) {
            LiveHttpUtil.cancel(LiveHttpConsts.TIME_CHARGE);
            LiveHttpUtil.timeCharge(mLiveUid, mStream, mTimeChargeCallback);
            startRequestTimeCharge();
        }
    }

    /**
     * 开始请求计时收费的扣费接口
     */
    public void startRequestTimeCharge() {
        if (mLiveRoomHandler != null) {
            mLiveRoomHandler.sendEmptyMessageAtTime(LiveRoomHandler.WHAT_TIME_CHARGE, getNextTime(60000));
        }
    }


    /**
     * 添加聊天消息到聊天栏
     */
    public void insertChat(LiveChatBean bean) {
        if (mLiveChatAdapter != null) {
            mLiveChatAdapter.insertItem(bean);
        }
    }

    /**
     * 播放飘心动画
     */
    public void playLightAnim() {
        if (mLightAnimPresenter != null) {
            mLightAnimPresenter.play();
        }
    }

    /**
     * 点亮
     */
    private void light() {
        ((LiveAudienceActivity) mContext).light();
    }

    public void setOffsetY(int offsetY) {
        LiveLightView.sOffsetY = offsetY;
        mOffsetY = offsetY;
    }


    /**
     * 键盘高度变化
     */
    public void onKeyBoardChanged(int keyBoardHeight) {
        if (mRoot != null) {
            if (keyBoardHeight == 0 || keyBoardHeight < 200) {
                mRoot.setTranslationY(0);
                return;
            }
            if (mOffsetY == 0) {
                mRoot.setTranslationY(-keyBoardHeight);
                return;
            }
            if (mOffsetY > 0 && mOffsetY < keyBoardHeight) {
                mRoot.setTranslationY(mOffsetY - keyBoardHeight);
            }
        }
    }

    /**
     * 聊天栏滚到最底部
     */
    public void chatScrollToBottom() {
        if (mLiveChatAdapter != null) {
            mLiveChatAdapter.scrollToBottom();
        }
    }

    /**
     * 用户进入房间 金光一闪,坐骑动画
     */
    public void onEnterRoom(LiveEnterRoomBean bean) {
        if (bean == null) {
            return;
        }
        if (mLiveEnterRoomAnimPresenter != null) {
            mLiveEnterRoomAnimPresenter.enterRoom(bean);
        }
    }

    /**
     * 显示弹幕
     */
    public void showDanmu(LiveDanMuBean bean) {
        if (mVotes != null) {
            mVotes.setText(bean.getVotes());
        }
        if (mLiveDanmuPresenter == null) {
            mLiveDanmuPresenter = new LiveDanmuPresenter(mContext, mParentView);
        }
        mLiveDanmuPresenter.showDanmu(bean);
    }

    /**
     * 显示主播的个人资料弹窗
     */
    private void showAnchorUserDialog() {
        if (TextUtils.isEmpty(mLiveUid)) {
            return;
        }
        showUserDialog(mLiveUid);
    }

    /**
     * 显示个人资料弹窗
     */
    public void showUserDialog(String toUid) {
        if (!TextUtils.isEmpty(mLiveUid) && !TextUtils.isEmpty(toUid)) {
            LiveUserDialogFragment fragment = new LiveUserDialogFragment();
            fragment.setLifeCycleListener((LiveActivity) mContext);
            Bundle bundle = new Bundle();
            bundle.putString(Constants.LIVE_UID, mLiveUid);
            bundle.putString(Constants.STREAM, mStream);
            bundle.putString(Constants.TO_UID, toUid);
            fragment.setArguments(bundle);
            fragment.show(((LiveActivity) mContext).getSupportFragmentManager(), "LiveUserDialogFragment");
        }
    }

    /**
     * 直播间贡献榜窗口
     */
    private void openContributeWindow() {
        ((LiveActivity) mContext).openContributeWindow();
    }


    /**
     * 显示礼物动画
     */
    public void showGiftMessage(LiveReceiveGiftBean bean) {
        mVotes.setText(bean.getVotes());
        if (mLiveGiftAnimPresenter == null) {
            mLiveGiftAnimPresenter = new LiveGiftAnimPresenter(mContext, mContentView, mGifImageView, mSVGAImageView, mLiveGiftPrizePoolContainer);
        }
        mLiveGiftAnimPresenter.showGiftAnim(bean);
    }

    /**
     * 增加主播映票数
     *
     * @param deltaVal 增加的映票数量
     */
    public void updateVotes(String deltaVal) {
        if (mVotes == null) {
            return;
        }
        String votesVal = mVotes.getText().toString().trim();
        if (TextUtils.isEmpty(votesVal)) {
            return;
        }
        try {
            double votes = Double.parseDouble(votesVal);
            double addVotes = Double.parseDouble(deltaVal);
            votes += addVotes;
            mVotes.setText(StringUtil.format(votes));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ViewGroup getInnerContainer() {
        return (ViewGroup) findViewById(R.id.inner_container);
    }


    /**
     * 主播显示直播时间
     */
    public void startAnchorLiveTime() {
        if (mAnchorTimeHandler == null) {
            mAnchorTimeHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    mAnchorLiveTime += 1000;
                    if (mLiveTimeTextView != null) {
                        mLiveTimeTextView.setText(StringUtil.getDurationText(mAnchorLiveTime));
                    }
                    if (mAnchorTimeHandler != null) {
                        long now = SystemClock.uptimeMillis();
                        long next = now + (1000 - now % 1000);
                        mAnchorTimeHandler.sendEmptyMessageAtTime(0, next);
                    }
                }
            };
        }
        long now = SystemClock.uptimeMillis();
        long next = now + (1000 - now % 1000);
        mAnchorTimeHandler.sendEmptyMessageAtTime(0, next);
    }




    /**
     * 主播开始飘心
     */
    public void startAnchorLight() {
        if (mLiveRoomHandler != null) {
            mLiveRoomHandler.removeMessages(LiveRoomHandler.WHAT_ANCHOR_LIGHT);
            mLiveRoomHandler.sendEmptyMessageAtTime(LiveRoomHandler.WHAT_ANCHOR_LIGHT, getNextTime(500));
        }
    }


    /**
     * 主播checkLive
     */
    public void startAnchorCheckLive() {
        if (mLiveRoomHandler != null) {
            mLiveRoomHandler.removeMessages(LiveRoomHandler.WHAT_ANCHOR_CHECK_LIVE);
            mLiveRoomHandler.sendEmptyMessageAtTime(LiveRoomHandler.WHAT_ANCHOR_CHECK_LIVE, getNextTime(60000));
        }
    }

    /**
     * 主播checkLive
     */
    private void anchorCheckLive() {
        if (mContext instanceof LiveAnchorActivity) {
            ((LiveAnchorActivity) mContext).checkLive();
            startAnchorCheckLive();
        }
    }


    /**
     * 主播切后台，50秒后关闭直播
     */
    public void anchorPause() {
        if (mLiveRoomHandler != null) {
            mLiveRoomHandler.sendEmptyMessageAtTime(LiveRoomHandler.WHAT_ANCHOR_PAUSE, getNextTime(50000));
        }
    }

    private long getNextTime(int time) {
        long now = SystemClock.uptimeMillis();
        if (time < 1000) {
            return now + time;
        }
        return now + time + -now % 1000;
    }


    /**
     * 主播切后台后又回到前台
     */
    public void anchorResume() {
        if (mLiveRoomHandler != null) {
            mLiveRoomHandler.removeMessages(LiveRoomHandler.WHAT_ANCHOR_PAUSE);
        }
    }

    /**
     * 主播结束直播
     */
    private void anchorEndLive() {
        if (mContext != null && mContext instanceof LiveAnchorActivity) {
            ((LiveAnchorActivity) mContext).endLive();
        }
    }

    /**
     * 主播飘心
     */
    private void anchorLight() {
        if (mContext instanceof LiveAnchorActivity) {
            ((LiveAnchorActivity) mContext).light();
            startAnchorLight();
        }
    }


    /**
     * 幸运礼物中奖
     */
    public void onLuckGiftWin(LiveLuckGiftWinBean bean) {
        if (mLiveGiftAnimPresenter == null) {
            mLiveGiftAnimPresenter = new LiveGiftAnimPresenter(mContext, mContentView, mGifImageView, mSVGAImageView, mLiveGiftPrizePoolContainer);
        }
        mLiveGiftAnimPresenter.showLuckGiftWinAnim(bean);
    }


    /**
     * 奖池中奖
     */
    public void onPrizePoolWin(LiveGiftPrizePoolWinBean bean) {
        if (mLiveGiftAnimPresenter == null) {
            mLiveGiftAnimPresenter = new LiveGiftAnimPresenter(mContext, mContentView, mGifImageView, mSVGAImageView, mLiveGiftPrizePoolContainer);
        }
        mLiveGiftAnimPresenter.showPrizePoolWinAnim(bean);
    }

    /**
     * 奖池升级
     */
    public void onPrizePoolUp(String level) {
//        if (mLiveGiftAnimPresenter == null) {
//            mLiveGiftAnimPresenter = new LiveGiftAnimPresenter(mContext, mContentView, mGifImageView, mSVGAImageView, mLiveGiftPrizePoolContainer);
//        }
//        mLiveGiftAnimPresenter.showPrizePoolUp(level);
    }


    /**
     * 全站礼物
     */
    public void onGlobalGift(GlobalGiftBean bean) {
        if (mLiveGiftAnimPresenter == null) {
            mLiveGiftAnimPresenter = new LiveGiftAnimPresenter(mContext, mContentView, mGifImageView, mSVGAImageView, mLiveGiftPrizePoolContainer);
        }
        mLiveGiftAnimPresenter.showGlobalGift(bean);
    }


    /**
     * 设置当前显示的直播间商品
     */
    public void setShowGoodsBean(GoodsBean bean) {
        mShowGoodsBean = bean;
        if (bean == null) {
            if (mGoodsThumb != null) {
                mGoodsThumb.setImageDrawable(null);
            }
            if (mGoodsName != null) {
                mGoodsName.setText(null);
            }
            if (mGoodsPrice != null) {
                mGoodsPrice.setText(null);
            }
            if (mGroupGoods != null && mGroupGoods.getVisibility() != View.GONE) {
                mGroupGoods.setVisibility(View.GONE);
            }
        } else {
            if (mGroupGoods != null && mGroupGoods.getVisibility() != View.VISIBLE) {
                mGroupGoods.setVisibility(View.VISIBLE);
            }
            if (mGoodsThumb != null) {
                ImgLoader.display(mContext, bean.getThumb(), mGoodsThumb);
            }
            if (mGoodsName != null) {
                mGoodsName.setText(bean.getName());
            }
            if (mGoodsPrice != null) {
                mGoodsPrice.setText(bean.getPriceNow());
            }
        }
    }

    /**
     * 直播间购物飘屏
     */
    public void onLiveGoodsFloat(String userName) {
        if (mLiveGiftAnimPresenter == null) {
            mLiveGiftAnimPresenter = new LiveGiftAnimPresenter(mContext, mContentView, mGifImageView, mSVGAImageView, mLiveGiftPrizePoolContainer);
        }
        mLiveGiftAnimPresenter.onLiveGoodsFloat(userName);
    }

    public void release() {
        LiveHttpUtil.cancel(LiveHttpConsts.GET_USER_LIST);
        LiveHttpUtil.cancel(LiveHttpConsts.TIME_CHARGE);
        CommonHttpUtil.cancel(CommonHttpConsts.SET_ATTENTION);
        if (mLiveRoomHandler != null) {
            mLiveRoomHandler.release();
        }
        mLiveRoomHandler = null;
        if (mAnchorTimeHandler != null) {
            mAnchorTimeHandler.removeCallbacksAndMessages(null);
        }
        mAnchorTimeHandler = null;
        if (mLightAnimPresenter != null) {
            mLightAnimPresenter.release();
        }
        if (mLiveEnterRoomAnimPresenter != null) {
            mLiveEnterRoomAnimPresenter.release();
        }
        if (mLiveGiftAnimPresenter != null) {
            mLiveGiftAnimPresenter.release();
        }
        mRefreshUserListCallback = null;
        mTimeChargeCallback = null;
        if (mBtnViewHolder != null) {
            mBtnViewHolder.release();
        }
        mBtnViewHolder = null;
    }

    public void clearData() {
        LiveHttpUtil.cancel(LiveHttpConsts.GET_USER_LIST);
        LiveHttpUtil.cancel(LiveHttpConsts.TIME_CHARGE);
        CommonHttpUtil.cancel(CommonHttpConsts.SET_ATTENTION);
        if (mLiveRoomHandler != null) {
            mLiveRoomHandler.removeCallbacksAndMessages(null);
        }
        if (mAvatar != null) {
            mAvatar.setImageDrawable(null);
        }
        if (mLevelAnchor != null) {
            mLevelAnchor.setImageDrawable(null);
        }
        if (mName != null) {
            mName.setText("");
        }
        if (mID != null) {
            mID.setText("");
        }
        if (mVotes != null) {
            mVotes.setText("");
        }
        if (mGuardNum != null) {
            mGuardNum.setText("");
        }
        if (mLiveUserAdapter != null) {
            mLiveUserAdapter.clear();
        }
        if (mLiveChatAdapter != null) {
            mLiveChatAdapter.clear();
        }
        if (mLiveEnterRoomAnimPresenter != null) {
            mLiveEnterRoomAnimPresenter.cancelAnim();
            mLiveEnterRoomAnimPresenter.resetAnimView();
        }
        if (mLiveDanmuPresenter != null) {
            mLiveDanmuPresenter.release();
            mLiveDanmuPresenter.reset();
        }
        if (mLiveGiftAnimPresenter != null) {
            mLiveGiftAnimPresenter.cancelAllAnim();
        }
        setShowGoodsBean(null);
    }


    private static class LiveRoomHandler extends Handler {

        private LiveRoomViewHolder mLiveRoomViewHolder;
        private static final int WHAT_REFRESH_USER_LIST = 1;
        private static final int WHAT_TIME_CHARGE = 2;//计时收费房间定时请求接口扣费
        private static final int WHAT_ANCHOR_PAUSE = 4;//主播切后台
        private static final int WHAT_ANCHOR_LIGHT = 5;//主播飘心
        private static final int WHAT_ANCHOR_CHECK_LIVE = 6;//主播checkLive

        public LiveRoomHandler(LiveRoomViewHolder liveRoomViewHolder) {
            mLiveRoomViewHolder = new WeakReference<>(liveRoomViewHolder).get();
        }

        @Override
        public void handleMessage(Message msg) {
            if (mLiveRoomViewHolder != null) {
                switch (msg.what) {
                    case WHAT_REFRESH_USER_LIST:
                        mLiveRoomViewHolder.refreshUserList();
                        break;
                    case WHAT_TIME_CHARGE:
                        mLiveRoomViewHolder.requestTimeCharge();
                        break;
                    case WHAT_ANCHOR_PAUSE:
                        mLiveRoomViewHolder.anchorEndLive();
                        break;
                    case WHAT_ANCHOR_LIGHT:
                        mLiveRoomViewHolder.anchorLight();
                        break;
                    case WHAT_ANCHOR_CHECK_LIVE:
                        mLiveRoomViewHolder.anchorCheckLive();
                        break;
                }
            }
        }

        public void release() {
            removeCallbacksAndMessages(null);
            mLiveRoomViewHolder = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LiveHttpUtil.cancel(LiveHttpConsts.GET_USER_LIST);
        LiveHttpUtil.cancel(LiveHttpConsts.TIME_CHARGE);
        CommonHttpUtil.cancel(CommonHttpConsts.SET_ATTENTION);
    }



}
