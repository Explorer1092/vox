package com.voxlearning.utopia.service.newexam.api.client;

import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.newexam.api.entity.NewExamProcessResult;

import java.util.Collection;
import java.util.Map;

/**
 * Created by tanguohong on 2016/3/7.
 */
public interface INewExamProcessResultLoaderClient extends IPingable {

    NewExamProcessResult loadById(String id);

    Map<String,NewExamProcessResult> loadByIds(Collection<String> ids);
}
