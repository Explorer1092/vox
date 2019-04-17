package com.voxlearning.utopia.service.business.impl.processor.fairyland;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.business.impl.processor.AbstractExecuteTask;
import com.voxlearning.utopia.service.config.api.constant.GlobalTagName;
import com.voxlearning.utopia.service.config.client.GlobalTagServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Objects;

/**
 * 用户黑名单，如果用户不是白名单但是在黑名单中，所有应用都看不见
 *
 * @author Ruib
 * @since 2019/1/2
 */
@Named
public class FSMA_PersonalBlack extends AbstractExecuteTask<FetchStudentAppContext> {

    @Inject private GlobalTagServiceClient client;

    @Override
    public void execute(FetchStudentAppContext context) {

        if (context.isWhite()) return;

        boolean black = client.getGlobalTagBuffer().findByName(GlobalTagName.AfentiBlackListUsers.name())
                .stream()
                .filter(p -> Objects.equals(context.getStudentId(), SafeConverter.toLong(p.getTagValue())))
                .findFirst()
                .orElse(null) != null;

        if (black) context.terminateTask();
    }
}
