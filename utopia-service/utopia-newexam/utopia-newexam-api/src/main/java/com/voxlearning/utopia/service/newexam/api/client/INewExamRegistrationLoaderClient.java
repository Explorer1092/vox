package com.voxlearning.utopia.service.newexam.api.client;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.newexam.api.entity.NewExamRegistration;
import com.voxlearning.utopia.service.newexam.api.mapper.NewExamForExport;
import com.voxlearning.utopia.service.newexam.api.mapper.NewExamRegistrationLoaderMapper;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by tanguohong on 2016/3/7.
 */
public interface INewExamRegistrationLoaderClient extends IPingable {

    NewExamRegistration loadById(String id);

    Map<String, NewExamRegistration> loadByIds(Collection<String> ids);

    MapMessage loadByNewExamIdAndPage(NewExamRegistrationLoaderMapper newExamRegistrationLoaderMapper);

    List<NewExamForExport> loadByNewExam(String newExamId);
}
