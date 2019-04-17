package com.voxlearning.utopia.service.afenti.impl.service.processor.result;

import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.api.constant.AfentiState;
import com.voxlearning.utopia.service.afenti.api.constant.UtopiaAfentiConstants;
import com.voxlearning.utopia.service.afenti.api.context.ElfResultContext;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Ruib
 * @since 2016/7/24
 */
@Named
public class IER_SimilarQuestionNotFound extends SpringContainerSupport implements IAfentiTask<ElfResultContext> {
    @Inject private SimilarElfResultProcessor processor;

    @Override
    public void execute(ElfResultContext context) {
        ElfResultContext in = new ElfResultContext();
        in.setStudent(context.getStudent());
        in.setSubject(context.getSubject());
        in.setQuestionId(UtopiaAfentiConstants.NO_SIMILAR_QUESTION);
        in.setMaster(true);
        in.setAfentiState(AfentiState.INCORRECT2MASTER);
        in.setOriginalQuestionId(context.getQuestionId());
        in.setQuestion(context.getQuestion());

        ElfResultContext out = processor.process(in);
        if (!out.isSuccessful()) {
            logger.error("IER_SimilarQuestionNotFound {}", JsonUtils.toJson(in));
            context.errorResponse();
        } else {
            context.getResult().put("count", out.getResult().get("count"));
            context.getResult().put("integral", out.getResult().get("integral"));
            context.getResult().put("creditCount", out.getResult().get("creditCount"));
        }
    }
}
