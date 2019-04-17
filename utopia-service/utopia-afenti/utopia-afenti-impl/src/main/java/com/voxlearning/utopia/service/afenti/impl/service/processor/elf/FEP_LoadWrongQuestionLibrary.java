package com.voxlearning.utopia.service.afenti.impl.service.processor.elf;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.api.constant.StudyType;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiType;
import com.voxlearning.utopia.service.afenti.api.context.FetchElfPageContext;
import com.voxlearning.utopia.service.afenti.api.entity.WrongQuestionLibrary;
import com.voxlearning.utopia.service.afenti.impl.service.AfentiLoaderImpl;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Ruib
 * @since 2016/7/24
 */
@Named
public class FEP_LoadWrongQuestionLibrary extends SpringContainerSupport implements IAfentiTask<FetchElfPageContext> {

    @Inject private AfentiLoaderImpl afentiLoader;

    @Override
    public void execute(FetchElfPageContext context) {
        List<WrongQuestionLibrary> realList = afentiLoader.loadWrongQuestionLibraryByUserIdAndSubject(context.getStudentId(), context.getSubject()) .stream()
                .filter(Objects::nonNull)
                .filter(l -> l.getState() != null)
                .filter(l -> (StringUtils.equals(l.getSource(), AfentiType.学习城堡.name()) ||
                        StringUtils.equals(l.getSource(), StudyType.homework.name())))
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(realList)) {
            Collections.sort(realList, (o1, o2) -> o2.getUpdateAt().compareTo(o1.getUpdateAt()));
        }

        context.setLibraryList(realList);
    }
}

