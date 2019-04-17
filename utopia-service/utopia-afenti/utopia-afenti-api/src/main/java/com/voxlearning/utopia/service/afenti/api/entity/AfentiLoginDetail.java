package com.voxlearning.utopia.service.afenti.api.entity;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentIdAutoGenerator;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.meta.Subject;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Set;

/**
 * @author Ruib
 * @since 2016/8/25
 */
@Getter
@Setter
@DocumentConnection(configName = "mongo-journal")
@DocumentDatabase(database = "vox-afenti")
@DocumentCollection(collection = "vox_afenti_login_detail")
public class AfentiLoginDetail implements Serializable {
    private static final long serialVersionUID = 737650273474160742L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE) private String id;
    private Set<String> details; // 用户登陆记录，每天记录一次

    public static String generateId(Long userId, Subject subject) {
        if (userId == 0 || subject == null) {
            return null;
        }
        return userId + "-" + subject;
    }
}
