package com.voxlearning.utopia.service.afenti.impl.service.processor.result;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.api.constant.AfentiState;
import com.voxlearning.utopia.service.afenti.api.context.ElfResultContext;
import com.voxlearning.utopia.service.afenti.impl.dao.WrongQuestionLibraryDao;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;
import com.voxlearning.utopia.service.integral.api.constants.CreditType;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.CreditHistory;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.user.api.UserIntegralService;

import javax.inject.Inject;
import javax.inject.Named;

import static com.voxlearning.utopia.service.integral.api.constants.IntegralType.*;


/**
 * @author Ruib
 * @since 2016/7/24
 */
@Named
public class SER_RewardAndChangeState extends SpringContainerSupport implements IAfentiTask<ElfResultContext> {
    @Inject private WrongQuestionLibraryDao wrongQuestionLibraryDao;

    @ImportService(interfaceClass = UserIntegralService.class) protected UserIntegralService userIntegralService;

    @Override
    public void execute(ElfResultContext context) {
        IntegralType integralType;
        switch (context.getSubject()) {
            case ENGLISH: {
                integralType = 阿分提错题本;
                break;
            }
            case MATH: {
                integralType = AFENTI_MATH_ERROR_BOOK;
                break;
            }
            case CHINESE: {
                integralType = AFENTI_CHINESE_ERROR_BOOK;
                break;
            }
            default:
                return;
        }

        IntegralHistory integralHistory = new IntegralHistory(context.getStudent().getId(), integralType, context.getIntegral());
        integralHistory.setComment(StringUtils.formatMessage("阿分题{}错题本奖励学豆", context.getSubject().getValue()));
        MapMessage message = userIntegralService.changeIntegral(context.getStudent(), integralHistory);
        if (message.isSuccess()) {
            for (String id : context.getIds())
                wrongQuestionLibraryDao.updateState(id, AfentiState.INCORRECT2SPENDING, null);
        }

        // 发放自学积分
        if (context.getCreditCount() > 0) {
            CreditHistory creditHistory = new CreditHistory();
            creditHistory.setUserId(context.getStudent().getId());
            creditHistory.setAmount(context.getCreditCount());
            creditHistory.setComment("阿分题:错题中心发奖");
            creditHistory.setType(CreditType.afenti_receive.getType());
            MapMessage creditMapMessage = userIntegralService.changeCredit(creditHistory);

            if (!creditMapMessage.isSuccess())
                logger.error("afenti receive credit error::sid:{}creditCount:{}", context.getStudent().getId(), context.getCreditCount());
        }

        context.getResult().put("count", 5);
        context.getResult().put("integral", context.getIntegral());
        context.getResult().put("creditCount", context.getCreditCount());
    }
}
