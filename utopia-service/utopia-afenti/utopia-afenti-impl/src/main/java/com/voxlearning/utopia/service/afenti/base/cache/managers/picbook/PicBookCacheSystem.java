package com.voxlearning.utopia.service.afenti.base.cache.managers.picbook;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import lombok.Getter;

import javax.inject.Named;

/**
 * Created by Summer on 2018/4/8
 */
@Named
public class PicBookCacheSystem extends SpringContainerSupport {

    @Getter private StudentPicBookTopRankCacheManager studentPicBookTopRankCacheManager;
    @Getter private StudentPicBookTopSchoolRankCacheManager studentPicBookTopSchoolRankCacheManager;
    @Getter private StudentPicBookWeekRankRewardCacheManager studentPicBookWeekRankRewardCacheManager;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();

        UtopiaCache persistence = CacheSystem.CBS.getCache("persistence");
        studentPicBookTopRankCacheManager = new StudentPicBookTopRankCacheManager(persistence);
        studentPicBookTopSchoolRankCacheManager = new StudentPicBookTopSchoolRankCacheManager(persistence);
        studentPicBookWeekRankRewardCacheManager = new StudentPicBookWeekRankRewardCacheManager(persistence);
    }
}
