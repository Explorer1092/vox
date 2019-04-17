package com.voxlearning.utopia.service.vendor.api.entity;

import com.voxlearning.utopia.api.constant.ParentConfigType;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * Created by jiangpeng on 16/8/8.
 */
@Data
public class SelfStudyConfigWrapper implements Serializable {
    private static final long serialVersionUID = 1148764240151691976L;

    private String toolId; //学习工具id

    private String toolName;  //学习工具名称

    private Integer order;    //顺序

    private String iconUrl;   //图标url

    private String selfStudyType;   //自学类型  见枚举 SelfStudyType

    private String toolOpId;   //运营信息id

    private String toolOpText;   //运营文案

    private ParentConfigType toolType; //功能类型 h5 还是

    private String toolKey;

    private String nativeIconUrl; //app用的图标url

    /**
     * 没有孩子是否显示（前提是已登录）
     */
    private Boolean noChildShow;


    /**
     * 没登录的时候是否显示
     */
    private Boolean noLoginShow;


    /**
     * true 没有孩子也可以用
     * false 没有孩子点击去绑孩子页面
     */
    private Boolean noChildSupport;


    /**
     * true 没登录也可以用
     * false 没有登录跳登录页
     */
    private Boolean noLoginSupport;


    /**
     * 灰度配置
     */
    private String greyMain;

    /**
     * 灰度配置 这两个灰度配置必须都有,有一个没有相当于没有
     */
    private String greySub;

    private List<Integer> clazzLevels;

    /**
     * 非 灰度配置
     */
    private String reverseGreyMain;

    /**
     *  非 灰度配置 这两个灰度配置必须都有,有一个没有相当于没有
     */
    private String reverseGreySub;


    public static SelfStudyConfigWrapper newInstance(SelfStudyBasicConfig basicConfig){
        Objects.requireNonNull(basicConfig);

        SelfStudyConfigWrapper wrapper = new SelfStudyConfigWrapper();

        wrapper.setToolId(basicConfig.getId());
        wrapper.setToolName(basicConfig.getToolName());
        wrapper.setIconUrl(basicConfig.getIconUrl());
        wrapper.setOrder(basicConfig.getOrder());
        wrapper.setSelfStudyType(basicConfig.getSelfStudyTypeEnum() == null ? "":basicConfig.getSelfStudyTypeEnum().name());
        wrapper.setNoChildShow(basicConfig.getNoChildShow());
        wrapper.setNoLoginShow(basicConfig.getNoLoginShow());
        wrapper.setNoChildSupport(basicConfig.getNoChildSupport());
        wrapper.setNoLoginSupport(basicConfig.getNoLoginSupport());
        wrapper.setNativeIconUrl(basicConfig.getNativeIconUrl());
        wrapper.setToolType(basicConfig.getToolTypeEnum());
        wrapper.setToolKey(basicConfig.getToolKey());
        wrapper.setGreyMain(basicConfig.getGreyMain());
        wrapper.setGreySub(basicConfig.getGreySub());
        wrapper.setClazzLevels(basicConfig.getClazzLevels());
        wrapper.setReverseGreyMain(basicConfig.getReverseGreyMain());
        wrapper.setReverseGreySub(basicConfig.getReverseGreySub());
        return wrapper;
    }

    public SelfStudyConfigWrapper addOperativeConfig(SelfStudyOperativeConfig operativeConfig){
        Objects.requireNonNull(operativeConfig);
        this.setToolOpId(operativeConfig.getId());
        this.setToolOpText(operativeConfig.getText());
        return this;
    }


}
