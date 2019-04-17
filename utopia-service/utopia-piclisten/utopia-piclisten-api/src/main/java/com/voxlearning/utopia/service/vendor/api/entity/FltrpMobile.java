package com.voxlearning.utopia.service.vendor.api.entity;

import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndex;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndexes;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * @author xinxin
 * @since 3/17/17.
 * 记录调用外研社sdk时使用的手机号
 * 规则：
 * 1、手机号唯一
 * 2、手机号必须是没被17作业用户使用过的（只当前状态，生成后又被占用的情况忽略）
 * 3、这里的手机号有可能跟外研社那边的不相同，因为在外研社sdk内用户可以修改手机号，这种情况是不跟我们同步的
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-app")
@DocumentDatabase(database = "vox-jxt")
@DocumentCollection(collection = "vox_fltrp_mobile")
@DocumentIndexes(value = {
        @DocumentIndex(def = "{'mobile':1}", background = true),
        @DocumentIndex(def = "{'uid':1}", background = true, unique = true)
})
public class FltrpMobile implements CacheDimensionDocument {
    private static final long serialVersionUID = -62591739055074976L;

    @DocumentId
    private String id;
    @DocumentField(value = "uid")
    private Long userId;
    private String mobile;
    private Boolean checked;    //是否通过外研社的接口确认过用户在外研社用的手机号确实是这个手机号
    private Boolean real;   //是否真实手机号，如果是从外研社同步的过来就是真手机号
    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey("UID", userId)
        };
    }
}
