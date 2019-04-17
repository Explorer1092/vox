package com.voxlearning.utopia.service.newhomework.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.newhomework.api.VacationHomeworkPackageLoader;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkPackage;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class VacationHomeworkPackageLoaderClient implements VacationHomeworkPackageLoader {

    @ImportService(interfaceClass = VacationHomeworkPackageLoader.class)
    private VacationHomeworkPackageLoader remoteReference;

    @Override
    public VacationHomeworkPackage loadVacationHomeworkPackageById(String packageId) {
        return remoteReference.loadVacationHomeworkPackageById(packageId);
    }


    public Map<Long, List<VacationHomeworkPackage.Location>> loadVacationHomeworkPackageByClazzGroupIds(Collection<Long> groupIds){
        return remoteReference.loadVacationHomeworkPackageByClazzGroupIds(groupIds);
    }
}
