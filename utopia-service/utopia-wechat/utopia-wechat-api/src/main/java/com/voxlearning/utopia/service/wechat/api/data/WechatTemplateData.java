package com.voxlearning.utopia.service.wechat.api.data;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author songtao
 * @since 2018/6/13
 */
@Getter
@Setter
public class WechatTemplateData implements Serializable {
    private static final long serialVersionUID = -7718541201804196411L;
    private String value;
    private String color;
    public WechatTemplateData(){}

    public WechatTemplateData(String value, String color) {
        this.value = value;
        this.color = color;
    }
}
