package com.voxlearning.utopia.service.parent.homework.provider.intelligentTeaching.impl.entity;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentField;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 讲练测题
 *
 * @author Wenlong Meng
 * @since Feb 16, 2019
 */
@Getter
@Setter
@DocumentConnection(configName = "mongo-question")
@DocumentDatabase(database = "vox-question")
@DocumentCollection(collection = "intelligent_diagnosis_question")
public class IntelligentDiagnosisQuestion implements Serializable {

    private static final long serialVersionUID = -2022210401815265192L;

    @DocumentId
    private String id;
    @DocumentField("question_id")
    private String questionId;

}
