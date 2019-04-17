package com.voxlearning.utopia.service.newhomework.api.mapper.avenger;

import com.voxlearning.alps.annotation.common.DateRangeType;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentRangeable;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkSourceType;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkTag;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * @author xuesong.zhang
 * @since 2017/6/15
 */
@Getter
@Setter
@DocumentConnection(configName = "mongo-journal")
@DocumentDatabase(database = "vox-newhomework")
@DocumentCollection(collection = "avenger_homework_{}", dynamic = true)
@DocumentRangeable(range = DateRangeType.D)
public class AvengerHomework implements Serializable {

    private static final long serialVersionUID = 1612233228215434465L;

    private String env;
    private Date timeFiled;

    @DocumentId
    private String id;
    private String homeworkId;
    private NewHomeworkType type;                               // 作业类型
    private HomeworkTag homeworkTag;                            // 作业标签
    private String sourceHomeworkId;                            // 原作业id，和类题作业类型成对出现
    private SchoolLevel schoolLevel;                            // 猜猜
    private Subject subject;                                    // 学科
    private String actionId;                                    // 在批量布置的时候一定要保持这个id一致,拼接方法:"teacherId_${批量布置时间点}"
    private String title;                                       // 作业名称

    private Integer weekRank;                                   // 第几周
    private Integer dayRank;                                    // 第几天
    private String packageId;                                   // 假期作业包id
    private Long studentId;                                     // 学生id
    private Long teacherId;                                     // 老师id
    private Long clazzGroupId;                                  // 班组id

    private Long duration;                                      // 标准时长（单位：秒）
    private String remark;                                      // 备注
    private HomeworkSourceType source;                          // 布置作业来源

    private Boolean checked;                                    // 是否检查
    private Date checkedAt;                                     // 检查时间
    private HomeworkSourceType checkHomeworkSource;             // 检查作业的端信息（大数据用）

    private Date startTime;                                     // 作业起始时间
    private Date endTime;                                       // 作业结束时间
    private Boolean disabled;                                   // 默认false，删除true
    private Boolean includeSubjective;                          // 是否包含需要主观作答的试题
    private Boolean includeIntelligentTeaching;                 // 是否包含重点讲练测
    private Boolean remindCorrection;                           // 是否已推荐巩固

    private Map<String, String> additions;                      // 扩展字段


    private Date createAt;                                      // 作业生成时间
    private Date updateAt;                                      // 作业更新时间

    private Map<ObjectiveConfigType, AvengerHomeworkPracticeContent> practices;     // 作业内容
}
