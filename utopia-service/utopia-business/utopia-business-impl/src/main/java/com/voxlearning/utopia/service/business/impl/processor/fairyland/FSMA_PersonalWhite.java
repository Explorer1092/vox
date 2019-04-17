package com.voxlearning.utopia.service.business.impl.processor.fairyland;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.business.impl.processor.AbstractExecuteTask;
import com.voxlearning.utopia.service.config.api.constant.GlobalTagName;
import com.voxlearning.utopia.service.config.client.GlobalTagServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Objects;

/**
 * 用户白名单，白名单优先级最高
 *
 * @author Ruib
 * @since 2019/1/2
 */
@Named
public class FSMA_PersonalWhite extends AbstractExecuteTask<FetchStudentAppContext> {

    @Inject private GlobalTagServiceClient client;

    @Override
    public void execute(FetchStudentAppContext context) {

        context.setWhite(client.getGlobalTagBuffer().findByName(GlobalTagName.PaymentWhiteListUsers.name())
                .stream()
                .filter(p -> Objects.equals(context.getStudentId(), SafeConverter.toLong(p.getTagValue())))
                .findFirst()
                .orElse(null) != null);

    }
}
