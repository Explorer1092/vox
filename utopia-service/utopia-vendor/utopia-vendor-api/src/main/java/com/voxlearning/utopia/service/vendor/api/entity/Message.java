package com.voxlearning.utopia.service.vendor.api.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * 消息
 *
 * @author Wenlong Meng
 * @since Feb 26, 2019
 */
@Setter
@Getter
public class Message implements Serializable {
    private static final long serialVersionUID = -5818986133657824660L;
    private Set<Long> userIds;
    private String content;
    private Map<String, Object> extInfo;
}
