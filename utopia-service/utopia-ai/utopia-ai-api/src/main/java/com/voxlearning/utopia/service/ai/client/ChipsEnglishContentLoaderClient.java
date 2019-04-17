package com.voxlearning.utopia.service.ai.client;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.ai.api.ChipsEnglishContentLoader;
import lombok.Getter;

/**
 * @author guangqing
 * @since 2018/10/23
 */
public class ChipsEnglishContentLoaderClient {
    @Getter
    @ImportService(interfaceClass = ChipsEnglishContentLoader.class)
    ChipsEnglishContentLoader remoteReference;
}
