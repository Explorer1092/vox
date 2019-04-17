package com.voxlearning.luffy.controller.chips;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.luffy.controller.AbstractXcxController;
import com.voxlearning.utopia.service.ai.api.ChipsWechatUserLoader;
import com.voxlearning.utopia.service.ai.constant.ChipsErrorType;
import com.voxlearning.utopia.service.ai.constant.WechatUserType;
import com.voxlearning.utopia.service.ai.data.ChipsWechatUser;

import java.util.function.Consumer;

public abstract class AbstractChipsController extends AbstractXcxController {

    @ImportService(interfaceClass = ChipsWechatUserLoader.class)
    protected ChipsWechatUserLoader wechatUserLoader;

    @Override
    protected MapMessage wrapper(Consumer<MapMessage> wrapper) {
        MapMessage mm = MapMessage.successMessage();
        try {
            wrapper.accept(mm);
        } catch (DuplicatedOperationException e) {
            mm = MapMessage.errorMessage(ChipsErrorType.DUPLICATED_OPERATION.getInfo()).setErrorCode(ChipsErrorType.DUPLICATED_OPERATION.getCode());
        } catch (Exception e) {
            mm = MapMessage.errorMessage(ChipsErrorType.SERVER_ERROR.getInfo()).setErrorCode(ChipsErrorType.SERVER_ERROR.getCode());
        }
        return mm;
    }

    protected ChipsWechatUser getWechatUser() {
        String openId = SafeConverter.toString(getRequest().getAttribute("openId"));
        return wechatUserLoader.loadByOpenId(openId, WechatUserType.CHIPS_MINI_PROGRAM.name());
    }

    protected MapMessage failMapMessage(ChipsErrorType errorType) {
        if (errorType == null) {
            errorType = ChipsErrorType.SERVER_ERROR;
        }
        return MapMessage.errorMessage(errorType.getInfo()).setErrorCode(errorType.getCode());
    }
}
