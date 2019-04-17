package com.voxlearning.utopia.service.zone.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.utopia.service.zone.api.ZoneCacheService;

import javax.inject.Inject;
import javax.inject.Named;

@Named
@ExposeService(interfaceClass = ZoneCacheService.class)
public class ZoneCacheServiceImpl implements ZoneCacheService {

    @Inject private ZonePhotoServiceImpl zonePhotoService;

    @Override
    public AlpsFuture<Boolean> alreadyUploaded(Long studentId, Long clazzId) {
        return zonePhotoService.alreadyUploaded(studentId, clazzId);
    }

    @Override
    public void photoUploaded(Long studentId, Long clazzId) {
        zonePhotoService.photoUploaded(studentId, clazzId);
    }
}
