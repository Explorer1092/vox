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
public class AfentiRankHeadlineMapper extends StudentInteractiveHeadline {
    private static final long serialVersionUID = -7030077182875687765L;

    private String className;     // 班级名称
    private String prodName;      // 应用名称
    private String unitName;      // 单元名称
    private String shareType;     // 分享类型
    private Boolean onList;       // 榜上有名
    private Integer rank;         // 班级名次
    private String linkUrl;       // 跳转链接

    @Override
    public boolean valid() {
        return StringUtils.isNoneBlank(className, prodName, shareType, linkUrl);
    }
}
