package com.voxlearning.utopia.service.newhomework.api.entity;

import com.voxlearning.alps.annotation.dao.DocumentField;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentIdAutoGenerator;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlPrimaryKeyGeneratorType;
import com.voxlearning.alps.spi.common.PrimaryKeyAccessor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author majianxin
 * @version V1.0
 * @date 2019/1/15
 */
@Getter
@Setter
@DocumentTable(table = "VOX_HOMEWORK_DICT")
public class HomeworkDict implements PrimaryKeyAccessor<String> {
    private static final long serialVersionUID = -1325820056160903862L;

    @UtopiaSqlColumn(name = "ID", primaryKey = true, primaryKeyGeneratorType = UtopiaSqlPrimaryKeyGeneratorType.OBJECT_ID)
    @DocumentId(autoGenerator = DocumentIdAutoGenerator.OBJECT_ID)
    @DocumentField("ID") private String id;

    @DocumentField("NAME") private String name;
    @DocumentField("LEVEL") private String level;
    @DocumentField("LEVEL_ID") private String levelId;
    @DocumentField("PARENT_ID") private String parentId;
    @DocumentField("PREFIX") private String prefix;
    @DocumentField("ENUM_VALUE") private String enumValue;
    @DocumentField("BE_AUTHED") private String beAuthed;
}
