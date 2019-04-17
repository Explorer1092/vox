package com.voxlearning.washington.mapper.studentheadline;

import com.voxlearning.alps.core.util.StringUtils;
import lombok.Getter;
import lombok.Setter;

/**
 * 图文分享相关消息
 *
 * @author haitian.gan
 * @since 2018/06/08
 */
@Getter
@Setter
public class ImageTextShareHeadlineMapper extends StudentInteractiveHeadline {
    private static final long serialVersionUID = -7030077182875687765L;

    private String imageUrl;          // 背景图片
    private String content;           // 文字内容
    private String linkUrl;           // 跳转链接
    private String title;             // 标题

    @Override
    public boolean valid() {
        return StringUtils.isNoneBlank(imageUrl, linkUrl, content);
    }
}
