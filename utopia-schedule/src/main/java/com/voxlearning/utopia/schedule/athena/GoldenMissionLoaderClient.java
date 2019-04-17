package com.voxlearning.utopia.schedule.athena;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.athena.api.recom.loader.GoldenMissionLoader;
import lombok.Getter;

import javax.inject.Named;

/**
 * @author xuesong.zhang
 * @since 2017/7/18
 */
@Named
public class GoldenMissionLoaderClient {

    @Getter
    @ImportService(interfaceClass = GoldenMissionLoader.class)
    private GoldenMissionLoader goldenMissionLoader;
}
