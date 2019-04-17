package com.voxlearning.utopia.service.afenti.impl.service.processor.video.prepraration;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.context.FetchPreparationVideoContext;
import com.voxlearning.utopia.service.afenti.impl.service.AfentiLoaderImpl;
import com.voxlearning.utopia.service.afenti.impl.util.AfentiPreparationVideoUtils;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;
import com.voxlearning.utopia.service.question.api.entity.Video;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author songtao
 * @since 17/7/20
 */
@Named
public class FPV_LoadVideo extends SpringContainerSupport implements IAfentiTask<FetchPreparationVideoContext> {
    @Inject AfentiLoaderImpl afentiLoader;

    @Override
    public void execute(FetchPreparationVideoContext context) {
        String bookId = context.getBook().book.getId();

        Map<String,Video> videoMap = afentiLoader.loadPreparationVideosByBookId(bookId);

        List<String> videoIds = AfentiPreparationVideoUtils.getVideosByBookId(bookId);
        videoIds.forEach( e -> {
            if (videoMap.get(e) != null) {
                context.getVideoList().add(videoMap.get(e));
            }
        });
    }

}
