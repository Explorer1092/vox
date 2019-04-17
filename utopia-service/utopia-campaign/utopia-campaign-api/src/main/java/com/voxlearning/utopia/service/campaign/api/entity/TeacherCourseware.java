package com.voxlearning.utopia.service.campaign.api.entity;

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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@DocumentConnection(configName = "mongo-crm")
@DocumentDatabase(database = "vox-activity")
@DocumentCollection(collection = "vox_teacher_courseware")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20181111")
public class TeacherCourseware implements Serializable {
    private static final long serialVersionUID = 6989490205718509539L;
    @DocumentId private String id;
    private String title;
    private Subject subject;
    private Integer clazzLevel;
    private Integer termType;
    private String bookId;
    private String unitId;
    private String description;
    /**
     * ppt 信息
     */
    private String coursewareFile;
    private String coursewareFileName;
    private List<String> coursewareFilePreview;

    // zip 中的 ppt 信息
    private String pptCoursewareFile;
    private String pptCoursewareFileName;

    private Long teacherId;
    private String teacherName;
    private Status status;
    private Integer totalScore;
    private Integer commentNum;
    private Integer visitNum;
    private String examiner;
    private Date examineUpdateTime;
    private ExamineStatus examineStatus;
    private String examineExt;

    // 拉票阶段的投票数量
    private Integer canvassNum;
    // 被人帮忙拉票的次数
    private Integer canvassHelperNum;

    /**
     * 教材版本信息
     */
    private String serieId;
    private String serieName;
    /**
     * 课程信息
     */
    private String lessonId;
    private String lessonName;
    /**
     * 图片信息
     */
    private Date pictureUpdateTime;
    private List<Map<String,String>> picturePreview;

    /**
     * 奖项信息
     */
    private String awardLevelName;
    private Integer awardLevelId;
    private String awardIntroduction;
    private Date awardPictureTime;
    private List<Map<String,String>> awardPicturePreview;
    /**
     * 封面信息
     */
    private Date coverUpdateTime;
    private String coverUrl;
    private String coverName;
    /**
     * word 文档信息
     */
    private Date wordUpdateTime;
    private String wordUrl;
    private String updatedWordUrl;
    private String wordName;
    private List<String> wordFilePreview;
    /**
     * 压缩文件信息
     */
    private Date compressedFileUpdateTime;
    private String compressedFileUrl;
    private String compressedFileName;

    private String zipFileUrl;

    /**
     * 是否需要重新打包
     */
    private Boolean needPackage;

    private Boolean isUserUpload;

    private Integer downloadNum;

    private Boolean disabled;
    private Map<String,Integer> labelInfo;
    private Map<Integer,Integer> authenticatedStarInfo;
    private Map<Integer,Integer> generalStarInfo;
    @DocumentCreateTimestamp private Date createTime;
    @DocumentUpdateTimestamp private Date updateTime;

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public enum Status {
        DRAFT("未提交"), EXAMINING("审核中"), PUBLISHED("已发布"), REJECTED("被退回");
        @Getter
        private final String description;
    }


    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public enum ExamineStatus {
        WAITING("待审核"), EXAMINING("审核中"), PASSED("通过"), FAILED("驳回");
        @Getter
        private final String description;
    }

    public static String ck_teacher(Long teacherId) {
        return CacheKeyGenerator.generateCacheKey(TeacherCourseware.class,
                new String[]{"TID"},
                new Object[]{teacherId});
    }


    public static String ck_Id(String id) {
        return CacheKeyGenerator.generateCacheKey(TeacherCourseware.class, id);
    }

}
