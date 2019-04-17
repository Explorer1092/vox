package com.voxlearning.utopia.service.wechat.api.mapper;

import com.voxlearning.utopia.api.constant.AppType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author Longlong Yu
 * @since 2014-10-23 20:17
 */
@Getter
@Setter
@RequiredArgsConstructor
public class PushMessage implements Serializable {

    private static final long serialVersionUID = 2723249204047242753L;

    private final AppType appType;            //reference to the enum AppType
    private final List<Long> userIds;
    private final String content;
    private final Map<String, Object> extras;

}
