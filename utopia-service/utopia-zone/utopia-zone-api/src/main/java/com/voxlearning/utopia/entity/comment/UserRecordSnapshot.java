package com.voxlearning.utopia.entity.comment;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * 班级新鲜事用户信息
 * Created by Yuechen.Wang on 17/11/06.
 */

@Getter
@Setter
@EqualsAndHashCode(of = {"userId"})
public class UserRecordSnapshot implements Serializable, Comparable<UserRecordSnapshot> {

    private static final long serialVersionUID = 6977009343958212295L;

    private Long userId;        // 用户ID
    private String userName;    // 用户姓名
    private String comment;     // 评论内容
    private Date createTime;    // 评论/点赞时间

    private Map<String, Object> extInfo; // 其他补充信息

    public UserRecordSnapshot() {
    }

    public UserRecordSnapshot(Long userId) {
        this.userId = userId;
        this.createTime = new Date();
    }

    public UserRecordSnapshot(Long userId, String comment) {
        this.userId = userId;
        this.comment = comment;
        this.createTime = new Date();
    }

    public void appendExtInfo(String key, Object value) {
        if (extInfo == null) {
            extInfo = new HashMap<>();
        }
        if (StringUtils.isBlank(key) || value == null) {
            return;
        }
        extInfo.put(key, value);
    }

    public Map<String, Object> snapshot() {
        Map<String, Object> info = new HashMap<>();
        if (extInfo != null && !extInfo.isEmpty()) {
            info.putAll(extInfo);
        }

        // 避免关键信息覆盖
        info.put("userId", userId);
        info.put("userName", userName);
        info.put("userComment", comment);
        info.put("createTime", createTime);
        return info;
    }

    public boolean valid() {
        return userId != null && userId > 0L;
    }

    public boolean validComment() {
        return userId != null && userId > 0L && StringUtils.isNotBlank(comment);
    }

    @Override
    public int compareTo(@SuppressWarnings("NullableProblems") UserRecordSnapshot that) {
        return this.createTime.compareTo(that.createTime);
    }

    public String extValueString(String key) {
        return SafeConverter.toString(extValue(key));
    }

    public Integer extValueInt(String key) {
        return SafeConverter.toInt(extValue(key));
    }

    public Long extValueLong(String key) {
        return SafeConverter.toLong(extValue(key));
    }

    private Object extValue(String key) {
        if (extInfo == null) {
            return null;
        }
        return extInfo.get(key);
    }

}