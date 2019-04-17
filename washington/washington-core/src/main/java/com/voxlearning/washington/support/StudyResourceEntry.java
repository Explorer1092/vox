package com.voxlearning.washington.support;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.voxlearning.alps.annotation.meta.Subject;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author feng.guo
 * @since 2019-01-17
 */
@Getter
@Setter
public class StudyResourceEntry implements Serializable {

    private static final long serialVersionUID = -7222473414148604540L;
    /**
     * 名称
     */
    private String name;
    /**
     * 科目
     */
    private Subject subject;
    /**
     * icon地址
     */
    @JsonProperty("icon_url")
    private String iconUrl;
    /**
     * 应用名称
     */
    private String mainTitle;
    /**
     * 应用副标题
     */
    private String subheading;
    /**
     * 在线人数
     */
    @JsonProperty("use_nums")
    private String userNum;

    @JsonProperty("function_type")
    private StudyEntry.FunctionType functionType;

    @JsonProperty("function_key")
    private String functionKey;

    /**
     * 是否显示或者隐藏应用
     */
    private boolean display = false;

    public StudyResourceEntry touchFunctionTypeH5() {
        this.functionType = StudyEntry.FunctionType.H5;
        return this;
    }

    public StudyResourceEntry touchFunctionTypeNative() {
        this.functionType = StudyEntry.FunctionType.NATIVE;
        return this;
    }

    public StudyResourceEntry touchFunctionKey(StudyEntry.FunctionKey functionKey) {
        this.functionKey = functionKey.name();
        return this;
    }

    public StudyResourceEntry touchFunctionKey(String h5Url) {
        this.functionKey = h5Url;
        return this;
    }
}
