package com.voxlearning.utopia.business.api.entity;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentField;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndex;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndexes;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author fugui.chang
 * @since 2016/9/19
 */
@Getter
@Setter
@DocumentDatabase(database = "vox-schoolmaster")
@DocumentCollection(collection = "vox_knowledge_ability_analysis")
@DocumentIndexes({
        @DocumentIndex(def = "{'schoolId':1,'dt':1,'subject':1}", background = true),
})
@UtopiaCacheExpiration
@UtopiaCacheRevision("20160919")
@DocumentConnection(configName = "mongo-schoolmaster")
public class KnowledgeAbilityAnalysis implements Serializable{
    private static final long serialVersionUID = 2115893537186047182L;

    @DocumentId private String id;
    @DocumentField("term") private Integer term; //学期
    @DocumentField("dt") private Long yearmonth ; //日期
    @DocumentField("province_id") private Long provinceId; //省id
    @DocumentField("province_name") private String provinceName;
    @DocumentField("city_id") private Long cityId; //市id
    @DocumentField("city_name") private String cityName;
    @DocumentField("county_id") private Long countyId; //区id
    @DocumentField("county_name") private String countyName;
    @DocumentField("school_id") private Long schoolId; //学校id
    @DocumentField("school_name") private String schoolName;
    @DocumentField("school_level") private Integer schoolLevel;//学校级别
    @DocumentField("grade") private Integer grade;  //年级
    @DocumentField("subject") private String subject; //学科
    @DocumentField("primary_ability") private String primaryAbility; //小学
    @DocumentField("middle_ability") private  String middleAbility; //初中
    @DocumentCreateTimestamp @DocumentField("createTime") protected Date createTime; //创建时间

    @Getter
    @Setter
    @NoArgsConstructor
    public static final class PrimaryAbility implements Serializable {
        private static final long serialVersionUID = 1115825537236087182L;
        @DocumentField("listenRate") Double listenRate;
        @DocumentField("speakRate") Double speakRate;
        @DocumentField("readRate") Double readRate;
        @DocumentField("writeRate") Double writeRate;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static final class MiddleAbility implements Serializable {
        private static final long serialVersionUID = -1215893537186944182L;
        @DocumentField("listenRate") Double listenRate;
        @DocumentField("oralRate") Double oralRate;
        @DocumentField("wordRate") Double wordRate;
        @DocumentField("readingRate") Double readingRate;
        @DocumentField("grammarRate") Double grammarRate;
    }


    public static String generateCacheKey(Long schoolId,Long dt,String subject){
        return CacheKeyGenerator.generateCacheKey(KnowledgeAbilityAnalysis.class,new String[]{"SCHOOLID","YEARMONTH","SUBJECT"},new Object[]{schoolId,dt,subject});
    }

}

