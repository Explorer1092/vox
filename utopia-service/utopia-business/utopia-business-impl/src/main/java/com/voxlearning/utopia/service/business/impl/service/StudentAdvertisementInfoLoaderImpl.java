package com.voxlearning.utopia.service.business.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.business.api.StudentAdvertisementInfoLoader;
import com.voxlearning.utopia.service.business.api.entity.StudentAdvertisementInfo;
import com.voxlearning.utopia.service.business.impl.dao.StudentAdvertisementInfoDao;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * @author janko
 * @date 2016/10/30
 * @desc
 */
@Named
@Service(interfaceClass = StudentAdvertisementInfoLoader.class)
@ExposeService(interfaceClass = StudentAdvertisementInfoLoader.class)
public class StudentAdvertisementInfoLoaderImpl extends SpringContainerSupport implements StudentAdvertisementInfoLoader{

	@Inject StudentAdvertisementInfoDao studentAdvertisementInfoDao;

	@Override
	public List<StudentAdvertisementInfo> loadByUserId(Long userId) {
		return studentAdvertisementInfoDao.findByUserId(userId);
	}
}
