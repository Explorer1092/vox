package com.voxlearning.utopia.service.afenti.impl.service.processor.book;

import com.voxlearning.utopia.service.afenti.api.context.FetchGradeBookContext;
import com.voxlearning.utopia.service.afenti.impl.service.processor.AbstractAfentiProcessor;
import com.voxlearning.utopia.service.afenti.impl.service.processor.AfentiTasks;

import javax.inject.Named;

/**
 * @author Ruib
 * @since 2016/7/13
 */
@Named
@AfentiTasks({
        FGB_LoadCandidates.class,
        FGB_LoadBooks.class,
        FGB_SortBooks.class
})
public class FetchGradeBookProcessor extends AbstractAfentiProcessor<FetchGradeBookContext> {
}
