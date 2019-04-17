package com.voxlearning.washington.mapper.studentheadline;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 * User: qianxiaozhi
 * Date: 2017/1/4
 * Time: 10:49
 */
@Getter
@Setter
public class StudentAchievementHeadlineV1Mapper extends StudentInteractiveHeadline {

    private static final long serialVersionUID = 1969373092177845412L;

    private String vId;                   // 虚拟id
    private Long relevantUserId;          // 获取成就的学生
    private String headIcon;              // 获取成就学生的头像
    private String name;                  // 获取成就学生的姓名
    private String headWearImg;           // 头饰
    private Boolean showBtn = Boolean.FALSE;      // 是否展示鼓励按钮
    private Boolean disabledBtn = Boolean.FALSE;  // 按钮是否灰显

    // ============ 以上是过一段时间后可以清除的字段 2017-11-14 ==================

    /**
     * 获取成就列表
     * 成就名称
     * 成就级别
     * 成就icon
     * CJId 成就对应的动态ID
     * {"name":"abc","level":"L3","icon":"http://images.17zuoye.com/achievement.icon","CJId":123456L}
     */
    private List<Map<String, Object>> achievements = new LinkedList<>();

    /**
     * 生成vid
     *
     * @param cjIds 生成规则 班级动态id asc排序 "_"拼接
     */
    public static String genVid(List<Long> cjIds) {
        if (CollectionUtils.isEmpty(cjIds)) {
            return "";
        }
        cjIds = cjIds.stream().sorted().collect(Collectors.toList());
        return StringUtils.join(cjIds, "_");
    }
}
