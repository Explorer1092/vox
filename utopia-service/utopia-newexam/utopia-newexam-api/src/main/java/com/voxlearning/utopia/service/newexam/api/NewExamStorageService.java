package com.voxlearning.utopia.service.newexam.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.utopia.service.business.api.entity.GridFileTag;

import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "2018.04.24")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface NewExamStorageService {

    boolean existByBookIdCountyCodeGridFileTypePaperTypeYear(GridFileTag gridFileTag);

}
