package com.voxlearning.utopia.service.mizar.talkfun;

import com.voxlearning.alps.annotation.common.Singleton;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.mizar.api.mapper.talkfun.TkField;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * For resolving mapping name of TalkFun document field.
 * Created by Yuechen.Wang on 2017/1/10.
 */
@Singleton
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TKFieldNameResolver {
    @Getter static final TKFieldNameResolver instance = new TKFieldNameResolver();

    public String resolve(TKDataField accessor) {
        TkField tkField = accessor.getAnnotation(TkField.class);
        if (tkField != null && StringUtils.isNotBlank(tkField.value())) {
            return tkField.value().trim();
        }
        return accessor.getAccessor().getFieldName();
    }
}
