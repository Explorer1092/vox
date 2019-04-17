package com.voxlearning.washington.net.message.exam;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by tanguohong on 2015/7/16.
 */
@Setter
@Getter
public class TeachingDianosisCourseQuestionResultRequest extends TeachingDianosisQuestionResultRequest {

    private static final long serialVersionUID = 6475068066742729607L;

    private TeachingDianosisCourseQuestionExtraRequest extra;
}
