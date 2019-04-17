package com.voxlearning.utopia.service.feedback.client;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.feedback.api.FeedbackLoader;
import lombok.Getter;

public class FeedbackLoaderClient {

    @Getter
    @ImportService(interfaceClass = FeedbackLoader.class)
    private FeedbackLoader feedbackLoader;
}
