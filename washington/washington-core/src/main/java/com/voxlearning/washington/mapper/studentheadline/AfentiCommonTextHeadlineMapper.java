package com.voxlearning.washington.mapper.studentheadline;

import com.voxlearning.alps.core.util.StringUtils;
import lombok.Getter;
import lombok.Setter;

/**
 * 阿分题飞船相关动态处理
 *
 * @author 陈司南
 * @since 2018/11/30
 */
@Getter
@Setter
public class AfentiCommonTextHeadlineMapper extends StudentInteractiveHeadline {

    private static final long serialVersionUID = 8402467244177790856L;
    private Integer targetType;         // 类型  分享类型 主要用前端跳转
    private String appName;       // 应用名称
    private String contentText;   // 展示文本
    private String targetUrl;

    @Override
    public boolean valid() {
        return StringUtils.isNoneBlank(contentText);
    }

}
