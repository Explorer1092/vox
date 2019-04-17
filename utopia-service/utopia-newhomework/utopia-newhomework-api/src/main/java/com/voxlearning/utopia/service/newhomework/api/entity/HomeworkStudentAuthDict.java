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
@DocumentTable(table = "VOX_HOMEWORK_STUDENT_AUTH_DICT")
public class HomeworkStudentAuthDict implements PrimaryKeyAccessor<Long> {
    private static final long serialVersionUID = -5462886621484119774L;

    @UtopiaSqlColumn(name = "ID", primaryKey = true, primaryKeyGeneratorType = UtopiaSqlPrimaryKeyGeneratorType.AUTO_INC)
    @DocumentId(autoGenerator = DocumentIdAutoGenerator.AUTO_INC)
    @DocumentField("ID") private Long id;

    @DocumentField("HomeworkType") private String homeworkType;
    @DocumentField("HomeworkType_name") private String homeworkTypeName;
    @DocumentField("HomeworkFormType") private String homeworkFormType;
    @DocumentField("Homeworkform_name") private String homeworkFormName;
}
