package com.voxlearning.utopia.service.newhomework.api.hbase;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentIdAutoGenerator;
import com.voxlearning.alps.annotation.dao.hbase.DocumentTableColumnFamily;
import com.voxlearning.alps.annotation.dao.hbase.DocumentTableNamespace;
import com.voxlearning.alps.annotation.dao.hbase.DocumentTableQualifier;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.newhomework.api.entity.base.BaseHomeworkResultLight;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

/**
 * @author xuesong.zhang
 * @since 2017/8/10
 */

@Getter
@Setter
@DocumentConnection(configName = "hbase-homework")
@DocumentTableNamespace(namespace = "homework")
@DocumentTableQualifier(qualifier = "homework_result")
@DocumentTableColumnFamily(family = "hr")
//@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
//@UtopiaCacheRevision("20170810")
public class HomeworkResultHBase extends BaseHomeworkResultLight implements Serializable {

    private static final long serialVersionUID = 6162794899090830647L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private String id;
//    @DocumentCreateTimestamp
    private Date createAt;              // 创建时间
//    @DocumentUpdateTimestamp
    private Date updateAt;              // 修改时间

    private Boolean finishCorrect;
    private Boolean repair;
    private Boolean urge;               //催促
    private Integer beanNum;            //家长奖励学豆数

    private String jsonData;            // 此处为对象的完整json，数据为BSON导入HBase时期的产物


    /**
     * =========================================
     * 下面的这些方法保留一下吧，虽然并没有什么用处
     * =========================================
     */
    @Getter
    @Setter
    @EqualsAndHashCode(of = {"day", "subject", "hid", "userId"})
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ID implements Serializable {
        private static final long serialVersionUID = -6724261932519326129L;
        private String day;
        private Subject subject;
        private String hid;
        private String userId;

        @Override
        public String toString() {
            return day + "-" + subject + "-" + hid + "-" + userId;
        }
    }

    public HomeworkResultHBase.ID parseID() {
        if (id == null || id.trim().length() == 0) return null;
        String[] segments = StringUtils.split(id, "-");
        if (segments.length != 4) return null;
        String day = segments[0];
        Subject subject = Subject.safeParse(segments[1]);
        String hid = segments[2];
        String uid = segments[3];
        return new HomeworkResultHBase.ID(day, subject, hid, uid);
    }

//    public static String ck_id(String id) {
//        return CacheKeyGenerator.generateCacheKey(HomeworkResultHBase.class, id);
//    }
}
