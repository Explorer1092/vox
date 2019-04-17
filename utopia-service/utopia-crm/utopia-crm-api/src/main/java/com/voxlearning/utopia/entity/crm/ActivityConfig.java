package com.voxlearning.utopia.entity.crm;


import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.data.ActivityBaseRule;
import com.voxlearning.utopia.data.ActivityReport;
import com.voxlearning.utopia.enums.ActivityTypeEnum;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.*;

/**
 * 活动配置
 */
@Getter
@Setter
@DocumentConnection(configName = "mongo-crm")
@DocumentDatabase(database = "vox-crm")
@DocumentCollection(collection = "vox_activity_config")
@UtopiaCacheRevision("20181215")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class ActivityConfig implements Serializable, CacheDimensionDocument {

    private static final long serialVersionUID = -4775869009469002151L;

    public static final Integer ROLE_AGENT = 0;
    public static final Integer ROLE_TEACHER = 1;
    public static final Integer ROLE_DEV = 2;

    @DocumentId
    private String id;

    private ActivityTypeEnum type; // 活动类型

    private String title; // 标题

    private String description; // 描述

    private String image; // 配置图片 直接读取对应游戏的图片

    private Date startTime; // 活动开始时间

    private Date endTime; // 活动结束时间

    private ActivityBaseRule rules; // 游戏规则

    private String source; // 来源

    private List<Long> schoolIds; // 学校id

    private List<Long> areaIds; // 区域id

    private List<Integer> clazzLevels; // 年级

    private List<Long> clazzIds; // 班级id

    private String proveImg;        // 证明图 区级活动必填

    private Set<Subject> subjects;  // 开放学科 UNKNOWN 代表不限制

    private Integer status; // 1 未审核 2 审核通过 3 审核拒绝

    private String rejectReason;  // 拒绝原因

    private Long applicant; // 申请人

    private String email;   // 申请人邮箱

    private String auditor; // 审核人

    private Date auditorTime; // 审核时间

    private ActivityReport report; // 报告

    private Boolean noticeStatus; // 是否已经发过通知

    private Boolean disabled;

    private Integer applicantRole; // 申请人身份 0市场人员 1老师 2开发

    @DocumentCreateTimestamp
    private Date createTime;

    @DocumentUpdateTimestamp
    private Date updateTime;

    public boolean isStarting(Date now) {
        if (status == null || endTime == null || startTime == null) {
            return false;
        }
        return status == 2 && now.compareTo(endTime) <= 0 && now.compareTo(startTime) >= 0;
    }

    public boolean isUnStart(Date now) {
        if (status == null || endTime == null || startTime == null) {
            return false;
        }
        return (status != 2 && now.compareTo(endTime) <= 0) || now.compareTo(startTime) <= 0;
    }

    public boolean isEnd(Date now) {
        return now.after(endTime);
    }

    public Set<Subject> fetchSubjects() {
        if (this.subjects == null || this.subjects.isEmpty()) {
            return Collections.singleton(Subject.MATH);
        }
        return this.subjects;
    }

    public String fetchSubjectDesc() {
        StringBuilder sb = new StringBuilder();
        Set<Subject> subjects = fetchSubjects();
        for (Subject subject : subjects) {
            if (subject == Subject.UNKNOWN) {
                sb.append("全学科").append("、");
            } else {
                sb.append(subject.getValue()).append("、");
            }
        }
        String subjectName = sb.toString();
        return subjectName.substring(0, subjectName.lastIndexOf("、"));
    }

    public boolean limitMath() {
        Set<Subject> subjects = fetchSubjects();
        return subjects.contains(Subject.MATH);
    }

    /**
     * 是否是老师布置的
     */
    public boolean hasTeacher() {
        return Objects.equals(ROLE_TEACHER, applicantRole);
    }

    /**
     * 是否是开发布置的
     */
    public boolean hasDev() {
        return Objects.equals(ROLE_DEV, applicantRole);
    }

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(ActivityConfig.class, id);
    }

    public static String ck_applicant_role(Long applicant,Integer applicantRole) {
        if (applicantRole == null) applicantRole = ROLE_AGENT; // 难受的兼容
        return CacheKeyGenerator.generateCacheKey(ActivityConfig.class, new String[]{"AID", "AR"}, new Object[]{applicant, applicantRole});
    }

    public static List<String> ck_classIds(List<Long> clazzIds) {
        List<String> keys = new ArrayList<>();
        if (clazzIds != null) {
            clazzIds.forEach(cid -> keys.add(CacheKeyGenerator.generateCacheKey(ActivityConfig.class, new String[]{"CID"}, new Object[]{cid})));
        }
        return keys;
    }

    @Override
    public String[] generateCacheDimensions() {
        List<String> keys = new ArrayList<>();
        keys.add(ck_id(id));
        keys.add(ck_applicant_role(applicant, applicantRole));
        keys.addAll(ck_classIds(clazzIds));
        return keys.toArray(new String[]{});
    }
}
