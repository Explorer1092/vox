package com.voxlearning.utopia.agent.persist.entity.qrcode;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.agent.constants.QRCodeBusinessType;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * 用户二维码关联信息
 *
 * @author song.wang
 * @date 2018/12/17
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "agent_user_qrcode")
public class UserQrCode implements CacheDimensionDocument {

    @DocumentId
    private String id;

    private Long userId;
    private QRCodeBusinessType businessType;
    private String qrCode;
    private String relatedId;             // 关联的第三方ID

    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
          newCacheKey(new String[]{"t", "u"}, new Object[]{this.businessType, this.userId}),
                newCacheKey(new String[]{"t", "rid"}, new Object[]{this.businessType, this.relatedId})
        };
    }
}
