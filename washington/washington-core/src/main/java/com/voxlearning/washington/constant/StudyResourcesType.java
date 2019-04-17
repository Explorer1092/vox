package com.voxlearning.washington.constant;

import com.voxlearning.alps.annotation.meta.Subject;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 优质练习推荐位
 *
 * @author feng.guo
 * @since 2019-01-17
 */
@Getter
@AllArgsConstructor
public enum StudyResourcesType {

    SYNCHRONOUS_EXERCISE("家长布置练习", Subject.MATH,
            "/jiazhangbuzhilianxiyunying/2019/01/23/20190123104908119445.png",
            "/view/mobile/parent/homework_parent/homework_list.vpage",
            "名校同步练习", "精选名校习题，匹配本地教材，同步校内进度"),

    QUICK_ORAL_ARITHMETIC("在线口算速算", Subject.MATH,
            "/kousuanlianxiyunying/2019/01/23/20190123175738609741.png",
            "/view/mobile/parent/homework_parent/homework_list.vpage",
            "线上口算速算", "每天一练，支持键盘和手写"),

    ORAL_ARITHMETIC_EXERCISE("纸质口算练习", Subject.MATH,
            "/dayinkousuanyunying/2019/01/23/20190123175812843626.png",
            "/view/mobile/parent/print_oral/index.vpage?referrer=1&useNewCore=wk",
            "打印纸质口算本", "每周打印一本，孩子轻松练，父母秒批改"),

    MATH_PRACTICE("数学重难点辅导", Subject.MATH,
            "/shuxuezhongnandianfudao/2019/02/21/20190221162700101715.png",
            "/view/mobile/parent/curx_in_math/index.vpage?referrer=0&useNewCore=wk",
            "数学重难点辅导", "趣味课程和单元自测");

    /**
     * 名称
     */
    private String name;
    /**
     * 科目
     */
    private Subject subject;
    /**
     * icon地址
     */
    private String iconUrl;
    /**
     * 跳转地址
     */
    private String h5Url;
    /**
     * 应用名称
     */
    private String mainTitle;
    /**
     * 应用副标题
     */
    private String subheading;
}
