package com.voxlearning.utopia.service.vendor.api.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.voxlearning.utopia.api.constant.ParentConfigType;
import com.voxlearning.utopia.api.constant.SelfStudyType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author jiangpeng
 * @since 16/8/4.
 */
@Setter
@Getter
public class SelfStudyBasicConfig {

    private String id;

    private String toolName;

    /**
     * 老版本H5页面用的图标
     */
    private String iconUrl;

    /**
     * 原生用的图标
     */
    private String nativeIconUrl;

    private Integer order;

    @JsonProperty("status")
    private Status statusEnum;

    @JsonProperty("selfStudyType")
    private SelfStudyType selfStudyTypeEnum;

    @JsonProperty("toolType")
    private ParentConfigType toolTypeEnum;
    /**
     * functionTypeEnum 为h5时 这个是url
     * functionTypeEnum 为原生时, 这个是跳转的功能key
     */
    private String toolKey;

    /**
     * 从哪个版本开始出现
     */
    private String startVersion;

    /**
     * 支持哪一端 对应 com.voxlearning.utopia.service.vendor.api.SelfStudyConfigLoader.RequestSource
     */
    private List<String> requestSources;

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

    /**
     * 年级配置
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







//    public static void main(String[] args) {
//        SelfStudyBasicConfig t = new SelfStudyBasicConfig();
//        t.setId("1");
//        t.setToolName("随声听");
//        t.setSelfStudyTypeEnum(SelfStudyType.WALKMAN_ENGLISH);
//        t.setIconUrl("http://112312121l3123123");
//        t.setOrder(1);
//        t.setStatusEnum(Status.ONLINE);
//
//        System.out.println(JSON.toJSON(t));
//    }

    public enum Status {
        ONLINE,
        OFFLINE
    }

}
