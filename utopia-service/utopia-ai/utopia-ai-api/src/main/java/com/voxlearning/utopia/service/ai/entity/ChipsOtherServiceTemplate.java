package com.voxlearning.utopia.service.ai.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * @author guangqing
 * @since 2019/1/4
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-sochi")
@DocumentDatabase(database = "vox-chips")
@DocumentCollection(collection = "vox_other_service_template")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20190107")
public class ChipsOtherServiceTemplate implements Serializable {

    private static final long serialVersionUID = 1434545556790651243L;

    @DocumentId //serviceType-自定义id
    private String id;
//  BINDING("绑定公众号"), USEINSTRUCTION("薯条英语开课指导"),  RENEWREMIND("续费提醒") 这三种类型
    private String serviceType;
    private String name;
    private String json;
    @DocumentCreateTimestamp
    private Date createDate; // 做题时间
    @DocumentUpdateTimestamp
    private Date updateDate; // 更新时间


}
