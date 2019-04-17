package com.voxlearning.utopia.service.business.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.*;

import java.util.Date;
import java.util.List;

/**
 * 用来存新版的锦囊、课件
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id", "category"})
@UtopiaCacheRevision("20190315")
@DocumentDatabase(database = "vox_o2o")
@DocumentCollection(collection = "vox_new_teacher_resource")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
public class NewTeacherResource implements CacheDimensionDocument {

    private static final long serialVersionUID = 7993281011107703887L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.OBJECT_ID)
    private String id;
    private String title;                               // 标题
    private String subHead;                             // 副标题

    private Subject subject;                            // 学科
    private Integer clazzLevel;                         // 年级 1~6
    private Integer termType;                           // 上下册 1\2
    private String bookId;                              // 教材
    private String bookShortPublisher;                  // 教材全称(本来想存出版社简称的)
    private String unitId;                              // 单元
    private String lessonId;                            // 课程

    private String desc;                                // 资源简介
    private String category;                            // 资源分类
    private List<String> label;                         // 资源标签
    private TeachingResource.WorkType workType;         // 布置作业类型
    private String task;                                // 关联任务
    private Integer validityPeriod;                     // 任务有效期
    private String image;                               // 列表页的图
    private String appImage;                            // 老师app首页图
    private String headImage;                           // 课件顶部的图
    private Boolean featuring;                          // 首页推荐
    private Long displayOrder;                          // 排序

    private Boolean online;                             // 上线状态
    private Date firstOnlineTime;                       // 第一次上线时间

    private Boolean receiveLimit;                       // 领取时是否要求认证

    private Long readCount;                             // 阅读次数
    private Long collectCount;                          // 收藏次数
    private Long participateNum;                        // 任务参与人数
    private Long finishNum;                             // 任务完成人数

    private Long score;                                 // 分数
    private Long stars;                                 // 星级
    private Long evaluateNum;                           // 评价人数

    private Integer source;                             // 课件来源         (0课件大赛、1一起作业)
    private String prizeLevel;                          // 曾获奖项
    private String coursewarePrize;                     // 课件大赛奖项      (课件大赛|最具创新智慧设计作品)
    private String coursewareId;                        // 课件大赛作品ID    (便于后期对应差错)
    private Long authorId;                              // 作者             (如果为空 作者显示"小柒老师" 学校显示"一起作业")
    private List<File> fileList;                        // 资源文件列表
    private String remark;                              // 运营备注


    @Getter
    @Setter
    @NoArgsConstructor
    public static class File implements java.io.Serializable {
        private static final long serialVersionUID = 7993281011107703887L;

        private String fileName;
        private String fileUrl;
    }

    private Boolean disabled;

    @JsonIgnore
    public boolean isDisabledTrue() {
        return disabled != null && disabled;
    }

    @DocumentCreateTimestamp
    private Date createAt;
    @DocumentUpdateTimestamp
    private Date updateAt;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                CacheKeyGenerator.generateCacheKey(NewTeacherResource.class, id)
        };
    }
}
