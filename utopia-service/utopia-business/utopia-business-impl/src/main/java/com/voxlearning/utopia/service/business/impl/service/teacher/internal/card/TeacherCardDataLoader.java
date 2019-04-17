package com.voxlearning.utopia.service.business.impl.service.teacher.internal.card;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.mapper.TeacherCardMapper;

import javax.inject.Named;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by tanguohong on 2017/4/17.
 */
@Named
@TeacherCardDataChain({
        LoadTeacherNewUserTask.class,
        LoadTeacherHomeworkCard.class,
        LoadTeacherActivityCard.class
})
public class TeacherCardDataLoader extends SpringContainerSupport {
    private final List<AbstractTeacherCardDataLoader> chains = new LinkedList<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        TeacherCardDataChain annotation = getClass().getAnnotation(TeacherCardDataChain.class);
        for (Class<? extends AbstractTeacherCardDataLoader> beanClass : annotation.value()) {
            AbstractTeacherCardDataLoader loader = getBean(beanClass);
            if (loader == null) {
                throw new IllegalStateException("Bean " + beanClass.getName() + " not found");
            }
            chains.add(loader);
        }
        logger.debug("Composed {} teacher card data loader chains", chains.size());
    }

    public List<TeacherCardMapper> process(TeacherCardDataContext context) {
        TeacherCardDataContext contextForUse = context;
        for (AbstractTeacherCardDataLoader chain : chains) {
            contextForUse = chain.process(contextForUse);
        }
        return contextForUse.getTaskCards();
    }
}
