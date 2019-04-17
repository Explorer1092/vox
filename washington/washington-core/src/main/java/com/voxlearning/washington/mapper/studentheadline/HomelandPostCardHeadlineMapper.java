package com.voxlearning.washington.mapper.studentheadline;

import com.voxlearning.alps.core.util.StringUtils;
import lombok.Getter;
import lombok.Setter;

/**
 * 家园分享明信片相关消息
 *
 * @author yuechen.wang
 * @since 2018/03/19
 */
@Getter
@Setter
public class HomelandPostCardHeadlineMapper extends StudentInteractiveHeadline {
    private static final long serialVersionUID = -7030077182875687765L;

    private String cardImg;           // 明信片图片
    private String cardName;          // 明信片名称
    private String typeImg;           // 等级图片
    private String typeText;          // 等级文案
    private Integer count;            // 次数
    private String linkUrl;           // 跳转链接

    @Override
    public boolean valid() {
        return StringUtils.isNoneBlank(cardImg, linkUrl);
    }
}
