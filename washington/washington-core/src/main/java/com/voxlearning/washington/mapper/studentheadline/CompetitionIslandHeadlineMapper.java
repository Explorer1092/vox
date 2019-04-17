package com.voxlearning.washington.mapper.studentheadline;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalType;
import lombok.Getter;
import lombok.Setter;

/**
 * 竞技岛相关消息
 *
 * @author yuechen.wang
 * @since 2017/11/01
 */
@Getter
@Setter
public class CompetitionIslandHeadlineMapper extends StudentInteractiveHeadline {
    private static final long serialVersionUID = 5294086525776753586L;

    private String rank;             // 排名
    private Integer credit;          // 竞技积分
    private String levelUpImg;       // 晋升段位图片

    @Override
    public boolean valid() {
        return !ClazzJournalType.COMPETITION_ISLAND_LEVEL_UP.name().equals(getType())
                || StringUtils.isNotBlank(levelUpImg);
    }
}
