package com.voxlearning.utopia.service.ai.client;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.api.buffer.ManagedNearBuffer;
import com.voxlearning.alps.api.buffer.NearBufferBuilder;
import com.voxlearning.utopia.service.ai.api.AiChipsEnglishConfigService;
import com.voxlearning.utopia.service.ai.buffer.ChipsEnglishConfigBuffer;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishPageContentConfig;
import lombok.Getter;
import org.springframework.beans.factory.InitializingBean;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author guangqing
 * @since 2018/9/10
 */
public class AiChipsEnglishConfigServiceClient implements InitializingBean {

    @Getter
    @ImportService(interfaceClass = AiChipsEnglishConfigService.class)
    private AiChipsEnglishConfigService remoteReference;

    private ManagedNearBuffer<List<ChipsEnglishPageContentConfig>, ChipsEnglishConfigBuffer> chipsEnglishConfigBuffer;

    @Override
    public void afterPropertiesSet() throws Exception {
        NearBufferBuilder<List<ChipsEnglishPageContentConfig>, ChipsEnglishConfigBuffer> builder = NearBufferBuilder.newBuilder();
        builder.nearBufferClass(ChipsEnglishConfigBuffer.class);
        builder.reloadNearBuffer(2, TimeUnit.MINUTES);
        builder.eagerInitUnderProduction(true);
        builder.initializeNearBuffer(() -> remoteReference.loadChipsEnglishConfigBufferData(-1L));
        builder.reloadNearBuffer((version, attributes) -> remoteReference.loadChipsEnglishConfigBufferData(version));
        chipsEnglishConfigBuffer = builder.build();
    }

    public List<ChipsEnglishPageContentConfig> loadAllChipsEnglishConfig() {
        return chipsEnglishConfigBuffer.getNativeBuffer().dump().getData();
    }

    public ChipsEnglishPageContentConfig loadChipsEnglishConfigByName(String name) {
        return chipsEnglishConfigBuffer.getNativeBuffer().toMap().get(name);
    }

}
