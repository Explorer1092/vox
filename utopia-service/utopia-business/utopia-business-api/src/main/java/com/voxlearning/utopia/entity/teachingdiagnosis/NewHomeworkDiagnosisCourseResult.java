package com.voxlearning.utopia.entity.teachingdiagnosis;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentIdAutoGenerator;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@DocumentConnection(configName = "mongo-madrid")
@DocumentDatabase(database = "vox-diagnosis")
@DocumentCollection(collection = "new_homework_diagnosis_course_result")
public class NewHomeworkDiagnosisCourseResult implements Serializable {
    private static final long serialVersionUID = 8614224149653529611L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private Long id;  //学生ID为主键ID
    private List<String> courseIds; //学生作业中已完成课程ID
}
