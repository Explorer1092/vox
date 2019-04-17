package com.voxlearning.utopia.service.feedback.client;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.feedback.api.FeedbackService;
import lombok.Getter;

public class FeedbackServiceClient {

    @Getter
    @ImportService(interfaceClass = FeedbackService.class)
    private FeedbackService feedbackService;
}
