package com.voxlearning.utopia.service.newhomework.api.entity.vacation;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.service.newhomework.api.entity.base.BaseHomeworkBook;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * @author guoqiang.li
 * @since 2017/6/5
 */
@Getter
@Setter
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-winter-vacation-2019")
@DocumentCollection(collection = "vacation_homework_book")
@UtopiaCacheExpiration(172800)
@UtopiaCacheRevision("20181128")
public class VacationHomeworkBook extends BaseHomeworkBook implements Serializable {

    private static final long serialVersionUID = -715396764030828706L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private String id;
    @DocumentCreateTimestamp
    private Date createAt;
    @DocumentUpdateTimestamp
    private Date updateAt;

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(VacationHomeworkBook.class, id);
    }
}
