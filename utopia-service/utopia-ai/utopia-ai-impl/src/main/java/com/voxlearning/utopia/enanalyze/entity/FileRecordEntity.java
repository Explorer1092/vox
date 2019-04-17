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
import java.util.Date;

/**
 * 文件记录
 *
 * @author xiaolei.li
 * @version 2018/7/21
 */
@Data
@DocumentConnection(configName = "main")
@DocumentTable(table = "EA_FILE_RECORD")
@UtopiaCacheExpiration
public class FileRecordEntity implements Serializable {

    @UtopiaSqlColumn(name = "ID", primaryKey = true, primaryKeyGeneratorType = UtopiaSqlPrimaryKeyGeneratorType.AUTO_INC)
    @DocumentId(autoGenerator = DocumentIdAutoGenerator.AUTO_INC)
    private Long id;

    @UtopiaSqlColumn(name = "FILE_ID")
    private String fileId;

    @UtopiaSqlColumn(name = "URL")
    private String url;

    @UtopiaSqlColumn(name = "OPEN_ID")
    private String openId;

    @UtopiaSqlColumn(name = "CREATE_DATE")
    private Date createDate;

    @UtopiaSqlColumn(name = "UPDATE_DATE")
    private Date updateDate;
}
