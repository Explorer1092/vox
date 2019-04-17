package com.voxlearning.washington.mapper;

import com.voxlearning.utopia.api.constant.ParentRemindingType;
import com.voxlearning.utopia.service.vendor.api.entity.SelfStudyConfigWrapper;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Created by jiangpeng on 2016/10/18.
 */
@Getter
@Setter
public class ParentFunctionConfig {

    private String iconUrl;

//    @JsonProperty("config_name")
    private String configName;

//    @JsonProperty("config_order")
    private Integer configOrder;

//    @JsonProperty("config_type")
    private String configType;

//    @JsonProperty("config_key")
    private String configKey;   // H5  NATIVE

//    @JsonProperty("config_default_label")
    private String configDefaultLabel;

//    @JsonProperty("config_reminding_id")
    private String configRemindingId;

//    @JsonProperty("config_operation_label")
    private String configOperationLabel;

//    @JsonProperty("config_reminding_type")
    private String configRemindingType;

//    @JsonProperty("config_reminding_number")
    private Integer configRemindingNumber;

    private String configAlias; //配置别名 用来做运营消息

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

    /**
     * 开始版本 (包含当前
     */
    private String startVersion;

    /**
     * 没登录是否展示
     */
    private Boolean noLoginShow;

    /**
     * 没孩子是否展示
     */
    private Boolean noChildShow;

    /**
     * 显示的年级
     */
    private List<Integer> clazzLevels;

    /**
     * 非 灰度配置
     */
    private String reverseGreyMain;

    /**
     *  非 灰度配置 这两个灰度配置必须都有,有一个没有相当于没有
     */
    private String reverseGreySub;


    public static ParentFunctionConfig fromSelfStudyConfigWrapper(SelfStudyConfigWrapper sc) {
        ParentFunctionConfig pc = new ParentFunctionConfig();
        pc.setConfigName(sc.getToolName());
        pc.setConfigOrder(sc.getOrder());
        pc.setConfigType(sc.getToolType().name());
        pc.setConfigKey(sc.getToolKey());
        pc.setConfigOperationLabel(sc.getToolOpText());
        pc.setConfigRemindingId(sc.getToolOpId());
        pc.setConfigRemindingType(ParentRemindingType.RED_POINT.name()); //自学工具的提醒都是红点
        pc.setNoChildSupport(sc.getNoChildSupport());
        pc.setNoLoginSupport(sc.getNoLoginSupport());
        pc.setIconUrl(sc.getNativeIconUrl());
        pc.setGreyMain(sc.getGreyMain());
        pc.setGreySub(sc.getGreySub());
        pc.setClazzLevels(sc.getClazzLevels());
        pc.setReverseGreyMain(sc.getReverseGreyMain());
        pc.setReverseGreySub(sc.getReverseGreySub());
        return pc;
    }
}
