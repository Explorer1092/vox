package com.voxlearning.utopia.service.wechat.api.data;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author guangqing
 * @since 2019/1/13
 */
@Getter
@Setter
public class CreateQrcodeReq implements Serializable{

    private static final long serialVersionUID = -4230547380247697361L;
    private String sceneStr;
    private int createQrcodeType;//CreateQrcodeType 对应的type值
    private long sceneId;
    private long expireSeconds;

}
