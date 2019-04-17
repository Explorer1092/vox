package com.voxlearning.utopia.service.afenti.impl.service.processor.video.prepraration;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.context.FetchPreparationVideoContext;
import com.voxlearning.utopia.service.afenti.api.data.BookVideo;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;
import com.voxlearning.utopia.service.question.api.entity.Video;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author songtao
 * @since 17/7/20
 */
@Named
public class FPV_Transform extends SpringContainerSupport implements IAfentiTask<FetchPreparationVideoContext> {

    @Override
    public void execute(FetchPreparationVideoContext context) {
        if (CollectionUtils.isNotEmpty(context.getVideoList())) {
            for (Video v : context.getVideoList()) {
                BookVideo res = new BookVideo();
                res.coverUrl = v.getCoverUrl();
                res.videoName = v.getVideoName();
                res.videoUrl = v.getVideoUrl();

                context.getVideos().add(res);
            }
        }
    }
}
