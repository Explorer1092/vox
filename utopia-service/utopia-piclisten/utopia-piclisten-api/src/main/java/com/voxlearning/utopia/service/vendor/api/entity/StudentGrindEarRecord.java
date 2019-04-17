package com.voxlearning.utopia.service.vendor.api.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 磨耳朵活动天记录
 *
 * @author jiangpeng
 * @since 2016-10-25 下午9:35
 **/
@Getter
@Setter
@DocumentConnection(configName = "mongo-jxt")
@DocumentDatabase(database = "vox-jxt")
@DocumentCollection(collection = "student_grind_ear_record_18start")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20180302")
public class StudentGrindEarRecord implements Serializable {
    private static final long serialVersionUID = -3163449586714562463L;

    @DocumentId
    private Long id; //学生id

    private List<Date> dateList;

    @DocumentCreateTimestamp
    private Date createDate;


    public static String ck_id(Long id){
        return CacheKeyGenerator.generateCacheKey(StudentGrindEarRecord.class, id);
    }

    public Long dayCount(){
        if (CollectionUtils.isEmpty(getDateList()))
            return 0L;
        return getDateList().stream().map(t -> DayRange.newInstance(t.getTime())).distinct().count();
    }

}
