package com.voxlearning.utopia.service.newhomework.impl.support;

import net.sf.cglib.beans.BeanCopier;

import java.util.HashMap;
import java.util.Map;

/**
 * @author xuesong.zhang
 * @since 2017/10/10
 */
public class HomeworkCopyUtils {
    private static Map<String, BeanCopier> beanCopierMap = new HashMap<>();

    public static void copyProperties(Object target, Object source) {
        String beanKey = generateKey(source.getClass(), target.getClass());
        BeanCopier copier;
        if (!beanCopierMap.containsKey(beanKey)) {
            copier = BeanCopier.create(source.getClass(), target.getClass(), false);
            beanCopierMap.put(beanKey, copier);
        } else {
            copier = beanCopierMap.get(beanKey);
        }
        copier.copy(source, target, null);
    }

    private static String generateKey(Class<?> class1, Class<?> class2) {
        return class1.toString() + class2.toString();
    }
}
