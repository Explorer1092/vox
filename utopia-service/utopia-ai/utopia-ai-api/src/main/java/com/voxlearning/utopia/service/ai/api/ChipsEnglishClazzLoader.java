package com.voxlearning.utopia.service.ai.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.ai.entity.AiChipsEnglishTeacher;

import java.util.List;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20190329")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 3)
public interface ChipsEnglishClazzLoader extends IPingable {


    List<AiChipsEnglishTeacher> loadChipsEnglishTeacherByName(String name);

}
