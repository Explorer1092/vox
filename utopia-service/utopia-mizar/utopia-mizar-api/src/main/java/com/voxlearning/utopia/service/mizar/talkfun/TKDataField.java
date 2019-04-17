package com.voxlearning.utopia.service.mizar.talkfun;

import com.voxlearning.alps.core.util.FieldAccessor;
import lombok.Getter;

import java.lang.annotation.Annotation;
import java.util.Objects;

/**
 * Created by Yuechen.Wang on 2017/1/10.
 */
public class TKDataField {
    private final TKFieldNameResolver nameResolver;
    @Getter private final FieldAccessor accessor;


    TKDataField(TKFieldNameResolver nameResolver, FieldAccessor accessor) {
        this.nameResolver = Objects.requireNonNull(nameResolver);
        this.accessor = Objects.requireNonNull(accessor);
    }

    public String getName() {
        return nameResolver.resolve(this);
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return accessor.getField().getAnnotation(annotationClass);
    }

    public void setValue(Object obj, Object value) {
        accessor.set(obj, value);
    }

    @Override
    public String toString() {
        return accessor.toString();
    }
}
