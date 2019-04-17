package com.voxlearning.utopia.service.business.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.business.api.StudentAdvertisementInfoLoader;
import com.voxlearning.utopia.service.business.api.entity.StudentAdvertisementInfo;

import java.util.List;

/**
 * @author peng.zhang.a
 * @date 2016/10/30
 * @desc
 */
public class StudentAdvertisementInfoLoaderClient implements StudentAdvertisementInfoLoader {

    @ImportService(interfaceClass = StudentAdvertisementInfoLoader.class)
    private StudentAdvertisementInfoLoader remoteReference;

    @Override

    public List<StudentAdvertisementInfo> loadByUserId(Long userId) {

        return remoteReference.loadByUserId(userId);
    }
}
