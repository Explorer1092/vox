/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.business.impl.support;

import com.voxlearning.alps.annotation.meta.Term;
import com.voxlearning.alps.core.util.FieldAccessor;
import com.voxlearning.alps.core.util.ReflectionUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.util.ClassUtils;
import com.voxlearning.utopia.service.business.impl.support.mode1.AbstractRSSchoolStatDao1;
import com.voxlearning.utopia.service.business.impl.support.mode2.AbstractRSSchoolStatDao2;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

abstract public class AbstractRSSchoolStatDao<E extends Serializable,
        D1 extends AbstractRSSchoolStatDao1<E>, D2 extends AbstractRSSchoolStatDao2<E>>
        extends SpringContainerSupport {

    private final Class<AbstractRSSchoolStatDao1<E>> d1Class;
    private final Class<AbstractRSSchoolStatDao2<E>> d2Class;

    @SuppressWarnings("unchecked")
    public AbstractRSSchoolStatDao() {
        Class<?> theClass = ClassUtils.filterCglibProxyClass(getClass());
        Type genericSuperClass = theClass.getGenericSuperclass();
        ParameterizedType type = (ParameterizedType) genericSuperClass;
        Type[] typeArgs = type.getActualTypeArguments();

        d1Class = (Class<AbstractRSSchoolStatDao1<E>>) typeArgs[1];
        d2Class = (Class<AbstractRSSchoolStatDao2<E>>) typeArgs[2];
    }

    protected AbstractRSSchoolStatDao1<E> dao1;
    protected AbstractRSSchoolStatDao2<E> dao2;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        dao1 = getBean(d1Class);
        dao2 = getBean(d2Class);
    }

    public List<E> findByAreaCode(Long cityCode) {
        return findByAreaCode(cityCode, null, null);
    }

    public List<E> findByAreaCode(Long cityCode, Integer year, Term term) {
        if (year == null || term == null) {
            return dao1.findByAreaCode(cityCode);
        } else {
            return dao2.findByAreaCode(cityCode, year, term);
        }
    }

    public List<E> findByAreaCodes(Collection<Long> cityCodes) {
        return findByAreaCodes(cityCodes, null, null);
    }

    public List<E> findByAreaCodes(Collection<Long> cityCodes, Integer year, Term term) {
        if (year == null || term == null) {
            return dao1.findByAreaCodes(cityCodes);
        } else {
            return dao2.findByAreaCodes(cityCodes, year, term);
        }
    }

    public List<E> findBySchoolIds(Collection<Long> schoolIds, Integer year, Term term) {
        if (year == null || term == null) {
            return dao1.findBySchoolIds(schoolIds);
        } else {
            return dao2.findBySchoolIds(schoolIds, year, term);
        }
    }

    public long countByCityCodes(Collection<Long> cityCodes, Integer year, Term term) {
        if (year == null || term == null) {
            return dao1.countByCityCodes(cityCodes);
        } else {
            return dao2.countByCityCodes(cityCodes, year, term);
        }
    }

    public long countByCityCodesAndIsValid(Collection<Long> cityCodes, Boolean isValid, Integer year, Term term) {
        if (year == null || term == null) {
            return dao1.countByCityCodesAndIsValid(cityCodes, isValid);
        } else {
            return dao2.countByCityCodesAndIsValid(cityCodes, isValid, year, term);
        }
    }

    public long countByCityCode(Long cityCide, Integer year, Term term) {
        if (year == null || term == null) {
            return dao1.countByCityCode(cityCide);
        } else {
            return dao2.countByCityCode(cityCide, year, term);
        }
    }

    public long countByAreaCodeAndIsValid(Long areaCode, Boolean isValid, Integer year, Term term) {
        if (year == null || term == null) {
            return dao1.countByAreaCodeAndIsValid(areaCode, isValid);
        } else {
            return dao2.countByAreaCodeAndIsValid(areaCode, isValid, year, term);
        }
    }

    public long countByAreaCodes(Collection<Long> areaCodes, Integer year, Term term) {
        if (year == null || term == null) {
            return dao1.countByAreaCodes(areaCodes);
        } else {
            return dao2.countByAreaCodes(areaCodes, year, term);
        }
    }

    public long countByAreaCodesAndIsValid(Collection<Long> areaCodes, Boolean isValid, Integer year, Term term) {
        if (year == null || term == null) {
            return dao1.countByAreaCodesAndIsValid(areaCodes, isValid);
        } else {
            return dao2.countByAreaCodesAndIsValid(areaCodes, isValid, year, term);
        }
    }

    public long countBySchoolIds(Collection<Long> schoolIds, Integer year, Term term) {
        if (year == null || term == null) {
            return dao1.countBySchoolIds(schoolIds);
        } else {
            return dao2.countBySchoolIds(schoolIds, year, term);
        }
    }

    public long countBySchoolIdsAndIsValid(Collection<Long> schoolIds, Boolean isValid, Integer year, Term term) {
        if (year == null || term == null) {
            return dao1.countBySchoolIdsAndIsValid(schoolIds, isValid);
        } else {
            return dao2.countBySchoolIdsAndIsValid(schoolIds, isValid, year, term);
        }
    }


    /**
     * only used for UT, don't use this method in code
     */
    public E save(E entity) {
        return save(entity, null, null);
    }

    /**
     * only used for UT, don't use this method in code
     */
    public E save(E entity, Integer year, Term term) {
        if (entity == null) {
            return null;
        }
        if (year != null && term != null) {
            String id = year + "" + term.getKey() + "." + RandomUtils.nextObjectId();

            ReflectionUtils.ClassField field = ReflectionUtils.lookupField(entity.getClass(), "id");
            FieldAccessor accessor = new FieldAccessor(field.getClazz(), field.getField());
            accessor.set(entity, id);

            dao2.insert(entity);
        } else {
            dao1.insert(entity);
        }
        return entity;
    }
}
