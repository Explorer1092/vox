package com.voxlearning.washington.mapper.studentheadline;

import com.voxlearning.alps.core.util.StringUtils;
import lombok.Getter;
import lombok.Setter;

/**
 * 班级BOSS相关消息
 *
 * @author yuechen.wang
 * @since 2017/11/01
 */
@Getter
@Setter
public class ClassBossHeadlineMapper extends StudentInteractiveHeadline {
    private static final long serialVersionUID = -8870708075533874144L;

    private String bossName;      // 当前boss活动名
    private Integer rank;         // 名次
    private String bossImg;       // boss活动图
    private String rankImg;       // 排名图片

    @Override
    public boolean valid() {
        return StringUtils.isNotBlank(bossImg) && StringUtils.isNotBlank(rankImg);
    }
}
