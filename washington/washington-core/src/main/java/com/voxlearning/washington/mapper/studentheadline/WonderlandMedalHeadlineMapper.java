package com.voxlearning.washington.mapper.studentheadline;

import com.voxlearning.alps.core.util.StringUtils;
import lombok.Getter;
import lombok.Setter;

/**
 * 成长世界勋章相关消息
 *
 * @author yuechen.wang
 * @since 2017/11/01
 */
@Getter
@Setter
public class WonderlandMedalHeadlineMapper extends StudentInteractiveHeadline {
    private static final long serialVersionUID = -7030077182875687765L;

    private String medalImg;      // 勋章图标
    private String medalName;     // 勋章名称
    private String medalRank;     // 勋章等级

    @Override
    public boolean valid() {
        return StringUtils.isNotBlank(medalImg);
    }
}
