package com.voxlearning.utopia.service.newhomework.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.newhomework.api.service.AbilityExamService;
import lombok.Getter;

/**
 * @author lei.liu
 * @version 18-11-2
 */
@Getter
public class AbilityExamServiceLoader implements IPingable {

    @ImportService(interfaceClass = AbilityExamService.class)
    private AbilityExamService hydraRemoteReference;

}
