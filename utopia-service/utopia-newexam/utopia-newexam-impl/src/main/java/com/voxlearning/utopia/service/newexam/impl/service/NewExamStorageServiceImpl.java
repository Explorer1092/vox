package com.voxlearning.utopia.service.newexam.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.api.constant.GridFileStatus;
import com.voxlearning.utopia.service.business.api.entity.GridFileInfo;
import com.voxlearning.utopia.service.business.api.entity.GridFileTag;
import com.voxlearning.utopia.service.newexam.api.NewExamStorageService;
import com.voxlearning.utopia.storage.api.client.StorageLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@Named("com.voxlearning.utopia.service.newexam.impl.service.NewExamStorageServiceImpl")
@ExposeService(interfaceClass = NewExamStorageService.class)
public class NewExamStorageServiceImpl extends SpringContainerSupport implements NewExamStorageService {

    @Inject private StorageLoaderClient storageLoaderClient;

    @Override
    public boolean existByBookIdCountyCodeGridFileTypePaperTypeYear(GridFileTag gridFileTag) {
        //已无效的资源需要过滤掉
        List<GridFileTag> gridFileTags = storageLoaderClient.getStorageLoader()
                .findGridFileTags(gridFileTag.getBookId(), gridFileTag.getCountyCode(), gridFileTag.getFileType(), gridFileTag.getPaperType(), gridFileTag.getYear())
                .getUninterruptibly();
        for (GridFileTag ft : gridFileTags) {
            GridFileInfo gridFileInfo = storageLoaderClient.getStorageLoader()
                    .loadGridFileInfoByGfsId(ft.getGfsId())
                    .getUninterruptibly();
            if (null != gridFileInfo && gridFileInfo.getFileStatus() != GridFileStatus.无效资源) {
                return true;
            }
        }
        return false;
    }
}
