package com.voxlearning.utopia.service.push.api.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 消息基类
 *
 * @author Wenlong Meng
 * @since Mar 11, 2019
 */
@Setter
@Getter
public abstract class Message implements Serializable {
    private static final long serialVersionUID = -5818986133657824660L;
    String content;
    Map<String, Object> extInfo;
    String source;

    /**
     * 构建用户消息
     *
     * @param source
     * @param content
     * @param extInfo
     * @param userIds
     * @return
     */
    public static Message build(String source, String content, Map<String, Object> extInfo, Long... userIds){
        return new UserMessage(source, content, extInfo, Arrays.stream(userIds).collect(Collectors.toSet()));
    }

    /**
     * 构建标签消息
     *
     * @param source
     * @param content
     * @param extInfo
     * @param tags
     * @return
     */
    public static Message build(String source, String content, Map<String, Object> extInfo, String... tags){
        return new TagMessage(source, content, extInfo, Arrays.stream(tags).collect(Collectors.toSet()));
    }
}
