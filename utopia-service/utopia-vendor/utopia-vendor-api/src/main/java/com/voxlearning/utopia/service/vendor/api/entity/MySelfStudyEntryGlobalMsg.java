package com.voxlearning.utopia.service.vendor.api.entity;

import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentIdAutoGenerator;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author jiangpeng
 * @since 2017-06-14 下午4:17
 **/
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-jxt")
@DocumentDatabase(database = "vox-jxt")
@DocumentCollection(collection = "vox_myselfstudy_global_msg{}", dynamic = true)
@UtopiaCacheRevision("20130318")
public class MySelfStudyEntryGlobalMsg implements Serializable, CacheDimensionDocument {
    private static final long serialVersionUID = -2687806285454422017L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private String id; //即SelfStudyType

    private String text;

    private String reminderId;

    private LiveCastIndexRefinedLessons refinedLessons;

    private LiveCastIndexRemind indexRemind;

    @Override
    public String[] generateCacheDimensions() {
        return new String[0];
    }
}
