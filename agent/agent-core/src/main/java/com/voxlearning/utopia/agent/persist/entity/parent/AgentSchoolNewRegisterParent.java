package com.voxlearning.utopia.agent.persist.entity.parent;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 *  新注册家长
 *
 * @author deliang.che
 * @since  2019/4/1
 */

@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "school_new_register_parent")
public class AgentSchoolNewRegisterParent implements CacheDimensionDocument {

    private static final long serialVersionUID = -6551286632464800810L;
    @DocumentId
    private String id;

    private Long schoolId;     //学校ID
    private Long parentId;     //家长ID


    private Date registerTime;//注册时间

    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey(new String []{"sid","pid"},new Object[]{schoolId,parentId})

        };
    }
}

