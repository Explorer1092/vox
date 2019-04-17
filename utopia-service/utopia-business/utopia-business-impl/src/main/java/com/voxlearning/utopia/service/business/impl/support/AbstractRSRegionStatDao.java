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
import com.voxlearning.utopia.service.business.impl.support.mode1.AbstractRSRegionStatDao1;
import com.voxlearning.utopia.service.business.impl.support.mode2.AbstractRSRegionStatDao2;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

abstract public class AbstractRSRegionStatDao<E extends Serializable,
        D1 extends AbstractRSRegionStatDao1<E>, D2 extends AbstractRSRegionStatDao2<E>>
        extends SpringContainerSupport {

    private final Class<AbstractRSRegionStatDao1<E>> d1Class;
    private final Class<AbstractRSRegionStatDao2<E>> d2Class;

    @SuppressWarnings("unchecked")
    protected AbstractRSRegionStatDao() {
        Class<?> theClass = ClassUtils.filterCglibProxyClass(getClass());
        Type genericSuperClass = theClass.getGenericSuperclass();
        ParameterizedType type = (ParameterizedType) genericSuperClass;
        Type[] typeArgs = type.getActualTypeArguments();

        d1Class = (Class<AbstractRSRegionStatDao1<E>>) typeArgs[1];
        d2Class = (Class<AbstractRSRegionStatDao2<E>>) typeArgs[2];
    }

    protected AbstractRSRegionStatDao1<E> dao1;
    protected AbstractRSRegionStatDao2<E> dao2;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        dao1 = getBean(d1Class);
        dao2 = getBean(d2Class);
    }

    public List<E> findByCityCode(Long cityCode) {
        return findByCityCode(cityCode, null, null);
    }

    public List<E> findByCityCode(Long cityCode, Integer year, Term term) {
        if (year == null || term == null) {
            return dao1.findByCityCode(cityCode);
        } else {
            return dao2.findByCityCode(cityCode, year, term);
        }
    }

    public List<E> findByCityCodes(Collection<Long> cityCodes) {
        return findByCityCodes(cityCodes, null, null);
    }

    public List<E> findByCityCodes(Collection<Long> cityCodes, Integer year, Term term) {
        if (year == null || term == null) {
            return dao1.findByCityCodes(cityCodes);
        } else {
            return dao2.findByCityCodes(cityCodes, year, term);
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
            String id = year + "." + term.getKey() + "." + RandomUtils.nextObjectId();

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
