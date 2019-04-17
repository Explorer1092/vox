package com.voxlearning.washington.mapper.studentheadline;

import com.voxlearning.alps.core.util.StringUtils;
import lombok.Getter;
import lombok.Setter;

/**
 * 阿分题勋章相关动态处理
 *
 * @author yuechen.wang
 * @since 2018/01/15
 */
@Getter
@Setter
public class AfentiMedalHeadlineMapper extends StudentInteractiveHeadline {
    private static final long serialVersionUID = -7030077182875687765L;

    private String imgUrl;        // 勋章图标
    private String medalName;     // 勋章名称
    private String level;         // 勋章等级
    private String appName;       // 应用名称
    private String contentText;       //展示内容

    @Override
    public boolean valid() {
        return StringUtils.isNoneBlank(medalName, appName);
    }
}
