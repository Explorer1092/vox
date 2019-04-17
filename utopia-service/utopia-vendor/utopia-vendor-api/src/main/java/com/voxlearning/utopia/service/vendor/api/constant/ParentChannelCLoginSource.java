package com.voxlearning.utopia.service.vendor.api.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author shiwe.liao
 * @since 2016-7-22
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Deprecated
public enum ParentChannelCLoginSource {
    INDEX(0, Boolean.TRUE, ParentChannelCLoginResult.LOGIN_SUCCESS, "首页登录按钮"),
    PERSONAL_CENTER(1, Boolean.TRUE, ParentChannelCLoginResult.LOGIN_SUCCESS, "个人中心登录按钮"),
    PERSONAL_CENTER_ADD_CHILD(2, Boolean.TRUE, ParentChannelCLoginResult.LOGIN_SUCCESS, "个人中心添加孩子按钮"),
    PERSONAL_CENTER_ORDER(3, Boolean.TRUE, ParentChannelCLoginResult.LOGIN_SUCCESS, "个人中心我的订单按钮"),
    @Deprecated
    CHILD_PERFORMANCE_ADD_CHILD(4, Boolean.FALSE, ParentChannelCLoginResult.ADD_STUDENT, "宝贝表现添加孩子按钮"),
    @Deprecated
    CHILD_PERFORMANCE_SELF_STUDY_TOOL(5, Boolean.FALSE, ParentChannelCLoginResult.ADD_STUDENT, "宝贝表现自学工具添加孩子按钮"),
    @Deprecated
    JXT_NEWS_VOTE(6, Boolean.FALSE, ParentChannelCLoginResult.LOGIN_SUCCESS, "资讯点赞按钮"),
    JXT_NEWS_COMMENT(7, Boolean.FALSE, ParentChannelCLoginResult.LOGIN_SUCCESS, "资讯评论、收藏、订阅、点赞按钮"),
    @Deprecated
    JXT_NEWS_COMMENT_VOTE(8, Boolean.FALSE, ParentChannelCLoginResult.LOGIN_SUCCESS, "资讯评论点赞按钮"),
    @Deprecated
    NEW_GROWTH_TAB(9, Boolean.TRUE, ParentChannelCLoginResult.ADD_STUDENT, "新的成长TAB添加孩子按钮"),
    SHARE_MENU_EASE_MOB(10, Boolean.FALSE, ParentChannelCLoginResult.LOGIN_SUCCESS, "分享菜单-家长通群聊按钮"),
    SHARE_MENU_OFFICIAL_ACCOUNT(11, Boolean.FALSE, ParentChannelCLoginResult.LOGIN_SUCCESS, "分享菜单-查看公众号按钮"),
    SHARE_MENU_COLLECT(12, Boolean.FALSE, ParentChannelCLoginResult.LOGIN_SUCCESS, "分享菜单-收藏按钮"),
    SHARE_MENU_HELP(13, Boolean.FALSE, ParentChannelCLoginResult.LOGIN_SUCCESS, "分享菜单-帮助按钮"),
    NEWS_RECOMMEND_LIST_SYNC_APP(14, Boolean.FALSE, ParentChannelCLoginResult.LOGIN_SUCCESS, "资讯推荐列表的同步应用"),
    INDEX_ADD_CHILD(15, Boolean.FALSE, ParentChannelCLoginResult.LOGIN_SUCCESS, "首页-添加孩子"),
    INDEX_PARENT_REWARD(16, Boolean.FALSE, ParentChannelCLoginResult.LOGIN_SUCCESS, "首页-家长奖励"),
    INDEX_PIC_LISTEN(17, Boolean.FALSE, ParentChannelCLoginResult.LOGIN_SUCCESS, "首页-xx学习-点读机"),
    INDEX_PICTURE_BOOK(17, Boolean.FALSE, ParentChannelCLoginResult.LOGIN_SUCCESS, "首页-xx学习-绘本"),
    INDEX_MY_SUBSCRIBE(17, Boolean.FALSE, ParentChannelCLoginResult.LOGIN_SUCCESS, "首页-xx学习-我的订阅"),
    STUDY_MY_SUBSCRIBE(18, Boolean.FALSE, ParentChannelCLoginResult.LOGIN_SUCCESS, "自学-顶部-我的订阅-H5"),
    STUDY_ALBUM_SUBSCRIBE(19, Boolean.FALSE, ParentChannelCLoginResult.LOGIN_SUCCESS, "自学-专辑订阅按钮-H5"),
    USER_NEWS_HISTORY(20, Boolean.FALSE, ParentChannelCLoginResult.LOGIN_SUCCESS, "用户资讯历史页入口"),
    PERSONAL_CENTER_MY_COLLECT(21, Boolean.FALSE, ParentChannelCLoginResult.LOGIN_SUCCESS, "个人中心-我的收藏"),
    PERSONAL_CENTER_FAQ(22, Boolean.FALSE, ParentChannelCLoginResult.LOGIN_SUCCESS, "个人中心-FAQ"),
    INDEX_LOGIN_OUT(23, Boolean.TRUE, ParentChannelCLoginResult.LOGIN_SUCCESS, "退出登录后跳到登录页"),
    YIQI_XUE(24, Boolean.FALSE, ParentChannelCLoginResult.LOGIN_SUCCESS, "一起学H5"),
    YIQI_XUE_PLATFORM_LESSON(25, Boolean.FALSE, ParentChannelCLoginResult.LOGIN_SUCCESS, "一起学平台课程触发登录"),
    INDEX_WECHAT_LOGIN(26, Boolean.TRUE, ParentChannelCLoginResult.LOGIN_SUCCESS, "首页微信登录"),


    UNKNOWN(-1, Boolean.FALSE, ParentChannelCLoginResult.NONE, "未知");
    private final int type;
    private final Boolean checkStudent;
    private final ParentChannelCLoginResult loginResult;
    private final String desc;

    public static final Map<Integer, ParentChannelCLoginSource> maps;

    static {
        maps = new HashMap<>();
        for (ParentChannelCLoginSource source : ParentChannelCLoginSource.values()) {
            if (!maps.containsKey(source.getType())) {
                maps.put(source.getType(), source);
            }
        }
    }

    public static ParentChannelCLoginSource parseWithUnknown(Integer source) {
        ParentChannelCLoginSource loginSource = maps.get(source);
        return loginSource == null ? ParentChannelCLoginSource.UNKNOWN : loginSource;
    }
}
