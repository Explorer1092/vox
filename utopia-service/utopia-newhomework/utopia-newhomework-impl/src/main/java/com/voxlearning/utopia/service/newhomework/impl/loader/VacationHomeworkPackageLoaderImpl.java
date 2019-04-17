package com.voxlearning.utopia.service.newhomework.impl.loader;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.newhomework.api.VacationHomeworkPackageLoader;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkPackage;
import com.voxlearning.utopia.service.newhomework.impl.dao.vacation.VacationHomeworkPackageDao;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Named
@Service(interfaceClass = VacationHomeworkPackageLoader.class)
@ExposeService(interfaceClass = VacationHomeworkPackageLoader.class)
public class VacationHomeworkPackageLoaderImpl implements VacationHomeworkPackageLoader {
    @Inject
    private VacationHomeworkPackageDao vacationHomeworkPackageDao;

    @Override
    public VacationHomeworkPackage loadVacationHomeworkPackageById(String packageId) {
        if (StringUtils.isBlank(packageId)) {
            return null;
        }
        return vacationHomeworkPackageDao.load(packageId);
    }

    @Override
    public Map<Long, List<VacationHomeworkPackage.Location>> loadVacationHomeworkPackageByClazzGroupIds(Collection<Long> groupIds) {
        if (CollectionUtils.isEmpty(groupIds)){
            return Collections.emptyMap();
        }
        return vacationHomeworkPackageDao.loadVacationHomeworkPackageByClazzGroupIds(groupIds);
    }
}
