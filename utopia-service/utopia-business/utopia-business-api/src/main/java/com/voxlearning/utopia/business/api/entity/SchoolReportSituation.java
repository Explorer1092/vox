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
@DocumentCollection(collection = "vox_school_situtation")
@DocumentIndexes({
        @DocumentIndex(def = "{'school_id':1,'dt':1}", background = true),
})
@UtopiaCacheExpiration
@UtopiaCacheRevision("20160919")
@DocumentConnection(configName = "mongo-schoolmaster")
public class SchoolReportSituation implements Serializable {
    private static final long serialVersionUID = 4003197398132888066L;

    @DocumentId private String id;
    @DocumentField("dt") private Long yearmonth;//日期 年月
    @DocumentField("province_id") private Long provinceId; //省
    @DocumentField("province_name") private String provinceName;
    @DocumentField("city_id") private Long cityId; //市
    @DocumentField("city_name") private String cityName;
    @DocumentField("county_id") private Long countyId; //区
    @DocumentField("county_name") private String countyName;
    @DocumentField("school_id") private Long schoolId; //学校
    @DocumentField("school_name") private String schoolName;
    @DocumentField("subject") private String subject; //科目
    @DocumentField("school_level") private Integer schoolLevel; //学校级别 (小学 中学)
    @DocumentField("auth_use_tea_num_total") private Long auth_use_tea_num_total; //认证老师布置作业老师累计使用数
    @DocumentField("month_sasc") private Long month_sasc; //认证学生完成一套及以上作业学生使用数
    @DocumentCreateTimestamp @DocumentField("createtime") protected Date createTime; //创建时间


    public static String generateCacheKey(Long schoolId,Long yearmonth) {
        return CacheKeyGenerator.generateCacheKey(SchoolReportSituation.class,new String[]{"SCHOOLID","YEARCHMONTH"}, new Object[]{schoolId,yearmonth});
    }
    public SchoolReportSituation(){

    }
}
