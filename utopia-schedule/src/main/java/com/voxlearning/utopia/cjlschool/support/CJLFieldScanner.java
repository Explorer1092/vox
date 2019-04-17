package com.voxlearning.utopia.cjlschool.support;

import com.voxlearning.alps.annotation.common.Singleton;
import com.voxlearning.alps.core.util.FieldAccessor;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.utopia.service.mizar.api.constants.cjlschool.CJLField;
import org.slf4j.Logger;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * For scanning all document fields of CJL E-School System return entity(document).
 * Created by Yuechen.Wang on 2017/7/10.
 */
@Singleton
public class CJLFieldScanner {
    private static final Logger logger = LoggerFactory.getLogger(CJLFieldScanner.class);

    private final CJLFieldNameResolver nameResolver;

    public CJLFieldScanner(CJLFieldNameResolver nameResolver) {
        this.nameResolver = Objects.requireNonNull(nameResolver);
    }

    public CIFDocumentAnalysis scan(Class<?> beanClass) {
        Objects.requireNonNull(beanClass);
        List<FieldAccessor> fields = Stream.of(beanClass.getDeclaredFields())
                .map(FieldAccessor::new)
                .collect(Collectors.toList());
        if (fields.stream().map(FieldAccessor::getFieldType).anyMatch(Class::isPrimitive)) {
            String errMsg = String.format("Primitive fields not allowed: [%s]", beanClass.getName());
            logger.error(errMsg);
            throw new UnsupportedOperationException(errMsg);
        }

        CIFDocumentAnalysis analysis = new CIFDocumentAnalysis();

        analysis.normalFields = fields.stream()
                .filter(this::check)
                .map(f -> new CJLMapperField(nameResolver, f))
                .collect(Collectors.toList());

        return analysis;
    }

    private boolean check(FieldAccessor field) {
        return field.isAnnotationPresent(CJLField.class);
    }

    /**
     * Analysis report of TalkFun entity scanning
     */
    public class CIFDocumentAnalysis {
        public List<CJLMapperField> normalFields;
    }
}
