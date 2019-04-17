package com.voxlearning.utopia.service.mizar.talkfun;

import com.voxlearning.alps.annotation.common.Singleton;
import com.voxlearning.alps.core.util.FieldAccessor;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.utopia.service.mizar.api.mapper.talkfun.TkField;
import org.slf4j.Logger;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * For scanning all document fields of TalkFun return entity(document).
 * Created by Yuechen.Wang on 2017/1/10.
 */
@Singleton
public class TKFieldScanner {
    private static final Logger logger = LoggerFactory.getLogger(TKFieldScanner.class);

    private final TKFieldNameResolver nameResolver;

    public TKFieldScanner(TKFieldNameResolver nameResolver) {
        this.nameResolver = Objects.requireNonNull(nameResolver);
    }


    public TKDocumentAnalysis scan(Class<?> beanClass) {
        Objects.requireNonNull(beanClass);
        List<FieldAccessor> fields = Stream.of(beanClass.getDeclaredFields())
                .map(FieldAccessor::new)
                .collect(Collectors.toList());
        if (fields.stream().map(FieldAccessor::getFieldType).anyMatch(Class::isPrimitive)) {
            String errMsg = "Primitive fields not allowed: [%s]";
            errMsg = String.format(errMsg, beanClass.getName());
            logger.error(errMsg);
            throw new UnsupportedOperationException(errMsg);
        }

        TKDocumentAnalysis analysis = new TKDocumentAnalysis();

        analysis.timeFields = fields.stream()
                .filter(f -> check(f, true))
                .map(f -> new TKDataField(nameResolver, f))
                .collect(Collectors.toList());

        analysis.normalFields = fields.stream()
                .filter(f -> check(f, false))
                .map(f -> new TKDataField(nameResolver, f))
                .collect(Collectors.toList());

        return analysis;
    }

    private boolean check(FieldAccessor field, boolean mark) {
        return field.isAnnotationPresent(TkField.class)
                && Boolean.valueOf(mark).equals(field.getField().getAnnotation(TkField.class).timestamp());
    }

    /**
     * Analysis report of TalkFun entity scanning
     */
    public class TKDocumentAnalysis {
        public List<TKDataField> timeFields;
        public List<TKDataField> normalFields;
    }
}
