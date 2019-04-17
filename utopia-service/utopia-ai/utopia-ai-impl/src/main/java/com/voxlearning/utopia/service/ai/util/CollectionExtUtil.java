package com.voxlearning.utopia.service.ai.util;

import com.voxlearning.alps.core.util.CollectionUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class CollectionExtUtil {

    public static boolean hasIntersection(List<String> list1, List<String> list2) {
        if (CollectionUtils.isEmpty(list1) || CollectionUtils.isEmpty(list2)) {
            return false;
        }
        Set<String> set1 = list1.stream().collect(Collectors.toSet());
        Set<String> set2 = list2.stream().collect(Collectors.toSet());
        Set<String> set = new HashSet<>();
        set.addAll(set1);
        set.addAll(set2);
        return set.size() != (set1.size() + set2.size());
    }

    public static boolean hasIntersection(Set<String> set1, List<String> list2) {
        if (CollectionUtils.isEmpty(set1) || CollectionUtils.isEmpty(list2)) {
            return false;
        }
        Set<String> set2 = list2.stream().collect(Collectors.toSet());
        Set<String> set = new HashSet<>();
        set.addAll(set1);
        set.addAll(set2);
        return set.size() != (set1.size() + set2.size());
    }

    public static boolean equivalent(Collection<String> collection1, Collection<String> collection2) {
        if (collection1 == null && collection2 == null) {
            return true;
        }

        if (collection1 == null && collection2 != null || (collection1 != null && collection2 == null)) {
            return false;
        }

        Set<String> set1 = collection1.stream().collect(Collectors.toSet());
        Set<String> set2 = collection2.stream().collect(Collectors.toSet());
        if (set1.size() != set2.size()) {
            return false;
        }

        Set<String> set = new HashSet<>();
        set.addAll(set1);
        set.addAll(set2);
        return set.size() == set1.size();
    }
}
