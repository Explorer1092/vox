package com.voxlearning.utopia.service.newexam.api.context;

import com.voxlearning.utopia.service.newexam.api.entity.NewExamProcessResult;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by tanguohong on 2016/3/11.
 */
@Getter
@Setter
public class FinishNewExamContext extends AbstractContext<FinishNewExamContext> {

    // in
    private NewExamProcessResult currentProcessResult; // 当前考试结果




}
