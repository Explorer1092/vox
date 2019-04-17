package com.voxlearning.utopia.service.vendor.api.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author shiwei.liao
 * @since 2016-11-29
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Deprecated
public enum IMMessageExtButton {
    //第三个字段link。由于是与业务强相关的。所以这里只能标识需要这样一个字段。具体值还需要根据业务由业务自己去指定
    SEND_FLOWER("去送花", "#ffffff", "#FF8971", "", Boolean.FALSE, "", "", ""),
    OPEN_HOMEWORK_REPORT("查看作业报告", "#ffffff", "#41BB54", "", Boolean.FALSE, "", "", ""),
    OPEN_WEEK_REPORT("查看周报告", "#ffffff", "#41BB54", "", Boolean.TRUE, "查看周报告", "#ffffff", "#15A6EE"),
    OPEN_URGE_HOMEWORK("查看作业", "#ffffff", "#41BB54", "", Boolean.TRUE, "查看作业", "#ffffff", "#15A6EE"),
    OPEN_URGE_CORRECT_HOMEWORK("查看作业", "#ffffff", "#41BB54", "", Boolean.TRUE, "查看作业", "#ffffff", "#15A6EE"),
    OPEN_HOMEWORK_SUMMARY("查看作业小结", "#ffffff", "#41BB54", "", Boolean.TRUE, "作业小结", "#ffffff", "#15A6EE"),
    OPEN_HOMEWORK_REVIEW("查看复习小结", "#ffffff", "#41BB54", "", Boolean.TRUE, "复习小结", "#ffffff", "#15A6EE"),
    CHECK_HOMEWORK("查看作业", "#ffffff", "#FF8971", "", Boolean.FALSE, "", "", ""),
    CHECK_EXPAND_HOMEWORK("查看拓展任务", "#ffffff", "#32CD32", "", Boolean.FALSE, "", "", ""),
    CHECK_EXAM("查看测试", "#ffffff", "#FF8971", "", Boolean.FALSE, "", "", ""),
    OPEN_EXAM_REPORT("查看测试报告", "#ffffff", "#41BB54", "", Boolean.FALSE, "", "", ""),
    OPEN_RECOMMEND_NEWS("查看学习资源", "#ffffff", "#41BB54", "", Boolean.TRUE, "查看学习资源", "#ffffff", "#41BB54"),
    OPEN_VACATION_HOMEWORK("查看寒假作业", "#ffffff", "#41BB54", "", Boolean.FALSE, "", "", ""),
    OFFLINE_HOMEWORK_DETAIL("查看详情", "#ffffff", "#41BB54", "", Boolean.TRUE, "查看详情", "#ffffff", "#15A6EE"),
    OPEN_VACATION_DETAIL("查看完成情况", "#ffffff", "#41BB54", "", Boolean.FALSE, "", "", ""),
    OPEN_EVALUATION_DETAIL("查看报告", "#ffffff", "#41BB54", "", Boolean.FALSE, "", "", "")
    ;


    private final String buttonContent;
    private final String contentColor;
    private final String backColor;
    private final String link;

    //2017.06.29一下字段新为老师端配置
    private final Boolean teacherIsShow;
    private final String teacherButtonContent;
    private final String teacherContentColor;
    private final String teacherBackColor;
}
