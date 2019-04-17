package com.voxlearning.utopia.service.push.api.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * 标签消息
 *
 * @author Wenlong Meng
 * @since Mar 11, 2019
 */
@Setter
@Getter
public class TagMessage extends Message implements Serializable {
    private static final long serialVersionUID = -5818986133657824660L;
    private Set<String> tags;

    /**
     * 构建标签消息 see {@link Message#build(String, String, Map, String...)}
     *
     * @param source
     * @param content
     * @param extInfo
     * @param tags
     */
    TagMessage(String source, String content, Map<String, Object> extInfo, Set<String> tags) {
        this.source = source;
        this.tags = tags;
        this.content = content;
        this.extInfo = extInfo;
    }
}
