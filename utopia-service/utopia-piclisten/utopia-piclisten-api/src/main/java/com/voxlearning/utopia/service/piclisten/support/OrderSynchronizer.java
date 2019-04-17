package com.voxlearning.utopia.service.piclisten.support;

import com.voxlearning.utopia.service.order.api.entity.UserOrder;

import java.util.function.Consumer;

/**
 * @author xinxin
 * @since 7/11/17.
 * <p>
 * order synchronize interface
 */
public interface OrderSynchronizer extends Consumer<UserOrder> {
    String PUBLISHER_PEP = "renjiao";
    String PUBLISHER_FLTRP = "waiyan";
    String PUBLISHER_SEPH = "hujiao";
    String FIELD_APP_ID = "appId";
    String FIELD_USER_NAME = "username";
    String FIELD_NEW_BOOK_CODE = "newBookEditionCode";
    String FIELD_ORDER_NO = "orderSn";
    String FIELD_MOBILE = "mobile";
    String FIELD_SIGN = "sign";
    String FIELD_SDK_NAME = "sdk";
    String FIELD_BOOK_ID = "sdk_book_id";
    String FIELD_BOOK_PERIOD = "period";
    String FIELD_PACKAGE_ID = "piclisten_package_id";
    String CONFIG_PICLISTEN_WAIYANSHE_APPID = "piclisten.waiyanshe.appid";
    String CONFIG_PICLISTEN_WAIYANSHE_SECRET = "piclisten.waiyanshe.secret";
    String CONFIG_PICLISTEN_RENJIAO_APPID = "piclisten.renjiao.appid";
    String CONFIG_PICLISTEN_RENJIAO_SECRET = "piclisten.renjiao.secret";
    String CONFIG_PICLISTEN_SEPH_SECRET = "piclisten.seph.secret";
    String CONFIG_PICLISTEN_SEPH_IV = "piclisten.seph.iv";
    String CONFIG_ORDER_SYNCHRONIZE_TARGET_FLTRP = "ORDER_SYNCHRONIZE_TARGET_FLTRP";
    String CONFIG_ORDER_SYNCHRONIZE_TARGET_PEP = "ORDER_SYNCHRONIZE_TARGET_PEP";
    String CONFIG_ORDER_SYNC_TARGET_PEP_PACKAGE = "ORDER_SYNC_TARGET_PEP_PACKAGE";
    String CONFIG_ORDER_SYNCHRONIZE_TARGET_SEPH = "ORDER_SYNCHRONIZE_TARGET_SEPH";
    String CONFIG_ORDER_SYNCHRONIZE_REFUND_TARGET_PEP = "ORDER_SYNCHRONIZE_REFUND_PEP";
    String CONFIG_ORDER_SYNC_REFUND_PEP_PACKAGE = "ORDER_SYNC_REFUND_PEP_PACKAGE";
    String CONFIG_ORDER_SYNCHRONIZE_REFUND_TARGET_SEPH = "ORDER_SYNCHRONIZE_REFUND_SEPH";
    String CONFIG_ORDER_SYNCHRONIZE_TARGET_QUERY_MOBILE = "ORDER_SYNCHRONIZE_QUERY_MOBILE";
    String CONFIG_ORDER_CHANGE_BOOK_TARGET_FLTRP = "ORDER_CHANGE_BOOK_TARGET_FLTRP";
    String CONFIG_ORDER_CHANGE_BOOK_TARGET_PEP = "ORDER_CHANGE_BOOK_TARGET_PEP";
    String CONFIG_ORDER_CHANGE_BOOK_TARGET_SEPH = "ORDER_CHANGE_BOOK_TARGET_SEPH";
}
