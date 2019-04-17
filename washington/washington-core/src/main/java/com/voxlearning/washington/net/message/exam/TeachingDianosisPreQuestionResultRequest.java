package com.voxlearning.washington.net.message.exam;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TeachingDianosisPreQuestionResultRequest extends TeachingDianosisQuestionResultRequest {

    private static final long serialVersionUID = 6475068066742729607L;

    private TeachingDianosisPreQuestionExtraRequest extra;
}
