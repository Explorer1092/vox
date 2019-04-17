package com.voxlearning.utopia.service.vendor.api.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author malong
 * @since 2016/8/3
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum StudentTabType {
    FAIRYLAND(0, "自学乐园", "2.4.0.0", null, "Web", "/studentMobile/center/fairyland.vpage", "/resources/app/17student/res/first_page/primary_student_self_study_normal_new.png"),
    HOMEWORK_HISTORY(1, "作业记录", "1.0.0", null, "Web", "/studentMobile/homework/historylist.vpage", "resources/app/17student/res/first_page/primary_student_study_record_normal_new.png");

    private final int tabType;
    private final String tabName;
    private final String startVer;
    private final String endVer;
    private final String functionType;
    private final String functionUrl;
    private final String imgUrl;
}
