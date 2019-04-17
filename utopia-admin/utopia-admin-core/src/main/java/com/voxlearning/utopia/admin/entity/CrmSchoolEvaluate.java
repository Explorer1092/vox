package com.voxlearning.utopia.admin.entity;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.util.CacheKeyGenerator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * 学校评分
 * Created by yaguang.wang on 2017/1/6.
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-crm")
@DocumentDatabase(database = "vox-crm")
@DocumentCollection(collection = "vox_school_evaluate")
public class CrmSchoolEvaluate implements Serializable {
    private static final long serialVersionUID = -3810622607443054611L;
    @DocumentId
    private String id;
    private String account;          // 评价人账号
    private String accountName;      // 评价人姓名
    private Integer placeScore;      // 地理位置得分
    private Integer teachScore;      // 教学质量
    private Integer studentScore;    // 生源水平
    private Integer commercializeScore;     // 商业潜力
    private String remark;           // 评价备注
    private Long schoolId;           // 学校ID
    @DocumentCreateTimestamp private Date createTime;
    @DocumentUpdateTimestamp private Date updateTime;

    public static String ck_sid(Long schoolId) {
        return CacheKeyGenerator.generateCacheKey(CrmSchoolEvaluate.class, "sid", schoolId);
    }
}
