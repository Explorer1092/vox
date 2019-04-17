package com.voxlearning.utopia.service.afenti.impl.service.processor.login;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiLearningType;
import com.voxlearning.utopia.service.afenti.api.context.LoginContext;
import com.voxlearning.utopia.service.afenti.api.data.AfentiBook;
import com.voxlearning.utopia.service.afenti.impl.service.AfentiServiceImpl;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * 初始化用户教材
 *
 * @author Ruib
 * @since 2016/7/12
 */
@Named
public class L_LoadAfentiBook extends SpringContainerSupport implements IAfentiTask<LoginContext> {
    @Inject private AfentiServiceImpl afentiService;

    @Override
    public void execute(LoginContext context) {
        MapMessage message = afentiService.fetchAfentiBook(context.getStudent().getId(), context.getSubject(), AfentiLearningType.castle);
        if (!message.isSuccess()) {
            logger.error("L_LoadAfentiBook Cannot load afenti book for user {}, subject {}",
                    context.getStudent().getId(), context.getSubject());
            context.errorResponse();
            return;
        }
        AfentiBook book = (AfentiBook) message.get("book");
        if (null == book) {
            logger.error("L_LoadAfentiBook Cannot load afenti book for user {}, subject {}",
                    context.getStudent().getId(), context.getSubject());
            context.errorResponse();
            return;
        }
        context.setBook(book);
        context.getResult().put("bookId", book.book.getId());
        context.getResult().put("bookName", book.book.getName());
        context.getResult().put("autoChangeBook", SafeConverter.toBoolean(message.get("changeFlag")));
    }
}
