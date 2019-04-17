package com.voxlearning.utopia.service.newhomework.api.entity.poetry;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.MonthRange;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * 古诗报名表
 * @author majianxin
 * @version V1.0
 * @date 2019/2/20
 */
@Getter
@Setter
@DocumentConnection(configName = "mongo-siberia")
@DocumentDatabase(database = "vox-poetry")
@DocumentCollection(collection = "poetry_register")
@UtopiaCacheExpiration(604800)
@UtopiaCacheRevision("20190220")
public class AncientPoetryRegister implements Serializable {
    private static final long serialVersionUID = -5894874470497725097L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private String id;                       // 主键(年月-活动ID-班组ID, 例:201809-activityId-545561)
    private String activityId;               // 活动ID
    private Long clazzGroupId;               // 班组ID
    private Long teacherId;                  // 老师ID
    @DocumentCreateTimestamp
    private Date createAt;                   // 报名时间
    @DocumentUpdateTimestamp
    private Date updateAt;                   // 修改时间
    private Boolean beenCanceled;            // 是否取消

    public static String generateId(Date activityCreateAt, String activityId, Long clazzGroupId) {
        MonthRange monthRange = MonthRange.newInstance(activityCreateAt.getTime());
        return StringUtils.join(monthRange.toString(), "-", activityId, "-", clazzGroupId);
    }

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(AncientPoetryRegister.class, id);
    }

    public static String ck_clazzGroupId(Long clazzGroupId) {
        return CacheKeyGenerator.generateCacheKey(AncientPoetryRegister.class,
                new String[]{"CG"},
                new Object[]{clazzGroupId});
    }
}
