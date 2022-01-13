package com.yuanfen.mall.views;

import android.content.Context;
import android.view.ViewGroup;

import com.yuanfen.mall.adapter.SellerOrderBaseAdapter;
import com.yuanfen.mall.adapter.SellerOrderRefundAdapter;

/**
 * 卖家 订单列表 待退款
 */
public class SellerOrderRefundViewHolder extends AbsSellerOrderViewHolder {

    public SellerOrderRefundViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    @Override
    public String getOrderType() {
        return "wait_refund";
    }

    @Override
    public SellerOrderBaseAdapter getSellerOrderAdapter() {
        return new SellerOrderRefundAdapter(mContext);
    }

}
