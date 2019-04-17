package com.voxlearning.utopia.service.parent.homework.api.entity;

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

/**
 * 学生偏好设置表
 * @author chongfeng.qi
 * @date 2018-11-07
 *
 */
@Getter
@Setter
@DocumentConnection(configName = "mongo-jxt")
@DocumentDatabase(database = "vox-parent-homework")
@DocumentCollection(collection = "homework_user_preferences")
@DocumentIndexes({
        @DocumentIndex(def = "{'userId':1}", background = true)
})
@UtopiaCacheRevision("20181111")
@UtopiaCacheExpiration(604800)
public class HomeworkUserPreferences implements Serializable {

    private static final long serialVersionUID = -3775873132796336377L;

    public String ckUserId() {
        return CacheKeyGenerator.generateCacheKey(HomeworkUserPreferences.class,"userId",userId);
    }
    public String ckId() {
        return CacheKeyGenerator.generateCacheKey(HomeworkUserPreferences.class, id);
    }
    @DocumentId
    private String id; // 主键

    private Long userId; // 学生id

    private String subject; // 学科

    private String bookId; // 教材

    private List<String> levels; // 难度 BASE：基础，CRUX：提升

    @DocumentUpdateTimestamp
    private Date updateTime; //更新日期，格式为yyyy-MM-dd

    @DocumentCreateTimestamp
    private Date createTime; //创建日期，格式为yyyy-MM-dd

}
