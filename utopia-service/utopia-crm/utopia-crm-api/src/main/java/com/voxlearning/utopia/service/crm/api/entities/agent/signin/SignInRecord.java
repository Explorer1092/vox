package com.voxlearning.utopia.service.crm.api.entities.agent.signin;

import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.service.crm.api.constants.agent.SignInBusinessType;
import com.voxlearning.utopia.service.crm.api.constants.agent.SignInType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * 签到信息
 *
 * @author song.wang
 * @date 2018/12/6
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-crm")
@DocumentDatabase(database = "vox-crm")
@DocumentCollection(collection = "vox_sign_in_record")
@UtopiaCacheRevision("20181214")
public class SignInRecord implements CacheDimensionDocument {
    private static final long serialVersionUID = 4977584795075255061L;
    @DocumentId
    private String id;
    private SignInBusinessType businessType;      // 签到的业务类型
    private SignInType signInType;                // 签到类型  GPS, 照片签到
    private String coordinateType;   // 坐标类型
    private String latitude;         // 纬度
    private String longitude;        // 经度
    private String photoUrl;         // 照片地址
    private String address;          // 地址

    private Long userId;
    private String userName;

    private Date signInTime;

    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
        };
    }
}
