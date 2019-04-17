package com.voxlearning.utopia.service.ai.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ExposeServices;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.buffer.ManagedNearBuffer;
import com.voxlearning.alps.api.buffer.NearBufferBuilder;
import com.voxlearning.alps.api.buffer.VersionedBufferData;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.utopia.service.ai.api.AiChipsEnglishConfigService;
import com.voxlearning.utopia.service.ai.buffer.ChipsEnglishConfigBuffer;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishPageContentConfig;
import com.voxlearning.utopia.service.ai.impl.persistence.ChipsEnglishConfigVersion;
import com.voxlearning.utopia.service.ai.impl.persistence.ChipsEnglishPageContentConfigDao;
import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author guangqing
 * @since 2018/9/10
 */
@Named
@ExposeServices({
        @ExposeService(interfaceClass = AiChipsEnglishConfigService.class, version = @ServiceVersion(version = "20180910")),
        @ExposeService(interfaceClass = AiChipsEnglishConfigService.class, version = @ServiceVersion(version = "20181116"))
})
public class AiChipsEnglishConfigServiceImpl implements AiChipsEnglishConfigService, InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(AiChipsEnglishConfigServiceImpl.class);

    @Inject
    private ChipsEnglishPageContentConfigDao chipsEnglishPageContentConfigDao;

    @Inject
    private ChipsEnglishConfigVersion chipsEnglishConfigVersion;

    private ManagedNearBuffer<List<ChipsEnglishPageContentConfig>, ChipsEnglishConfigBuffer> chipsEnglishConfigBuffer;

    @Override
    public void afterPropertiesSet() {
        NearBufferBuilder<List<ChipsEnglishPageContentConfig>, ChipsEnglishConfigBuffer> builder = NearBufferBuilder.newBuilder();
        builder.name("ChipsEnglishConfigBuffer");
        builder.category("SERVER");
        builder.nearBufferClass(ChipsEnglishConfigBuffer.class);
        builder.reloadNearBuffer(5, TimeUnit.MINUTES);
        builder.eagerInitUnderProduction(true);
        builder.initializeNearBuffer(() -> {
            long version = chipsEnglishConfigVersion.current();
            List<ChipsEnglishPageContentConfig> list = chipsEnglishPageContentConfigDao.query();
            return new VersionedBufferData<>(version, list);
        });
        builder.reloadNearBuffer((oldVersion, attributes) -> {
            long currentVersion = chipsEnglishConfigVersion.current();
            if (oldVersion < currentVersion) {
                List<ChipsEnglishPageContentConfig> list = chipsEnglishPageContentConfigDao.query();
                return new VersionedBufferData<>(currentVersion, list);
            }
            return null;
        });
        chipsEnglishConfigBuffer = builder.build();
    }


    @Override
    public MapMessage addChipsEnglishPageContentConfig(String name, String value, String memo) {
        try {
            Boolean result = AtomicCallbackBuilderFactory.getInstance()
                    .<Boolean>newBuilder()
                    .keyPrefix("createChipsEnglishPageContentConfig")
                    .expirationInSeconds(80)
                    .keys(name)
                    .callback(() -> {
                        ChipsEnglishPageContentConfig temp = chipsEnglishConfigBuffer.getNativeBuffer().toMap().get(name);
                        if (temp != null) {
                            return false;
                        }
                        ChipsEnglishPageContentConfig config = new ChipsEnglishPageContentConfig();
                        config.setName(name);
                        config.setValue(value);
                        config.setMemo(memo);
                        config.setCreateTime(new Date());
                        config.setUpdateTime(new Date());
                        config.setDisabled(false);
                        chipsEnglishPageContentConfigDao.insert(config);
                        chipsEnglishConfigVersion.increment();
                        return true;
                    })
                    .build()
                    .execute();
            if (result) {
                return MapMessage.successMessage();
            } else {
                return MapMessage.errorMessage().add("info", "this name has bean used");
            }
        } catch (Exception e) {
            logger.error("call addChipsEnglishPageContentConfig error. name:{}, value:{}", name, value, e);
            return MapMessage.errorMessage().add("info", e.getMessage());
        }
    }

    @Override
    public MapMessage updateChipsEnglishPageContentConfig(String id, String name, String value, String memo) {
        ChipsEnglishPageContentConfig config = new ChipsEnglishPageContentConfig();
        config.setId(id);
        config.setName(name);
        config.setValue(value);
        config.setMemo(memo);
        config.setDisabled(false);
        config.setUpdateTime(new Date());
        chipsEnglishPageContentConfigDao.upsert(config);
        chipsEnglishConfigVersion.increment();
        return MapMessage.successMessage();
    }

    @Override
    public boolean deleteChipsEnglishPageContentConfig(String id) {
        chipsEnglishPageContentConfigDao.remove(id);
        chipsEnglishConfigVersion.increment();
        return true;
    }

    // 字典配置CRM 不走buffer
    @Override
    public ChipsEnglishPageContentConfig loadChipsEnglishConfigById(String id) {
        return chipsEnglishPageContentConfigDao.load(id);
    }

    @Override
    public VersionedBufferData<List<ChipsEnglishPageContentConfig>> loadChipsEnglishConfigBufferData(Long version) {
        ChipsEnglishConfigBuffer nativeBuffer = chipsEnglishConfigBuffer.getNativeBuffer();
        if (version < 0 || version < nativeBuffer.getVersion()) {
            return nativeBuffer.dump();
        }
        return null;
    }

    // 字典配置CRM 不走buffer
    @Override
    public List<ChipsEnglishPageContentConfig> loadAllChipsConfig4Crm() {
        return chipsEnglishPageContentConfigDao.query();
    }

    public ChipsEnglishPageContentConfig loadChipsConfigByName(String name) {
        return chipsEnglishConfigBuffer.getNativeBuffer().toMap().get(name);
    }

    public List<ChipsEnglishPageContentConfig> loadAllChipsConfig() {
        return chipsEnglishConfigBuffer.getNativeBuffer().dump().getData();
    }
}
