package com.yuanfen.live.views;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import  androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import  androidx.recyclerview.widget.RecyclerView;;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.yuanfen.common.CommonAppConfig;
import com.yuanfen.common.Constants;
import com.yuanfen.common.activity.AbsActivity;
import com.yuanfen.common.glide.ImgLoader;
import com.yuanfen.common.http.HttpCallback;
import com.yuanfen.common.interfaces.ActivityResultCallback;
import com.yuanfen.common.interfaces.CommonCallback;
import com.yuanfen.common.interfaces.ImageResultCallback;
import com.yuanfen.common.mob.MobCallback;
import com.yuanfen.common.upload.UploadBean;
import com.yuanfen.common.upload.UploadCallback;
import com.yuanfen.common.upload.UploadStrategy;
import com.yuanfen.common.upload.UploadUtil;
import com.yuanfen.common.utils.ActivityResultUtil;
import com.yuanfen.common.utils.DialogUitl;
import com.yuanfen.common.utils.DpUtil;
import com.yuanfen.common.utils.L;
import com.yuanfen.common.utils.MediaUtil;
import com.yuanfen.common.utils.ScreenDimenUtil;
import com.yuanfen.common.utils.StringUtil;
import com.yuanfen.common.utils.ToastUtil;
import com.yuanfen.common.utils.WordUtil;
import com.yuanfen.common.views.AbsViewHolder;
import com.yuanfen.live.R;
import com.yuanfen.live.activity.LiveActivity;
import com.yuanfen.live.activity.LiveAnchorActivity;
import com.yuanfen.live.activity.LiveChooseClassActivity;
import com.yuanfen.live.adapter.LiveReadyShareAdapter;
import com.yuanfen.live.bean.LiveRoomTypeBean;
import com.yuanfen.live.dialog.LiveRoomTypeDialogFragment;
import com.yuanfen.live.dialog.LiveTimeDialogFragment;
import com.yuanfen.live.http.LiveHttpConsts;
import com.yuanfen.live.http.LiveHttpUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cxf on 2018/10/7.
 * 开播前准备
 */

public class LiveReadyViewHolder extends AbsViewHolder implements View.OnClickListener {

    private ImageView mAvatar;
    private TextView mCoverText;
    private EditText mEditTitle;
    private RecyclerView mLiveShareRecyclerView;
    private LiveReadyShareAdapter mLiveShareAdapter;
    private File mAvatarFile;
    private TextView mCity;
    private ImageView mLocationImg;
    private TextView mLiveClass;
    private TextView mLiveTypeTextView;//房间类型TextView
    private int mLiveClassID;//直播频道id
    private int mLiveType;//房间类型
    private int mLiveTypeVal;//门票收费金额
    private String mLivePwd = "";//房间密码，
    private int mLiveTimeCoin;//计时收费金额
    private ActivityResultCallback mActivityResultCallback;
    private CommonCallback<LiveRoomTypeBean> mLiveTypeCallback;
    private boolean mOpenLocation = true;
    private int mLiveSdk;
    private CheckBox mShopSwitch;//购物车开关
    private String mShopOpenString;
    private String mShopCloseString;
    private int haveStore;


