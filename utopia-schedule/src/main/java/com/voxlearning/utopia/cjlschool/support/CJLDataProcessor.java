package com.voxlearning.utopia.cjlschool.support;

import com.unitever.cif.core.message.CIFDataEntity;
import com.unitever.cif.core.message.CIFDataField;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.mizar.api.constants.cjlschool.CJLDataEntity;
import com.voxlearning.utopia.service.mizar.consumer.service.CJLSyncDataServiceClient;
import com.voxlearning.utopia.service.user.consumer.*;
import com.voxlearning.utopia.service.user.consumer.client.kuailexue.NewKuailexueServiceClient;

import javax.inject.Inject;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Created by Yuechen.Wang on 2017/7/19.
 */
abstract public class CJLDataProcessor<M extends CJLDataEntity> extends SpringContainerSupport {

    @Inject protected CJLSyncDataServiceClient cjlSyncDataServiceClient;
    @Inject private CommonConfigServiceClient commonConfigServiceClient;
    @Inject protected TeacherLoaderClient teacherLoaderClient;
    @Inject protected UserLoaderClient userLoaderClient;
    @Inject protected UserServiceClient userServiceClient;

    /**
     * 请求数据 和 新增数据的同步
     */
    public abstract MapMessage sync(List<CIFDataEntity> data);

    /**
     * 变更处理
     */
    public abstract MapMessage modify(List<CIFDataEntity> data);

    @SuppressWarnings("unchecked")
    protected M convert(CIFDataEntity data) {
        if (data == null) {
            return null;
        }
        try {
            Class<M> mapper = (Class<M>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
            // 处理 传进来的Class
            CJLFieldScanner.CIFDocumentAnalysis analysis = new CJLFieldScanner(CJLFieldNameResolver.getInstance()).scan(mapper);
            if (analysis == null) {
                return null;
            }
            // 根据Map的key去映射生成实体
            Map<String, Object> dataMap = new LinkedHashMap();
            for (CIFDataField field : data.getFields()) {
                dataMap.put(field.getName().trim(), field.getValue());
            }

            // 根据Map的key去映射生成实体
            M bean = mapper.newInstance();
            // 处理普通字段
            analysis.normalFields.forEach(f -> f.setValue(bean, dataMap.get(f.getName())));
            return bean;
        } catch (Exception ex) {
            logger.error("Failed to parse CIF Data return data", ex);
            return null;
        }
    }

    protected Map<String, Long> getSchoolIdMapping() {
        String schoolMapConfig = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(
                ConfigCategory.MIDDLE_PLATFORM_GENERAL.getType(), "CJL_SCHOOL_MAP"
        );

        Map<String, Long> schoolIdMap = new HashMap<>();

        Stream.of(schoolMapConfig.split(",")).forEach(pair -> {
            String[] split = pair.split(":");
            schoolIdMap.put(split[0], SafeConverter.toLong(split[1]));
        });
        return schoolIdMap;
    }

}
