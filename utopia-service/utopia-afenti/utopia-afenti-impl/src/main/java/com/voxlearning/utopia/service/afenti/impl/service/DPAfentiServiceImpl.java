package com.voxlearning.utopia.service.afenti.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.utopia.service.afenti.api.DPAfentiService;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiLearningType;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ruib
 * @since 2017/2/10
 */
@Named
@Service(interfaceClass = DPAfentiService.class)
@ExposeService(interfaceClass = DPAfentiService.class)
public class DPAfentiServiceImpl implements DPAfentiService {
    @Inject private AfentiCastleServiceImpl afentiCastleService;
    @Inject private AfentiLoaderImpl afentiLoader;

    @Override
    public List<NewBookProfile> fetchGradeBookList(ClazzLevel clazzLevel, Subject subject) {
        return afentiCastleService.fetchGradeBookList(clazzLevel, subject, AfentiLearningType.castle);
    }

    @Override
    public Map<String, Object> loadUserAfentiStar(Long userId, Subject subject) {
        if (userId == null || subject == null) {
            return Collections.emptyMap();
        }
        int star = afentiLoader.loadUserTotalStar(userId, subject);
        Map<String, Object> result = new HashMap<>();
        result.put("star", star);
        return result;
    }
}
