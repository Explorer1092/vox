package com.voxlearning.utopia.service.newhomework.api.hbase;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentIdAutoGenerator;
import com.voxlearning.alps.annotation.dao.hbase.DocumentTableColumnFamily;
import com.voxlearning.alps.annotation.dao.hbase.DocumentTableNamespace;
import com.voxlearning.alps.annotation.dao.hbase.DocumentTableQualifier;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.utopia.service.newhomework.api.entity.base.BaseHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.QuestionWrongReason;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

/**
 * 这个对象实际上并没有缓存处理
 * 因为都是冷数据，缓存意义不大
 *
 * @author xuesong.zhang
 * @since 2017/8/10
 */
@Getter
@Setter
@DocumentConnection(configName = "hbase-homework")
@DocumentTableNamespace(namespace = "homework")
@DocumentTableQualifier(qualifier = "homework_process_result")
@DocumentTableColumnFamily(family = "hpr")
//@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
//@UtopiaCacheRevision("20170810")
public class HomeworkProcessResultHBase extends BaseHomeworkProcessResult implements Serializable {

    private static final long serialVersionUID = 7180585754380896360L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private String id;
    // @DocumentCreateTimestamp
    private Date createAt;                          // 创建时间
    // @DocumentUpdateTimestamp
    private Date updateAt;                          // 修改时间
    private String sourceQuestionId;                // 原题id
    private QuestionWrongReason wrongReason;        // 错题原因

    private String jsonData;                        // 此处为对象的完整json，数据为BSON导入HBase时期的产物

//    public static String ck_id(String id) {
//        return CacheKeyGenerator.generateCacheKey(NewHomeworkProcessResult.class, id);
//    }

    /**
     * =========================================
     * 下面的这些方法保留一下吧，虽然并没有什么用处
     * =========================================
     */
    @Getter
    @Setter
    @EqualsAndHashCode(of = {"time", "randomId"})
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ID implements Serializable {
        private static final long serialVersionUID = 7861980956874978169L;
        private String randomId = RandomUtils.nextObjectId();
        private String time;

        public ID(Date createTime) {
            this.time = Long.toString(createTime.getTime());
        }

        @Override
        public String toString() {
            return randomId + "-" + time;
        }
    }
}
