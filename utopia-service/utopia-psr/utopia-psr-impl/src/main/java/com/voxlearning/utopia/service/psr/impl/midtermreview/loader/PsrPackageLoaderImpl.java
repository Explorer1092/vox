package com.voxlearning.utopia.service.psr.impl.midtermreview.loader;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.psr.entity.midtermreview.EnglishPackage;
import com.voxlearning.utopia.service.psr.impl.midtermreview.service.PsrPackageService;
import com.voxlearning.utopia.service.psr.midtermreview.loader.PsrPackageLoader;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2016/10/9.
 */

@Named
@ExposeService(interfaceClass = PsrPackageLoader.class)
public class PsrPackageLoaderImpl implements PsrPackageLoader {
    @Inject private PsrPackageService psrPackageService;

    private static Cache<String, List<EnglishPackage>> cacheEnglishPackage = CacheBuilder.newBuilder().maximumSize(1000).initialCapacity(600).expireAfterAccess(1, TimeUnit.HOURS).build();


    @Override
    public List<EnglishPackage> loadPackage(String bookId, Integer groupId) {
        if (StringUtils.isEmpty(bookId) || groupId == null || groupId <= 0)
            return Collections.emptyList();

        String key = bookId + Integer.toString(groupId);
        List<EnglishPackage> englishPackages = cacheEnglishPackage.getIfPresent(key);

        // 此处 englishPackages 可以为emptyList
        if (englishPackages != null)
            return englishPackages;

        englishPackages = psrPackageService.recomPsrPackageByGroupAndBook(bookId, groupId);
        if (englishPackages != null) {
            cacheEnglishPackage.put(key, englishPackages);
            return englishPackages;
        }
        return Collections.emptyList();
        //return psrPackageService.recomArtificialPsrPackageByGroupAndBook(bookId, groupId);
    }

    @Override
    public List<EnglishPackage> testLoadPackage(String bookId, Integer groupId) {
        return loadPackage(bookId, groupId);
    }
}
