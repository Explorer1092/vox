package com.voxlearning.utopia.service.afenti.impl.service.processor.result;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.api.constant.StudyType;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiType;
import com.voxlearning.utopia.service.afenti.api.context.ElfResultContext;
import com.voxlearning.utopia.service.afenti.api.entity.WrongQuestionLibrary;
import com.voxlearning.utopia.service.afenti.impl.service.AfentiLoaderImpl;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.api.constant.AfentiState.INCORRECT2MASTER;

/**
 * @author Ruib
 * @since 2016/7/24
 */
@Named
public class SER_LoadWaitingList extends SpringContainerSupport implements IAfentiTask<ElfResultContext> {
    @Inject private AfentiLoaderImpl afentiLoader;

    @Override
    public void execute(ElfResultContext context) {

        List<WrongQuestionLibrary> libs = afentiLoader.loadWrongQuestionLibraryByUserIdAndSubject(
                context.getStudent().getId(), context.getSubject())
                .stream()
                .filter(l -> l != null)
                .filter(l -> l.getState() != null)
                .filter(l -> (StringUtils.equals(l.getSource(), AfentiType.学习城堡.name()) ||
                        StringUtils.equals(l.getSource(), StudyType.homework.name())))
                .filter(l -> StringUtils.isNotBlank(l.getSeid()))
                .collect(Collectors.toList());

        // 查找处于INCORRECT2MASTER状态的
        context.getIds().addAll(libs.stream().filter(l -> l.getState() == INCORRECT2MASTER)
                .map(WrongQuestionLibrary::getId).collect(Collectors.toSet()));
    }
}
