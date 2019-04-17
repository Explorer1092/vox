package com.voxlearning.utopia.enanalyze.entity;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentIdAutoGenerator;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlPrimaryKeyGeneratorType;
import lombok.Data;

import java.io.Serializable;

/**
 * 群
 *
 * @author xiaolei.li
 * @version 2018/7/21
 */
@Data
@DocumentConnection(configName = "main")
@DocumentTable(table = "EA_GROUP")
@UtopiaCacheExpiration
public class GroupEntity implements Serializable {

    @UtopiaSqlColumn(name = "ID", primaryKey = true, primaryKeyGeneratorType = UtopiaSqlPrimaryKeyGeneratorType.AUTO_INC)
    @DocumentId(autoGenerator = DocumentIdAutoGenerator.AUTO_INC)
    private Long id;

    @UtopiaSqlColumn(name = "OPEN_GROUP_ID")
    private String openGroupId;

    @UtopiaSqlColumn(name = "OPEN_GROUP_NAME")
    private String openGroupName;
}
