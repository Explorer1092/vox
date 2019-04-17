package com.voxlearning.utopia.entity.activity;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;

import java.util.List;
import java.util.Map;

/**
 *  七巧板配置 - 实体
 *  @author haitian.gan
 */
@DocumentConnection(configName = "mongo-misc")
@DocumentTable(table = "vox-activity")
@DocumentCollection(collection = "vox_student_tangram_config")
public class TangramConfig implements CacheDimensionDocument{

    private static final long serialVersionUID = 1178151111264018456L;

    @DocumentId private Long id;
    private List<Map<String,Object>> data;

    @Override
    public String[] generateCacheDimensions() {
        return new String[0];
    }
}
