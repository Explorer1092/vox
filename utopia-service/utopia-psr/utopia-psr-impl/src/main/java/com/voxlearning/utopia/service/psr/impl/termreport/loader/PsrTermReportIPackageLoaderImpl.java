package com.voxlearning.utopia.service.psr.impl.termreport.loader;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.psr.entity.termreport.GroupUnitReportPackage;
import com.voxlearning.utopia.service.psr.entity.termreport.TermReportPackage;
import com.voxlearning.utopia.service.psr.impl.termreport.service.PsrTermReportIPackageService;
import com.voxlearning.utopia.service.psr.termreport.loader.PsrTermReportIPackageLoader;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.TimeUnit;

/**
 * Created by 17ZY-HPYKFD2 on 2016/10/20.
 */
@Named
@ExposeService(interfaceClass = PsrTermReportIPackageLoader.class)
public class PsrTermReportIPackageLoaderImpl implements PsrTermReportIPackageLoader {
    @Inject private PsrTermReportIPackageService psrTermReportIPackageService;
    private static final GroupUnitReportPackage defaultRes = new GroupUnitReportPackage();
    private static Cache<String, GroupUnitReportPackage> cacheGroupUnitPackage = CacheBuilder.newBuilder().maximumSize(1000).initialCapacity(600).expireAfterAccess(1, TimeUnit.HOURS).build();

    private static final TermReportPackage defaultResMnonth = new TermReportPackage();
    private static Cache<String, TermReportPackage> cacheGroupPackageMonth = CacheBuilder.newBuilder().maximumSize(1000).initialCapacity(600).expireAfterAccess(1, TimeUnit.HOURS).build();

    @Override
    public GroupUnitReportPackage loadGroupUnitReportPackage(Integer groupId, String unitId) {
        if (StringUtils.isEmpty(unitId) || groupId == null || groupId <= 0)
            return defaultRes;
        String key = unitId + Integer.toString(groupId);
        GroupUnitReportPackage unitReportPackages = cacheGroupUnitPackage.getIfPresent(key);
        if (unitReportPackages != null)
            return unitReportPackages;
        unitReportPackages = psrTermReportIPackageService.loadGroupUnitReportPackage(groupId, unitId);
        if (unitReportPackages != null) {
            cacheGroupUnitPackage.put(key, unitReportPackages);
            return unitReportPackages;
        }
        return defaultRes;
    }

    @Override
    public TermReportPackage loadTermtReportPackage(Integer yearId, Integer termId, Integer groupId, String subjectName) {
        if (StringUtils.isEmpty(subjectName) || groupId == null || groupId <= 0 || yearId == null || yearId <= 0 || termId < 0 || termId > 1)
            return defaultResMnonth;
        String key = Integer.toString(yearId) + Integer.toString(termId) + Integer.toString(groupId) + subjectName;
        TermReportPackage unitReportPackages = cacheGroupPackageMonth.getIfPresent(key);
        if (unitReportPackages != null)
            return unitReportPackages;
        unitReportPackages = psrTermReportIPackageService.loadTermtReportPackage(yearId, termId, groupId, subjectName);
        if (unitReportPackages != null) {
            cacheGroupPackageMonth.put(key, unitReportPackages);
            return unitReportPackages;
        }
        return defaultResMnonth;
    }


    //TODO 上线之后去掉
    @Override
    public GroupUnitReportPackage testLoadGroupUnitReportPackage(Integer groupId, String unitId) {
        return psrTermReportIPackageService.testLoadGroupUnitReportPackage(groupId, unitId);
    }

    @Override
    public TermReportPackage testLoadTermtReportPackage(Integer yearId, Integer termId, Integer groupId, String subjectName) {
        return psrTermReportIPackageService.testLoadTermtReportPackage(yearId, termId, groupId, subjectName);
    }
}
