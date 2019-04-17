package com.voxlearning.utopia.service.afenti.impl.service.processor.video.prepraration;

import com.voxlearning.utopia.service.afenti.api.context.FetchPreparationVideoContext;
import com.voxlearning.utopia.service.afenti.impl.service.processor.AbstractAfentiProcessor;
import com.voxlearning.utopia.service.afenti.impl.service.processor.AfentiTasks;

import javax.inject.Named;

/**
 * @author songtao
 * @since 2017/7/20
 */
@Named
@AfentiTasks({
        FPV_ValidatePaid.class,
        FPV_LoadAfentiBook.class,
        FPV_LoadVideo.class,
        FPV_Transform.class
})

public class FetchPreparationVideoDataProcessor extends AbstractAfentiProcessor<FetchPreparationVideoContext> {
}
