package com.voxlearning.utopia.service.push.api.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * 用户消息
 *
 * @author Wenlong Meng
 * @since Mar 11, 2019
 */
@Setter
@Getter
public class UserMessage extends Message implements Serializable {
    private static final long serialVersionUID = -5818986133657824660L;
    private Set<Long> userIds;

    /**
     * 构建用户消息, see {@link Message#build(String, String, Map, Long...)}
     *
     * @param source
     * @param content
     * @param extInfo
     * @param userIds
     */
    UserMessage(String source, String content, Map<String, Object> extInfo, Set<Long> userIds) {
        this.userIds = userIds;
        this.content = content;
        this.extInfo = extInfo;
        this.source = source;
    }
}