    public LiveReadyViewHolder(Context context, ViewGroup parentView, int liveSdk, int haveStore) {
        super(context, parentView, liveSdk, haveStore);
        View group_1 = findViewById(R.id.group_1);
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) group_1.getLayoutParams();
        params.topMargin = ScreenDimenUtil.getInstance().getStatusBarHeight() + DpUtil.dp2px(5);
        group_1.requestLayout();
    }

    @Override
    protected void processArguments(Object... args) {
        if (args.length > 0) {
            mLiveSdk = (int) args[0];
        }
        if (args.length > 1) {
            haveStore = (int) args[1];
        }
    }

    @Override
    protected int getLayoutId() {
        if (((LiveActivity) mContext).isVoiceChatRoom()) {
            return R.layout.view_live_ready_voice;
        }
        return R.layout.view_live_ready;
    }

    @Override
    public void init() {
        mAvatar = (ImageView) findViewById(R.id.avatar);
        mCoverText = (TextView) findViewById(R.id.cover_text);
        mEditTitle = (EditText) findViewById(R.id.edit_title);
        mCity = (TextView) findViewById(R.id.city);
        mCity.setText(CommonAppConfig.getInstance().getCity());
        mLocationImg = (ImageView) findViewById(R.id.location_img);
        findViewById(R.id.btn_locaiton).setOnClickListener(this);
        mOpenLocation = true;

        mLiveShareRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mLiveShareRecyclerView.setHasFixedSize(true);
        mLiveShareRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        mLiveShareAdapter = new LiveReadyShareAdapter(mContext);
        mLiveShareRecyclerView.setAdapter(mLiveShareAdapter);
        findViewById(R.id.avatar_group).setOnClickListener(this);
        findViewById(R.id.btn_close).setOnClickListener(this);
        findViewById(R.id.btn_start_live).setOnClickListener(this);

        if (!((LiveActivity) mContext).isVoiceChatRoom()) {
            mLiveTypeTextView = (TextView) findViewById(R.id.btn_room_type);
            mLiveTypeTextView.setOnClickListener(this);
            mLiveClass = (TextView) findViewById(R.id.live_class);
            mLiveClass.setOnClickListener(this);
            findViewById(R.id.btn_camera).setOnClickListener(this);
            findViewById(R.id.btn_beauty).setOnClickListener(this);
            mActivityResultCallback = new ActivityResultCallback() {
                @Override
                public void onSuccess(Intent intent) {
                    mLiveClassID = intent.getIntExtra(Constants.CLASS_ID, 0);
                    mLiveClass.setText(intent.getStringExtra(Constants.CLASS_NAME));
                }
            };
            mLiveTypeCallback = new CommonCallback<LiveRoomTypeBean>() {
                @Override
                public void callback(LiveRoomTypeBean bean) {
                    switch (bean.getId()) {
                        case Constants.LIVE_TYPE_NORMAL:
                            onLiveTypeNormal(bean);
                            break;
                        case Constants.LIVE_TYPE_PWD:
                            onLiveTypePwd(bean);
                            break;
                        case Constants.LIVE_TYPE_PAY:
                            onLiveTypePay(bean);
                            break;
                        case Constants.LIVE_TYPE_TIME:
                            onLiveTypeTime(bean);
                            break;
                    }
                }
            };
            mShopSwitch = (CheckBox) findViewById(R.id.check_box);
            if (haveStore == 1) {
                mShopSwitch.setVisibility(View.VISIBLE);
                mShopOpenString = WordUtil.getString(R.string.live_shop_open);
                mShopCloseString = WordUtil.getString(R.string.live_shop_close);
                mShopSwitch.setOnClickListener(this);
            }
        }

    }

    @Override
    public void onClick(View v) {
        if (!canClick()) {
            return;
        }
        int i = v.getId();
        if (i == R.id.avatar_group) {
            setAvatar();

        } else if (i == R.id.btn_camera) {
            toggleCamera();

        } else if (i == R.id.btn_close) {
            close();

        } else if (i == R.id.live_class) {
            chooseLiveClass();

        } else if (i == R.id.btn_beauty) {
            beauty();

        } else if (i == R.id.btn_room_type) {
            chooseLiveType();

        } else if (i == R.id.btn_start_live) {
            startLive();

        } else if (i == R.id.btn_locaiton) {
            switchLocation();
        } else if (i == R.id.check_box) {
            setShopSwitch();
        }
    }

    private void setShopSwitch() {
        if (mShopSwitch != null) {
            mShopSwitch.setText(mShopSwitch.isChecked() ? mShopCloseString : mShopOpenString);
        }
    }

    /**
     * 打开 关闭位置
     */
    private void switchLocation() {
        if (mOpenLocation) {
            new DialogUitl.Builder(mContext)
                    .setContent(WordUtil.getString(R.string.live_location_close_3))
                    .setCancelable(true)
                    .setConfrimString(WordUtil.getString(R.string.live_location_close_2))
                    .setClickCallback(new DialogUitl.SimpleCallback() {

                        @Override
                        public void onConfirmClick(Dialog dialog, String content) {
                            toggleLocation();
                        }
                    })
                    .build()
                    .show();
        } else {
            toggleLocation();
        }
    }

    private void toggleLocation() {
        mOpenLocation = !mOpenLocation;
        if (mLocationImg != null) {
            mLocationImg.setImageResource(mOpenLocation ? R.mipmap.icon_live_ready_location_1 : R.mipmap.icon_live_ready_location_0);
        }
        if (mCity != null) {
            mCity.setText(mOpenLocation ? CommonAppConfig.getInstance().getCity() : WordUtil.getString(R.string.live_location_close));
        }
    }

    /**
     * 设置头像
     */
    private void setAvatar() {
        final ImageResultCallback imageResultCallback = new ImageResultCallback() {

            @Override
            public void beforeCamera() {
                ((LiveAnchorActivity) mContext).beforeCamera();
            }

            @Override
            public void onSuccess(File file) {
                if (file != null) {
                    ImgLoader.display(mContext, file, mAvatar);
                    if (mAvatarFile == null) {
                        mCoverText.setText(WordUtil.getString(R.string.live_cover_2));
                        mCoverText.setBackground(ContextCompat.getDrawable(mContext, R.drawable.bg_live_cover));
                    }
                    mAvatarFile = file;
                }
            }

            @Override
            public void onFailure() {
            }
        };
        if (mLiveSdk == Constants.LIVE_SDK_TX) {
            MediaUtil.getImageByAlumb((AbsActivity) mContext, imageResultCallback);
        } else {
            DialogUitl.showStringArrayDialog(mContext, new Integer[]{
                    R.string.alumb, R.string.camera}, new DialogUitl.StringArrayDialogCallback() {
                @Override
                public void onItemClick(String text, int tag) {
                    if (tag == R.string.camera) {
                        MediaUtil.getImageByCamera((AbsActivity) mContext, imageResultCallback);
                    } else {
                        MediaUtil.getImageByAlumb((AbsActivity) mContext, imageResultCallback);
                    }
                }
            });
        }
    }

    /**
     * 切换镜头
     */
    private void toggleCamera() {
        ((LiveAnchorActivity) mContext).toggleCamera();
    }

    /**
     * 关闭
     */
    private void close() {
        ((LiveAnchorActivity) mContext).onBackPressed();
    }

    /**
     * 选择直播频道
     */
    private void chooseLiveClass() {
        Intent intent = new Intent(mContext, LiveChooseClassActivity.class);
        intent.putExtra(Constants.CLASS_ID, mLiveClassID);
        ActivityResultUtil.startActivityForResult(((AbsActivity) mContext), intent, mActivityResultCallback);
    }

    /**
     * 设置美颜
     */
    private void beauty() {
        ((LiveAnchorActivity) mContext).beauty();
    }

    /**
     * 选择直播类型
     */
    private void chooseLiveType() {
        Bundle bundle = new Bundle();
        bundle.putInt(Constants.CHECKED_ID, mLiveType);
        LiveRoomTypeDialogFragment fragment = new LiveRoomTypeDialogFragment();
        fragment.setArguments(bundle);
        fragment.setCallback(mLiveTypeCallback);
        fragment.show(((LiveAnchorActivity) mContext).getSupportFragmentManager(), "LiveRoomTypeDialogFragment");
    }

    /**
     * 普通房间
     */
    private void onLiveTypeNormal(LiveRoomTypeBean bean) {
        mLiveType = bean.getId();
        mLiveTypeTextView.setText(bean.getName());
        mLiveTypeVal = 0;
        mLiveTimeCoin = 0;
        mLivePwd = "";
    }

    /**
     * 密码房间
     */
    private void onLiveTypePwd(final LiveRoomTypeBean bean) {
        new DialogUitl.Builder(mContext)
                .setTitle(WordUtil.getString(R.string.live_set_pwd))
                .setCancelable(true)
                .setInput(true)
                .setHint(WordUtil.getString(R.string.login_input_pwd))
                .setInputType(DialogUitl.INPUT_TYPE_NUMBER_PASSWORD)
                .setClickCallback(new DialogUitl.SimpleCallback() {
                    @Override
                    public void onConfirmClick(Dialog dialog, String content) {
                        if (TextUtils.isEmpty(content)) {
                            ToastUtil.show(R.string.live_set_pwd_empty);
                        } else {
                            mLiveType = bean.getId();
                            mLiveTypeTextView.setText(bean.getName());
                            mLiveTypeVal = 0;
                            mLivePwd = content;
                            mLiveTimeCoin = 0;
                            dialog.dismiss();
                        }
                    }
                })
                .build()
                .show();
    }

    /**
     * 付费房间
     */
    private void onLiveTypePay(final LiveRoomTypeBean bean) {

        new DialogUitl.Builder(mContext)
                .setTitle(WordUtil.getString(R.string.live_set_fee))
                .setCancelable(true)
                .setInput(true)
                .setHint(WordUtil.getString(R.string.mall_339))
                .setInputType(DialogUitl.INPUT_TYPE_NUMBER)
                .setLength(8)
                .setClickCallback(new DialogUitl.SimpleCallback() {
                    @Override
                    public void onConfirmClick(Dialog dialog, String content) {
                        if (TextUtils.isEmpty(content)) {
                            ToastUtil.show(R.string.live_set_fee_empty);
                        } else {
                            mLiveType = bean.getId();
                            mLiveTypeTextView.setText(bean.getName());
                            if (StringUtil.isInt(content)) {
                                mLiveTypeVal = Integer.parseInt(content);
                            }
                            mLivePwd = "";
                            mLiveTimeCoin = 0;
                            dialog.dismiss();
                        }
                    }
                })
                .build()
                .show();


    }

    /**
     * 计时房间
     */
    private void onLiveTypeTime(final LiveRoomTypeBean bean) {
        LiveTimeDialogFragment fragment = new LiveTimeDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(Constants.CHECKED_COIN, mLiveTimeCoin);
        fragment.setArguments(bundle);
        fragment.setCommonCallback(new CommonCallback<Integer>() {
            @Override
            public void callback(Integer coin) {
                mLiveType = bean.getId();
                mLiveTypeTextView.setText(bean.getName());
                mLiveTypeVal = coin;
                mLivePwd = "";
                mLiveTimeCoin = coin;
            }
        });
        fragment.show(((LiveAnchorActivity) mContext).getSupportFragmentManager(), "LiveTimeDialogFragment");
    }

    public void hide() {
        if (mContentView != null && mContentView.getVisibility() == View.VISIBLE) {
            mContentView.setVisibility(View.INVISIBLE);
        }
    }


    public void show() {
        if (mContentView != null && mContentView.getVisibility() != View.VISIBLE) {
            mContentView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 点击开始直播按钮
     */
    private void startLive() {
        if (!((LiveActivity) mContext).isVoiceChatRoom()) {
            boolean startPreview = ((LiveAnchorActivity) mContext).isStartPreview();
            if (!startPreview) {
                ToastUtil.show(R.string.please_wait);
                return;
            }
            if (mLiveClassID == 0) {
                ToastUtil.show(R.string.live_choose_live_class);
                return;
            }
        }
        if (mLiveShareAdapter != null) {
            String type = mLiveShareAdapter.getShareType();
            if (!TextUtils.isEmpty(type)) {
                ((LiveActivity) mContext).shareLive(type, mEditTitle.getText().toString().trim(), new MobCallback() {
                    @Override
                    public void onSuccess(Object data) {
                        LiveHttpUtil.dailyTaskShareLive();
                    }

                    @Override
                    public void onError() {

                    }

                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onFinish() {
                        createRoom();
                    }
                });
            } else {
                createRoom();
            }
        } else {
            createRoom();
        }
    }

    /**
     * 请求创建直播间接口，开始直播
     */
    private void createRoom() {
        if (!((LiveActivity) mContext).isVoiceChatRoom()) {
            if (mLiveClassID == 0) {
                ToastUtil.show(R.string.live_choose_live_class);
                return;
            }
        }
        final String title = mEditTitle.getText().toString().trim();
        int isShop = 0;
        if (!((LiveActivity) mContext).isVoiceChatRoom() && mShopSwitch.isChecked()) {
            isShop = 1;
        }
        final int finalShop = isShop;
        final String typeVal = mLiveType == Constants.LIVE_TYPE_PWD ? mLivePwd : String.valueOf(mLiveTypeVal);
        if (mAvatarFile != null && mAvatarFile.exists()) {
            UploadUtil.startUpload(new CommonCallback<UploadStrategy>() {
                @Override
                public void callback(UploadStrategy strategy) {
                    List<UploadBean> list = new ArrayList<>();
                    list.add(new UploadBean(mAvatarFile, UploadBean.IMG));
                    strategy.upload(list, false, new UploadCallback() {
                        @Override
                        public void onFinish(List<UploadBean> list, boolean success) {
                            if (success) {
                                LiveHttpUtil.createRoom(title, mLiveClassID, mLiveType, typeVal, finalShop, list.get(0).getRemoteFileName(), mOpenLocation, ((LiveActivity) mContext).isVoiceChatRoom(), new HttpCallback() {
                                    @Override
                                    public void onSuccess(int code, String msg, String[] info) {
                                        if (code == 0 && info.length > 0) {
                                            L.e("开播", "createRoom------->" + info[0]);

                                            ((LiveAnchorActivity) mContext).startLiveSuccess(info[0], mLiveType, mLiveTypeVal, title);
                                            ((LiveAnchorActivity) mContext).openShop(finalShop == 1);
                                        } else {
                                            ToastUtil.show(msg);
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            });
        } else {
            LiveHttpUtil.createRoom(title, mLiveClassID, mLiveType, typeVal, finalShop, null, mOpenLocation, ((LiveActivity) mContext).isVoiceChatRoom(), new HttpCallback() {
                @Override
                public void onSuccess(int code, String msg, String[] info) {
                    if (code == 0 && info.length > 0) {
                        L.e("开播", "createRoom------->" + info[0]);

                        ((LiveAnchorActivity) mContext).startLiveSuccess(info[0], mLiveType, mLiveTypeVal, title);
                        ((LiveAnchorActivity) mContext).openShop(finalShop == 1);
                    } else {
                        ToastUtil.show(msg);
                    }
                }
            });
        }
    }

    public void release() {
        mActivityResultCallback = null;
        mLiveTypeCallback = null;
    }

    @Override
    public void onDestroy() {
        UploadUtil.cancelUpload();
        LiveHttpUtil.cancel(LiveHttpConsts.CREATE_ROOM);
    }
}
