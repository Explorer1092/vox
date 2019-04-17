package com.voxlearning.utopia.entity.activity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlPrimaryKeyGeneratorType;
import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

/**
 * 七巧板活动学生信息
 * Created by Wang Yuechen on 2017/12/08.
 */
@Getter
@Setter
@DocumentConnection(configName = "hs_misc")
@DocumentTable(table = "VOX_TANGRAM_ACTIVITY_STUDENT")
@UtopiaCacheRevision("171215")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class TangramActivityStudent implements CacheDimensionDocument {
    private static final long serialVersionUID = -2237659722551908121L;

    public static final int STUDENT_MAXIMUM_LIMIT = 50;
    public static final int MASTERPIECE_MAXIMUM_LIMIT = 3;

    @UtopiaSqlColumn(name = "ID", primaryKey = true, primaryKeyGeneratorType = UtopiaSqlPrimaryKeyGeneratorType.AUTO_INC)
    private Long id;

    @UtopiaSqlColumn(name = "TEACHER_ID") private Long teacherId;         // 参与活动老师ID
    @UtopiaSqlColumn(name = "SCHOOL_ID") private Long schoolId;           // 老师学校ID
    @UtopiaSqlColumn(name = "STUDENT_NAME") private String studentName;   // 学生姓名
    @UtopiaSqlColumn(name = "STUDENT_CODE") private String studentCode;   // 学生编号
    @UtopiaSqlColumn(name = "GRADE_NAME") private String gradeName;       // 年级名称
    @UtopiaSqlColumn(name = "CLASS_NAME") private String className;       // 班级名称

    @UtopiaSqlColumn(name = "MASTERPIECE_1") private String masterpiece1;  // 上传作品1
    @UtopiaSqlColumn(name = "MASTERPIECE_2") private String masterpiece2;  // 上传作品2
    @UtopiaSqlColumn(name = "MASTERPIECE_3") private String masterpiece3;  // 上传作品3

    @UtopiaSqlColumn(name = "SCORE") private String score;           // 作品打分
    @UtopiaSqlColumn(name = "COMMENT") private String comment;       // 作品评价
    @UtopiaSqlColumn(name = "AUDITOR_ID") private String auditor;    // 打分人ID
    @UtopiaSqlColumn(name = "AUDIT_TIME") private Date auditTime;    // 最近打分时间

    @DocumentCreateTimestamp @UtopiaSqlColumn(name = "CREATE_TIME") private Date createTime;
    @DocumentUpdateTimestamp @UtopiaSqlColumn(name = "UPDATE_TIME") private Date updateTime;

    @UtopiaSqlColumn(name = "DISABLED") private Boolean disabled;   // 删除状态

    public static TangramActivityStudent newInstance(Long schoolId,
                                                     Long teacherId,
                                                     String studentName,
                                                     String studentCode,
                                                     String className,
                                                     List<String> masterpieces) {
        TangramActivityStudent student = new TangramActivityStudent();

        student.schoolId = schoolId;
        student.teacherId = teacherId;
        student.studentName = studentName;
        student.studentCode = studentCode;
        student.gradeName = ClazzLevel.FOURTH_GRADE.name();
        student.className = className;
        student.score = Score.UNTITLED.name();
        student.updateMasterpieces(masterpieces);
        student.disabled = false;

        return student;
    }

    public void updateMasterpieces(List<String> masterpieces) {
        if (masterpieces == null || masterpieces.isEmpty()) {
            return;
        }
        Iterator<String> it = masterpieces.iterator();
        masterpiece1 = it.hasNext() ? it.next() : "";
        masterpiece2 = it.hasNext() ? it.next() : "";
        masterpiece3 = it.hasNext() ? it.next() : "";
    }

    /**
     * 检查各项参数
     */
    public MapMessage check() {
        // 检查学校
        if (schoolId == null || schoolId <= 0L) {
            return MapMessage.errorMessage("无效的学校信息");
        }

        // 检查老师
        if (teacherId == null || teacherId <= 0L) {
            return MapMessage.errorMessage("无效的老师信息");
        }

        // 检查班级
        if (StringUtils.isBlank(className)) {
            return MapMessage.errorMessage("请输入班级");
        }
        if (!className.matches("[0-9]{1,2}")) {
            return MapMessage.errorMessage("班级不符合规范，请重新输入");
        }

        // 检查姓名
        if (StringUtils.isBlank(studentName)) {
            return MapMessage.errorMessage("请输入学生姓名");
        }
        if (studentName.length() > 10) {
            return MapMessage.errorMessage("姓名过长，请重新输入");
        }

        // 检查编号
        if (StringUtils.isBlank(studentCode)) {
            return MapMessage.errorMessage("请输入学生编号");
        }
        if (studentCode.length() > 20) {
            return MapMessage.errorMessage("编号超过20位，请重新输入");
        }

        // 检查年级
        if (clazzLevel() == null) {
            return MapMessage.errorMessage("无效的年级名称");
        }

        // 检查作品
        if (masterpieces().isEmpty()) {
            return MapMessage.errorMessage("请上传完整学生作品图片");
        }

        return MapMessage.successMessage();
    }

    public ClazzLevel clazzLevel() {
        try {
            return ClazzLevel.valueOf(gradeName);
        } catch (Exception ignored) {
            return null;
        }
    }

    public String gradeName() {
        ClazzLevel grade = clazzLevel();
        return grade == null ? "四年级" : grade.getDescription();
    }

    public String classFullName() {
        return gradeName() + className + "班";
    }

    public List<String> masterpieces() {
        List<String> masterpieces = new ArrayList<>();
        if (StringUtils.isNotBlank(masterpiece1)) masterpieces.add(masterpiece1);
        if (StringUtils.isNotBlank(masterpiece2)) masterpieces.add(masterpiece2);
        if (StringUtils.isNotBlank(masterpiece3)) masterpieces.add(masterpiece3);
        return masterpieces;
    }

    public Map<String, Object> snapshot() {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("studentId", id);
        snapshot.put("studentName", studentName);
        snapshot.put("studentCode", studentCode);
        snapshot.put("className", className);
        snapshot.put("masterpieces", masterpieces());
        return snapshot;
    }

    public boolean alreadyJudged() {
        return Score.UNTITLED != Score.parse(score) && StringUtils.isNotBlank(auditor);
    }

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey("T", teacherId),
                newCacheKey("S", schoolId),
        };
    }

    @JsonIgnore
    public boolean isDisabledTrue() {
        return Boolean.TRUE.equals(disabled);
    }

    public enum Score {
        UNTITLED, A, B, C, D, E;

        public static final Map<String, Score> scoreMap;

        static {
            scoreMap = new LinkedHashMap<>();
            for (Score score : Score.values()) {
                scoreMap.put(score.name(), score);
            }
        }

        public static Score parse(String score) {
            return scoreMap.getOrDefault(score, Score.UNTITLED);
        }
    }

}
