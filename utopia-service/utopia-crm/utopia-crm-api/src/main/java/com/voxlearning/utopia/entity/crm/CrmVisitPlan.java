package com.voxlearning.utopia.entity.crm;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * CrmVisitPlan
 *
 * @author song.wang
 * @date 2016/7/4
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-crm")
@DocumentDatabase(database = "vox-crm")
@DocumentCollection(collection = "vox_visit_plan")
public class CrmVisitPlan implements Serializable {
    private static final long serialVersionUID = -4260646613995969506L;

    @DocumentId private String id;                          // ID
    private Long userId;                                    // 用户ID
    private String userName;                                // 用户姓名
    private Long schoolId;                                  // 学校ID
    private String schoolName;                              // 学校名称
    private Date visitTime;                                 // 计划拜访时间
    private String content;                                 // 计划内容
    @DocumentCreateTimestamp private Date createTime;       // 创建时间
    @DocumentUpdateTimestamp private Date updateTime;       // 更新时间
    private Boolean disabled;                               // 删除标记

    public static String ck_user(Long userId) {
        return CacheKeyGenerator.generateCacheKey(CrmVisitPlan.class, "UID", userId);
    }

}
