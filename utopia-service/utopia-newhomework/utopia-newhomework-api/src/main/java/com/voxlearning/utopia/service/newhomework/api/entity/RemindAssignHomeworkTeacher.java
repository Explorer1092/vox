package com.voxlearning.utopia.service.newhomework.api.entity;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@DocumentTable(table = "VOX_REMIND_ASSIGN_HOMEWORK_TEACHER")
@DocumentConnection(configName = "homework")
@NoArgsConstructor
public class RemindAssignHomeworkTeacher extends AbstractDatabaseEntity {
    private static final long serialVersionUID = -8821419034680336437L;

    public static String ck_id(Long id) {
        return CacheKeyGenerator.generateCacheKey(RemindAssignHomeworkTeacher.class, id);
    }
}
