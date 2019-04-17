package com.voxlearning.utopia.cjlschool.support;

import com.voxlearning.alps.annotation.common.Singleton;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.mizar.api.constants.cjlschool.CJLField;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * For resolving mapping name of CJLDataEntity document field.
 * Created by Yuechen.Wang on 2017/1/10.
 */
@Singleton
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CJLFieldNameResolver {
    @Getter static final CJLFieldNameResolver instance = new CJLFieldNameResolver();

    public String resolve(CJLMapperField accessor) {
        CJLField field = accessor.getAnnotation(CJLField.class);
        if (field != null && StringUtils.isNotBlank(field.field())) {
            return field.field().trim();
        }
        return accessor.getAccessor().getFieldName();
    }
}
