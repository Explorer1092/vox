package com.voxlearning.utopia.service.ai.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentField;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * 薯条班级统计信息
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-sochi")
@DocumentDatabase(database = "vox-chips")
@DocumentCollection(collection = "vox_chips_english_class_statistics_latest")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.fixed, value = 10800)
@UtopiaCacheRevision("20190125")
public class ChipsEnglishClassStatisticsLatest implements Serializable {

    @DocumentId
    private String id;
    @DocumentField(value = "classid")
    private Long classId;
    @DocumentField(value = "unitid")
    private String unitId;         //单元ID
    @DocumentField(value = "bookid")
    private String bookId;         //教材ID
    @DocumentField(value = "classfinishnum")
    private Integer classFinishNum;//本班完成人数
    @DocumentField(value = "classattendnum")
    private Integer classAttendNum;//本班上课人数
    @DocumentField(value = "classnum")
    private Integer classNum;      //本班人数
    @DocumentField(value = "totalfinishnum")
    private Integer totalFinishNum;//本期完成人数
    @DocumentField(value = "totalattendnum")
    private Integer totalAttendNum;//本期上课人数
    @DocumentField(value = "totalnum")
    private Integer totalNum;      //本期人数
    @DocumentField(value = "createtime")
    private String createTime;
    @DocumentField(value = "classscore")
    private Integer classScore;
    @DocumentField(value = "classactivenum")
    private Integer classActiveNum;//本班主动服务人数

    @DocumentField(value = "totalactivenum")
    private Integer totalActiveNum;//本期主动服务人数
    @DocumentField(value = "classremarknum")
    private Integer classRemarkNum;//本班完课点评量
    @DocumentField(value = "perioremarknum")
    private Integer periodRemarkNum;//本期完课点评量
    @DocumentField(value = "classnotwxnum")
    private Integer classNotwxNum;
    @DocumentField(value = "periodnotwxnum")
    private Integer periodNotwxNum;


    //缓存key
    public static String ck_class_id(Long classId) {
        return CacheKeyGenerator.generateCacheKey(ChipsEnglishClassStatisticsLatest.class, "CID", classId);
    }
}
