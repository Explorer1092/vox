package com.voxlearning.utopia.service.dubbing.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.utopia.service.dubbing.api.DubbingRawService;
import com.voxlearning.utopia.service.dubbing.api.entity.DubbingRaw;
import com.voxlearning.utopia.service.dubbing.impl.dao.DubbingRawDao;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * @Author: wei.jiang
 * @Date: Created on 2017/10/12
 */
@Named
@ExposeService(interfaceClass = DubbingRawService.class)
public class DubbingRawServiceImpl implements DubbingRawService {

    @Inject
    private DubbingRawDao dubbingRawDao;

    @Override
    public void upsertDubbingRaw(DubbingRaw dubbingRaw) {
        dubbingRawDao.upsert(dubbingRaw);
    }

    @Override
    public List<DubbingRaw> exportDubbingRaw() {
        return dubbingRawDao.query();
    }

    @Override
    public DubbingRaw loadById(String id) {
        return dubbingRawDao.load(id);
    }
}
