package com.voxlearning.utopia.service.parent.homework.api.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndex;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndexes;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.service.parent.homework.api.model.DoType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 作业结果信息
 *
 * @author Wenlong Meng
 * @version 20181107
 * @date 2018-11-07
 */
@Getter
@Setter
@DocumentConnection(configName = "mongo-jxt")
@DocumentDatabase(database = "vox-parent-homework-{}", dynamic = true)
@DocumentCollection(collection = "homework_result_{}", dynamic = true)
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20181111")
public class HomeworkResult implements Serializable {
    private static final long serialVersionUID = -3775876181798636377L;
    @DocumentId(autoGenerator=DocumentIdAutoGenerator.NONE)
    private String id; //ID
    private String homeworkId;//作业id
    private String subject;//学科，参照Subject
    private String actionId;//批次标识，同一批次作业相同
    private Long userId;//学生id
    private Integer grade;//年级
    private Date startTime;//开始日期，格式为yyyy-MM-dd
    private Date endTime;//完成日期，格式为yyyy-MM-dd
    private Map<String, Object> additions;//扩展信息
    private Boolean repair;//是否补做
    private Integer questionCount;//题数
    private Integer errorQuestionCount;//错题数
    private Integer doQuestionCount;//已做题数
    private Double score;//总分
    private Double userScore;//用户得分
    private String scoreLevel;//得分等级
    private Long duration;//预计耗时
    private Long userDuration;//实际耗时
    private String clientType;//来源
    private String clientName;//来源
    private String ipImei;//来源
    private Boolean finished;//true——已完成
    private String source;//来源:parent--家长通
    private String bizType;//业务类型：EXAM--同步习题；MENTAL--口算
    private Integer timeLimit;//限时标识
    private Integer doCount;//次数
    private DoType doType;//类型
    @DocumentUpdateTimestamp
    private Date updateTime; //更新日期，格式为yyyy-MM-dd
    @DocumentCreateTimestamp
    private Date createTime; //创建日期，格式为yyyy-MM-dd
    private String name;//名称

    //订正扩展属性
    private List<String> errorQIds;//错题id
    private List<Map<String, Object>> errorDiagnostics;

}
