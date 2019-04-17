package com.voxlearning.utopia.service.newhomework.api.entity;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@DocumentConnection(configName = "homework")
@DocumentTable(table = "VOX_ANCIENT_POETRY_STUDENT_GLOBAL_STAR")
public class AncientPoetryStudentGlobalStar extends AbstractDatabaseEntity {

    private static final long serialVersionUID = 4823337181283750560L;

    private Double totalStar;   // 总星数
    private Long totalDuration; // 总时长
    private Integer clazzLevel; // 年级
    private Long schoolId;      // 学校id
    private Integer regionId;   // 区id


    public static String ck_id(Long id) {
        return CacheKeyGenerator.generateCacheKey(AncientPoetryStudentGlobalStar.class, id);
    }
}
