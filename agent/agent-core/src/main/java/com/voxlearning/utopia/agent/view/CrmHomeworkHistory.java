package com.voxlearning.utopia.agent.view;

import com.voxlearning.utopia.service.newhomework.api.mapper.baiscreview.report.BasicReviewHomeworkHistory;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CrmHomeworkHistory extends BasicReviewHomeworkHistory {
    private String homeworkFlag;
    private String assignTime;
}
