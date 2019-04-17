package com.voxlearning.utopia.service.push.api.support;

import com.voxlearning.utopia.service.push.api.constant.PushType;
import com.voxlearning.utopia.service.push.api.support.PushContext;
import lombok.Getter;
import lombok.Setter;

/**
 * @author xinxin
 * @since 14/11/2016.
 */
@Getter
@Setter
public class PushRetryContext extends PushContext {
    private String id;
    private PushType pushType;
}
