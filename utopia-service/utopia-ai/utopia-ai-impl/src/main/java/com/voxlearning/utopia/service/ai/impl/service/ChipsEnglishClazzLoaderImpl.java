package com.voxlearning.utopia.service.ai.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.ai.api.ChipsEnglishClazzLoader;
import com.voxlearning.utopia.service.ai.entity.AiChipsEnglishTeacher;
import com.voxlearning.utopia.service.ai.impl.persistence.AiChipsEnglishTeacherDao;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.List;

/**
 * @author guangqing
 * @since 2018/8/24
 */
@Slf4j
@Named
@ExposeService(interfaceClass = ChipsEnglishClazzLoader.class)
public class ChipsEnglishClazzLoaderImpl implements ChipsEnglishClazzLoader {
    @Inject
    private AiChipsEnglishTeacherDao chipsEnglishTeacherDao;

    @Override
    public List<AiChipsEnglishTeacher> loadChipsEnglishTeacherByName(String name) {
        if (StringUtils.isBlank(name)) {
            return Collections.emptyList();
        }
        List<AiChipsEnglishTeacher> list = chipsEnglishTeacherDao.loadByName(name);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        return list;
    }
}
