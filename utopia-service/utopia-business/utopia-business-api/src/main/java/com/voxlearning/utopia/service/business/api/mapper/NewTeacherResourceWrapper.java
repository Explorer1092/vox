package com.voxlearning.utopia.service.business.api.mapper;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.utopia.service.business.api.entity.NewTeacherResource;
import com.voxlearning.utopia.service.business.api.entity.TeachingResource;
import lombok.*;

import java.util.Date;
import java.util.List;

/**
 * 用来包装新版锦囊、课件、专题、新讲堂板块的内容
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id", "category"})
public class NewTeacherResourceWrapper implements java.io.Serializable {
    private static final long serialVersionUID = 7993281011107703887L;

    private String id;
    private String title;                               // 标题
    private String subHead;                             // 副标题

    private Subject subject;                            // 学科
    private Integer clazzLevel;                         // 年级 1~6
    private Integer termType;                           // 上下册 1\2
    private String bookId;                              // 教材
    private String bookShortPublisher;                  // 出版社(跟教材挂钩,为了便于展示冗余存一份)
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

    private Integer source;                             // 课件来源         (课件大赛、一起作业)
    private String prizeLevel;                          // 曾获奖项
    private String coursewarePrize;                     // 课件大赛奖项      (课件大赛|最具创新智慧设计作品)
    private String coursewareId;                        // 课件大赛作品ID    (便于后期对应差错)
    private Long authorId;                              // 作者             (如果为空 作者显示"小柒老师" 学校显示"一起作业")
    private List<NewTeacherResource.File> fileList;     // 资源文件列表
    private String remark;                              // 运营备注
}
