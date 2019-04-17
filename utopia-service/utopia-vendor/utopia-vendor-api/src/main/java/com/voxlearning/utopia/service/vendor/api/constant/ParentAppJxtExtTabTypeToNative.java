package com.voxlearning.utopia.service.vendor.api.constant;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.core.helper.VersionUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 家长同APP家校沟通的版本首页可配置的选项类型预定义
 *
 * @author shiwe.liao
 * @since 2016/4/15
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum ParentAppJxtExtTabTypeToNative {
    HOMEWORK_STATE(1, 1, "我的作业", "查看最新作业通知和报告", "/public/skin/parentMobile/images/new_icon/myhomework.png", Boolean.FALSE, "", Boolean.FALSE),
    USER_MESSAGE(2, 2, "系统通知", "查看一起作业官方通知和消息", "/public/skin/parentMobile/images/new_icon/xiaoxi.png", Boolean.FALSE, "", Boolean.TRUE),
    ORDER_LIST(3, 3, "我的订单", "查看已购买商品和待支付订单", "", Boolean.FALSE, "", Boolean.FALSE),
    FEED_BACK(4, 4, "意见反馈", "提交您的意见和建议", "", Boolean.FALSE, "", Boolean.FALSE),
    CUSTOMER_SERVICE(5, 5, "在线客服", "联系一起作业客服人员", "", Boolean.FALSE, "", Boolean.FALSE),
    OFFICIAL_ACCOUNT(6, 6, "公众号", "联系一起作业客服人员", "", Boolean.TRUE, "1.6.0", Boolean.TRUE);

    private final int type;
    private final int rank;
    private final String tabName;      // 标题
    private final String tabExtInfo;   // 副标题
    private final String tabIcon;      // 图标
    private final Boolean showCount;   // true 为数字  false为红点
    private final String version;
    private final Boolean online;

    public static List<ParentAppJxtExtTabTypeToNative> onlineTypeList(String version) {
        List<ParentAppJxtExtTabTypeToNative> list = new ArrayList<>();
        for (ParentAppJxtExtTabTypeToNative type : ParentAppJxtExtTabTypeToNative.values()) {
            if (Boolean.TRUE == type.getOnline() && (StringUtils.isBlank(type.getVersion()) || VersionUtil.compareVersion(version, type.getVersion()) >= 0)) {
                list.add(type);
            }
        }
        Collections.sort(list, (o1, o2) -> o1.getRank() - o2.getRank());
        return list;
    }
}
