package com.voxlearning.utopia.service.campaign.impl.support;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.random.RandomUtils;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class EternalLifeIterator<T> {

    private List<Collection<T>> originList;
    private Iterator<Collection<T>> listIterator;
    private Iterator<T> currentListIterator;

    public EternalLifeIterator(List<Collection<T>> originList) {
        init(originList);
    }

    public EternalLifeIterator(List<Collection<T>> originList, boolean randomOffset) {
        init(originList);
        offsetPoint(randomOffset ? RandomUtils.nextInt(originList.get(0).size() - 1) : 0);
    }

    public EternalLifeIterator(List<Collection<T>> originList, int offset) {
        init(originList);
        offsetPoint(offset);
    }

    private void init(List<Collection<T>> originList) {
        if (CollectionUtils.isEmpty(originList)) {
            throw new IllegalArgumentException();
        }
        for (Collection<T> ts : originList) {
            if (CollectionUtils.isEmpty(ts)) {
                throw new IllegalArgumentException();
            }
        }

        this.originList = originList;
        this.listIterator = originList.iterator();
        this.currentListIterator = listIterator.next().iterator();
    }

    private void offsetPoint(int offset) {
        for (int i = 0; i < offset; i++) {
            next();
        }
    }

    public T next() {
        if (currentListIterator.hasNext()) {
            return currentListIterator.next();
        } else {
            if (listIterator.hasNext()) {
                currentListIterator = listIterator.next().iterator();
            } else {
                listIterator = originList.iterator();
            }
            return next();
        }
    }
}
