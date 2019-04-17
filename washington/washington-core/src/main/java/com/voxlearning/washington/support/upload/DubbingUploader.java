package com.voxlearning.washington.support.upload;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.time.FastDateFormat;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.storage.StorageClient;
import com.voxlearning.alps.spi.storage.StorageClientLocation;
import com.voxlearning.alps.spi.storage.StorageMetadata;
import com.voxlearning.alps.spi.storage.StorageSystem;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.api.constant.SupportedFileType;
import com.voxlearning.utopia.service.newhomework.consumer.NewHomeworkServiceClient;
import com.voxlearning.utopia.service.question.api.entity.Dubbing;
import com.voxlearning.utopia.service.question.consumer.DubbingLoaderClient;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Arrays;
import java.util.Date;

@Named
public class DubbingUploader extends SpringContainerSupport {

    @StorageClientLocation(storage = "homework")
    private StorageClient storageClient;

    @StorageClientLocation(system = StorageSystem.GFS, storage = "fs-dubbing")
    private StorageClient gfsStorageClient;

    @Inject private NewHomeworkServiceClient newHomeworkServiceClient;
    @Inject private DubbingLoaderClient dubbingLoaderClient;

    public MapMessage uploadDubbing(Long userId, MultipartFile inputFile, String homeworkId, String dubbingId) {
        Dubbing dubbing = dubbingLoaderClient.loadDubbingByIdIncludeDisabled(dubbingId);
        if (dubbing == null) {
            return MapMessage.errorMessage("dubbing not exist").setErrorCode(ErrorCodeConstants.ERROR_CODE_PARAMETER);
        }
        String ext = StringUtils.substringAfterLast(inputFile.getOriginalFilename(), ".");
        ext = StringUtils.defaultString(ext).trim().toLowerCase();
        SupportedFileType fileType;
        try {
            fileType = SupportedFileType.valueOf(ext);
        } catch (Exception ex) {
            logger.warn("Unsupported dubbing file type: {}", ext);
            throw new RuntimeException("不支持此格式文件");
        }

        try {
            StorageMetadata storageMetadata = new StorageMetadata();
            storageMetadata.setContentLength(inputFile.getSize());
            String env = "fs-dubbing/prod/";
            if (RuntimeMode.isDevelopment() || RuntimeMode.isTest()) {
                env = "fs-dubbing/test/";
            }
            String path = env + FastDateFormat.getInstance("yyyy/MM/dd").format(new Date());
            String id = StringUtils.join(Arrays.asList(homeworkId, userId, dubbingId), "__");
            String audioName = id + "." + fileType.name();
            String audioUrl = storageClient.upload(inputFile.getInputStream(), audioName, path, storageMetadata);
//            String audioUrl = null;
            //aliyun上传失败回原gfs
            if(audioUrl == null){
                storageMetadata = new StorageMetadata();
                storageMetadata.setContentType(fileType.getContentType());
                //gfs的id必须是不重复的，因为上传gfs只是插入不会upsert
                String gfsId = RandomUtils.nextObjectId();
                gfsStorageClient.uploadWithId(inputFile.getInputStream(), gfsId, audioName, null, storageMetadata);
                //gfs的audioUrl不带yyyy/MM/dd路径，因为回原配置不好处理。
                audioUrl = env + audioName;
            }
            //记录上传成功的数据
            return newHomeworkServiceClient.uploaderDubbing(id, audioUrl, dubbing.getVideoUrl(), path);
        } catch (Exception ex) {
            logger.warn("Failed uploading dubbing file: {}", ex.getMessage());
            throw new RuntimeException("上传文件失败");
        }
    }

}
