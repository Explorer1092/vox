package com.voxlearning.utopia.service.parent.homework.api.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentIdAutoGenerator;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * 用户活动信息
 *
 * @author Wenlong Meng
 * @since Feb 23, 2019
 */
@Getter
@Setter
@DocumentConnection(configName = "mongo-jxt")
@DocumentDatabase(database = "vox-parent-homework")
@DocumentCollection(collection = "activity")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.fixed, value = 7*24*60*60)
@UtopiaCacheRevision("20190303")
public class Activity implements Serializable {

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private String id; //活动id
    private String type; //用户id
    private String name; // 名称
    private String desc; // 描述
    private String periodUnit;//周期单元
    private Date startTime;//开始时间
    private Date endTime;//结束时间
    private Integer status;//状态
    private Date createTime;//创建时间
    private Date updateTime;//更新时间
    private Map<String, Object> extInfo;//扩展信息

    /**
     * cache key
     *
     * @return
     */
    public String ckId(){
        return CacheKeyGenerator.generateCacheKey(Activity.class,null, id);
    }

    /**
     * 活动总周期数
     *
     * @return
     */
    public int periodCount(){
        int periodCount = 1;
        PeriodUnit unit = PeriodUnit.of(this.periodUnit);
        int days = days(endTime, startTime);
        switch (unit){
            case WEEK:
                periodCount = (days + unit.interval - 1) / unit.interval;
                break;
            case DAY:
                periodCount = days;
                break;
            case NO:
                periodCount = 1;
                break;
            default:
                break;
        }
        return periodCount;
    }

    /**
     * 活动当前周期数
     *
     * @return
     */
    public Period currentPeriod(){
        Date now = new Date();
        if(status != 0 || startTime.after(now) || endTime.before(now)){
            return Period.NULL;
        }
        int days = days(new Date(), startTime);
        if(days < 0){
            return Period.NULL;
        }
        PeriodUnit unit = PeriodUnit.of(this.periodUnit);
        switch (unit){
            case WEEK:
            case DAY:
                int index = (days + unit.interval -1) / unit.interval;
                Date startTime = DateUtils.addDays(this.startTime, days / unit.interval * unit.interval);
                Date endTime = DateUtils.addDays(startTime, unit.interval);
                return new Period(index, startTime, endTime);
            case NO:
                return new Period(1, this.startTime, this.endTime);
            default:
                break;
        }
        return Period.NULL;
    }

    /**
     * 计算两个日期相差天数
     *
     * @param d1
     * @param d2
     * @return
     */
    public static int days(Date d1, Date d2){
        return (int)Math.ceil((d1.getTime() - d2.getTime()) / 86400000D);
    }

}
