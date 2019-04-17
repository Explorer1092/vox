package com.voxlearning.utopia.api.constant;

import com.voxlearning.alps.spi.queue.MessageTransformer;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by jiangpeng on 2016/10/19.
 */
@Getter
@Setter
public class MySelfStudyActionEvent implements Serializable, MessageTransformer {

    private static final long serialVersionUID = 2556508128232254374L;
    private Long userId;

    private SelfStudyType selfStudyType;

    private MySelfStudyActionType mySelfStudyActionType;

    private Map<String, Object> attributes = new LinkedHashMap<>();

    public MySelfStudyActionEvent(){}
    public MySelfStudyActionEvent(Long studentId, SelfStudyType selfStudyType, MySelfStudyActionType mySelfStudyActionType){
        this.userId = studentId;
        this.selfStudyType = selfStudyType;
        this.mySelfStudyActionType = mySelfStudyActionType;
    }

}
