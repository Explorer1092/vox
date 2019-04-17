package com.voxlearning.utopia.cjlschool.support;

import com.voxlearning.alps.core.util.FieldAccessor;
import lombok.Getter;

import java.lang.annotation.Annotation;
import java.util.Objects;

/**
 * Created by Yuechen.Wang on 2017/7/20.
 */
public class CJLMapperField {
    private final CJLFieldNameResolver nameResolver;
    @Getter private final FieldAccessor accessor;


    CJLMapperField(CJLFieldNameResolver nameResolver, FieldAccessor accessor) {
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
