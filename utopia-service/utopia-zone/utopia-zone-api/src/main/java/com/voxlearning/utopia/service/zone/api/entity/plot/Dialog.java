package com.voxlearning.utopia.service.zone.api.entity.plot;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author : kai.sun
 * @version : 2018-11-09
 * @description :
 **/

@Getter
@Setter
@NoArgsConstructor
public class Dialog implements Serializable {

    public static final String COORDINATE_X="X",COORDINATE_Y="Y";

    private static final long serialVersionUID = 8258262325309314285L;
    private Integer order;       //排序
    private String text;        //话语
    private String pic;         //背景图
    private String audio;       //音频文件绝对路径
    private Map<String,Double>  coordinate; //气泡坐标
    private PicEffect picEffect; //图片效果
    private Popup popup;        //话语结束对应弹框
}
