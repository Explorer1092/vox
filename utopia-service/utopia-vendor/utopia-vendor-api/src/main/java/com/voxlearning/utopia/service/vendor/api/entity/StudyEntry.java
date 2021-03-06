package com.voxlearning.utopia.service.vendor.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.api.constant.SelfStudyType;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.service.user.api.mappers.NewAdMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 190首页我的学习入口
 *
 * @author jiangpeng
 * @since 2017-05-15 下午12:19
 **/
@Getter
@Setter
@Deprecated
public class StudyEntry implements Serializable {
    private static final long serialVersionUID = 7060267754609147950L;

    private String name;

    @JsonIgnore
    private EntryType entryType;

    @JsonIgnore
    private ActivityPosition activityPosition;

    @JsonProperty("self_study_type")
    private SelfStudyType selfStudyType;

    private String label;

    /**
     * 底色色值
     */
    @JsonProperty("label_color")
    private String labelColor = LabelColor.NORMAL.getColor();

    /**
     * 字体颜色
     *
     * @since v1.9.5
     */
    @JsonProperty("label_text_color")
    private String labelTextColor = LabelColor.BLACK.getColor();

    @JsonProperty("function_type")
    private FunctionType functionType;

    @JsonProperty("function_key")
    private String functionKey;

    private Map<String, Object> extra = new HashMap<>();

    @JsonProperty("icon_url")
    private String iconUrl;

    /**
     * 背景图片
     */
    @JsonProperty("back_img_url")
    private String backImgUrl;

    @JsonProperty("label_reminder")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LabelReminder labelReminder;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Tag tag;

    /**
     * 底部文案，目前就是共有 xx 个同班同学使用
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("bottom_text")
    private String bottomText;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("dot_id")
    private String dotId;

    /**
     * 直播专用的人数
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("watch_count")
    private String watchCount;


    /**
     * 运营推荐文字 如 北师大附小老师推荐
     *
     * @since v2.2.2
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("recommend_text")
    private String recommendText;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Category category;

    public static StudyEntry newInstance(SelfStudyType selfStudyType, String version) {
        if (selfStudyType == null) {
            return null;
        }
        StudyEntry studyEntry = new StudyEntry();
        studyEntry.setName(selfStudyType.getDesc());
        studyEntry.setIconUrl(selfStudyType.getIconUrl());
        studyEntry.setBackImgUrl(selfStudyType.getBackImgUrl());
        studyEntry.setSelfStudyType(selfStudyType);
        studyEntry.setEntryType(EntryType.STUDY);
        if (StringUtils.isNotBlank(version) && VersionUtil.compareVersion(version, "2.0.5.0") > 0) {
            studyEntry.setLabelTextColor(LabelColor.GRAY.getColor());
        }
        return studyEntry;
    }

    public static StudyEntry newInstance(ActivityPosition activityPosition, NewAdMapper newAdMapper) {
        if (activityPosition == null) {
            return null;
        }
        StudyEntry studyEntry = new StudyEntry();
        studyEntry.setName(newAdMapper.getName());
        studyEntry.setIconUrl(newAdMapper.getImg());
        studyEntry.setSelfStudyType(SelfStudyType.UNKNOWN);
        studyEntry.setEntryType(EntryType.ACTIVITY);
        studyEntry.setFunctionType(FunctionType.H5);
        studyEntry.setFunctionKey(newAdMapper.getUrl());
        studyEntry.setLabel(newAdMapper.getDescription());
        studyEntry.setBottomText(newAdMapper.getContent());
        return studyEntry;
    }

    public StudyEntry touchFunctionType(FunctionType functionType) {
        this.functionType = functionType;
        return this;
    }

    public StudyEntry touchFunctionKey(FunctionKey functionKey) {
        this.functionKey = functionKey.name();
        return this;
    }

    public StudyEntry touchFunctionKey(String h5Url) {
        this.functionKey = h5Url;
        return this;
    }

    public StudyEntry touchLabelColor(LabelColor labelColor) {
        this.labelColor = labelColor.getColor();
        return this;
    }

    public StudyEntry touchLabelTextColor(LabelColor labelColor) {
        this.labelTextColor = labelColor.getColor();
        return this;
    }

    public StudyEntry toucheTag(Tag tag) {
        this.tag = tag;
        return this;
    }

    public StudyEntry touchGlobalMsg(MySelfStudyEntryGlobalMsg mySelfStudyEntryGlobalMsg) {
        this.labelReminder = new LabelReminder();
        this.labelReminder.setId(mySelfStudyEntryGlobalMsg.getReminderId());
        this.labelReminder.setText(mySelfStudyEntryGlobalMsg.getText());
        this.labelReminder.setColor(LabelColor.PINK.getColor());
        this.labelReminder.setTextColor(LabelColor.RED.getColor());
        return this;
    }

    public StudyEntry touchLabelReminder(String text, String reminderId) {
        this.labelReminder = new LabelReminder();
        this.labelReminder.setId(reminderId);
        this.labelReminder.setText(text);
        this.labelReminder.setColor(LabelColor.PINK.getColor());
        this.labelReminder.setTextColor(LabelColor.RED.getColor());
        return this;
    }

    public enum FunctionType {
        NATIVE, H5
    }

    public enum EntryType {
        ACTIVITY,
        STUDY,;
    }

    @AllArgsConstructor
    public enum ActivityPosition {
        FIRST("220108"),
        SECOND("220109"),
        THIRD("220110"),
        BACKUP("220107"),;
        @Getter
        private String adSlotId;
    }

    public enum FunctionKey {
        PIC_LISTEN(""),
        LOGIN(""),
        FAIRYLAND_APP(""),
        OFFICIAL_ACCOUNT(""),
        DUBBING("2.0.3.0"),
        BIND_CHILD("2.0.3.0"),
        FRIESENGLISH("2.2.4.0");

        /**
         * 壳开始支持的版本 ""标示从开始就支持
         */
        private String startVersion;

        FunctionKey(String startVersion) {
            this.startVersion = startVersion;
        }

        /**
         * 跟当前native 版本比较，如果低于当前版本，说明壳不支持，返回 false
         *
         * @param ver
         * @return
         */
        public boolean versionCheck(String ver) {
            if (StringUtils.isBlank(startVersion))
                return true;
            if (VersionUtil.compareVersion(ver, startVersion) >= 0)
                return true;
            return false;
        }

        public static FunctionKey of(String name) {
            try {
                return FunctionKey.valueOf(name);
            } catch (Exception e) {
                return null;
            }
        }
    }

    @Getter
    public enum LabelColor {
        NORMAL("#333333"),
        PINK("#FF8971"),
        BLACK("#98A4C7"),
        RED("#FC6C4F"),
        WHITE("#FFFFFF"),
        GRAY("#878e9f"),
        ORANGE("#FF8A5F"),
        YELLOW("#FFC550"),
        YELLOW_V3("#D48A1C"),
        BLUE("#4EABEA");

        private String color;

        LabelColor(String color) {
            this.color = color;
        }
    }

    @Getter
    @Setter
    private class LabelReminder {
        private String text;
        /**
         * 底色色值
         */
        private String color;
        /**
         * 195版本开始文字色值
         */
        @JsonProperty("text_color")
        private String textColor;
        private String id;
    }

    @Getter
    public enum Tag {
        free,  //免费
        fresh, //新
        payed, //已付费
        rec,    //推荐
        renew   //续费
    }


    public enum Category {
        英语,
        语文,
        数学,
        百科,
        动物,
        科学
    }


}
