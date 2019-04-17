package com.voxlearning.utopia.service.parent.homework.api.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndex;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndexes;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 作业结果详情
 *
 * @author Wenlong Meng
 * @version 20181107
 * @date 2018-11-07
 */
@Getter
@Setter
@DocumentConnection(configName = "mongo-jxt")
@DocumentDatabase(database = "vox-parent-homework-{}", dynamic = true)
@DocumentCollection(collection = "homework_process_result_{}", dynamic = true)
@DocumentIndexes({
        @DocumentIndex(def = "{'homeworkResultId':1}", background = true)
})
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20181111")
public class HomeworkProcessResult implements Serializable {
    private static final long serialVersionUID = -3775876186096336377L;
    @DocumentId
    private String id; //ID
    private String homeworkResultId;//作业结果id
    private String homeworkTag;//作业标签，参照HomeworkTag
    private String type;//作业类型，参照NewHomeworkType
    private String homeworkId;//作业id
    private Long userId;//学生id
    private Integer grade;//年级
    private String bookId;//教材id
    private String unitId;//单元id
    private String questionId;//题id
    private String questionDocId;//题docId
    private Integer questionVersion;//试题Version
    private Double score;//总分
    private Double userScore;//用户得分
    private Boolean right;//是否全对
    private List<List<Boolean>> userSubGrasp; // 作答区域的掌握情况
    private List<List<String>> userAnswers; // 用户答案
    private List<List<String>> answers; // 正确答案
    private List<Double> userSubScore;//用户得分明细
    private Long duration;//实际耗时,单位秒
    private String subject;//学科，参照Subject
    private String objectiveConfigType;//作业形式，参照ObjectiveConfigType
    private Map<String, String> additions;//扩展字段
    @DocumentUpdateTimestamp
    private Date updateTime; //更新日期，格式为yyyy-MM-dd
    @DocumentCreateTimestamp
    private Date createTime; //创建日期，格式为yyyy-MM-dd

    /**
     * homeworkResultId cache key
     *
     * @return
     */
    public String ckHomeworkResultId(){
        return CacheKeyGenerator.generateCacheKey(HomeworkProcessResult.class,"homeworkResultId",homeworkResultId);
    }

}
