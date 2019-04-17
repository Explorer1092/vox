package com.voxlearning.utopia.service.ai.data;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @author guangqing
 * @since 2019/2/22
 */
@Getter
@Setter
public class RenewFirstPojo implements Serializable {
    private static final long serialVersionUID = -4983500700001944549L;
    private String openingRemarks;//开场白 改成 定级报告介绍
    private String levelIntroduction;//等级介绍
    private String scoreRemark;//孩子成绩解读
    private String scoreIntroduction;//孩子本期成绩介绍
    private List<WeekPoint> weekPointList;
    private String levelVideo;//定级样例视频

    @Getter
    @Setter
    public static class WeekPoint implements Serializable{
        private static final long serialVersionUID = -7588093692684637171L;
//        private UGCWeekPointsEnum weekPoint;//薄弱点
        private String weekPointName;//薄弱点 定级报告名称
        private String weekPointDesc;//薄弱点中文描述
        private int weekPointLevel;//薄弱点优先级
        private String remark;//薄弱点解读
        private String promote;//薄弱点提升
        private String pushLesson;//推课
    }
}
