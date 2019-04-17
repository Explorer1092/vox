package com.voxlearning.utopia.service.mizar.api.constants.cjlschool;

import com.voxlearning.utopia.service.mizar.api.entity.cjlschool.CJLTeacher;

import java.util.List;

/**
 * Created by Yuechen.Wang on 2017/7/26.
 */
public interface CJLDataDao<E extends CJLDataEntity> {

    E syncOne(E entity);

    void syncBatch(List<E> entityList);

    List<E> $findAllForJob();
}
