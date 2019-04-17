package com.voxlearning.utopia.service.piclisten.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.utopia.service.vendor.api.entity.TextBookManagement;
import com.voxlearning.utopia.service.vendor.api.mapper.TextBookMapper;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by jiang wei on 2017/4/5.
 */
@ServiceVersion(version = "2018.01.08")
@ServiceTimeout(timeout = 1, unit = TimeUnit.MINUTES)
@ServiceRetries
public interface CRMTextBookManagementService {


    TextBookManagement $loadByBookId(String bookId);

    List<TextBookManagement> $loadTextBooks();

    TextBookManagement $upsertTextBook(TextBookManagement textBookManager);

    List<TextBookMapper> $getPublisherBookList();


    Boolean removeBook(String bookId);

    Boolean removeBookIgnoreEnv(String bookId);


    /**
     * 仅供 staging job 使用
     * @return
     */
    @Deprecated
    List<TextBookManagement> $loadAllIgnoreEnv();

    /**
     * 仅供 staging job 使用
     * @return
     */
    @Deprecated
    void initSdkInfo();
}
