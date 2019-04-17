package com.voxlearning.utopia.service.mizar.api.entity.notify;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndex;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndexes;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.service.mizar.api.mapper.MizarFile;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Mizar平台消息记录
 *
 * @author yuechen.wang
 * @date 2016/12/01
 */
@Setter
@Getter
@DocumentDatabase(database = "vox_mizar")
@DocumentCollection(collection = "mizar_notify")
@DocumentIndexes(value = {
        @DocumentIndex(def = "{'creator':1}", background = true)
})
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20161009")
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
public class MizarNotify implements CacheDimensionDocument {

    private static final long serialVersionUID = 8279736058562192088L;

    public static final int MAXIMUM_FILE_COUNT = 3; // 最多上传附件的数量

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.OBJECT_ID) private String id;
    private String title;         // 标题
    private String content;       // 内容
    private String type;          // 类型, MizarNotifyType
    private String url;           // 跳转链接
    private String creator;       // 消息创建人
    private Date sendTime;        // 消息发送时间(以后用于支持定时发送)
    private List<MizarFile> files;   // 附件

    @DocumentCreateTimestamp private Date createAt;
    @DocumentUpdateTimestamp private Date updateAt;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey(id),
                newCacheKey("C", creator)
        };
    }
}
