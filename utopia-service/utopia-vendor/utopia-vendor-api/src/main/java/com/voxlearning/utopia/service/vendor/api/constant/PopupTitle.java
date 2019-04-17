package com.voxlearning.utopia.service.vendor.api.constant;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author peng.zhang.a
 * @Desc 学生端app自学乐园消息提醒类型
 * @since 16-7-13
 */
@Getter
@NoArgsConstructor
public enum PopupTitle {
    NEW("新消息", "/resources/app/17student/res/first_page/primary_study_function_icon_new.png", Boolean.FALSE),
    HOT("热门", "/resources/app/17student/res/first_page/primary_study_function_icon_hot.png", Boolean.FALSE),
    RECOMMEND("推荐", "/resources/app/17student/res/first_page/primary_study_function_icon_recommend.png", Boolean.FALSE),
    PARENT_REWARD("家长奖励", "", Boolean.FALSE),
    UNKNOWN("未知", "", Boolean.FALSE);

    private String desc;
    private String imgUrl;
    private Boolean tagInfo;  //标签图片不能确定里面是否包含文字信息，所以用这个字段控制是否返回desc给前端，感觉有点。。。

    PopupTitle(String desc, String imgUrl, Boolean tagInfo) {
        this.desc = desc;
        this.imgUrl = imgUrl;
        this.tagInfo = tagInfo;
    }

    public static PopupTitle parse(String name) {
        PopupTitle type;
        try {
            type = valueOf(name);
        } catch (Exception e) {
            return PopupTitle.UNKNOWN;
        }
        return type;
    }
}
