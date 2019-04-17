package com.voxlearning.utopia.service.push.api.support;

import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.push.api.constant.PushTargetType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * @author xinxin
 * @since 10/11/2016
 */
@Setter
@Getter
public class PushContext implements Serializable {
    private static final long serialVersionUID = 6638931339964119972L;

    /**
     * see {@link PushTargetType}
     */
    private String targetType;
    private Set<Long> aliases;
    private Map<String, Object> filter; //筛选条件

    private String ticker;  //必填，通知栏提示文字
    private String title;   //必填，通知标题
    private String content; //必填，通知内容
    private Integer duration;   //选填，发送时长
    private Map<String, Object> extInfo;    //push需要的扩展字段
    private Map<String, Object> clientInfo;  //客户端使用的自定义参数

    private AppMessageSource source;
}
