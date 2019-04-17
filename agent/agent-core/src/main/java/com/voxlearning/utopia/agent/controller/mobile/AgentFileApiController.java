package com.voxlearning.utopia.agent.controller.mobile;

import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.constants.UploadFileType;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.utils.AgentOssManageUtils;
import com.voxlearning.utopia.agent.utils.FlatVideoOssManageUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * AgentFileApiController
 *
 * @author song.wang
 * @date 2016/6/2
 */
@Slf4j
@Controller
@RequestMapping(value = {"/mobile/file", "file"})
public class AgentFileApiController extends AbstractAgentController{

    public static final String REQ_FILE = "file";
    public static final String REQ_FILE_SIZE = "file_size";
    public static final String REQ_FILE_TYPE = "file_type";

    protected final AtomicLockManager atomicLockManager = AtomicLockManager.instance();

    @RequestMapping(value = "upload.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage upload() {
        MapMessage resultMap = new MapMessage();

        Long userId = getCurrentUserId();
        Long fileSize = getRequestLong(REQ_FILE_SIZE);
        String fileType = getRequestString(REQ_FILE_TYPE);
        String fileUrl = "";
        try {
            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) getRequest();
            MultipartFile file = multipartRequest.getFile(REQ_FILE);
            if (file != null && !file.isEmpty()) {
                fileUrl = atomicLockManager.wrapAtomic(this).keyPrefix("agent_user").keys(userId).proxy().uploadFile(file, fileSize, fileType);
            }
        } catch (Exception ex) {
            log.error("App-Tinaji upload file failed : ", ex);
        }
        if(StringUtils.isNotBlank(fileUrl)){
            resultMap.setSuccess(true);
            resultMap.add("fileUrl", fileUrl);
            resultMap.setInfo("文件上传成功");
        }else{
            resultMap.setSuccess(false);
            resultMap.add("fileUrl", "");
            resultMap.setInfo("文件上传失败，请重新上传");
        }
        return  resultMap;
    }

    private String uploadFile(MultipartFile file, Long fileSize, String fileType) {
        String result = "";
        if (file.getSize() != fileSize) {
            return result;
        }
        return AgentOssManageUtils.upload(file);
    }

    /**
     * 多文件上传
     * @param request
     * @return
     */
    @RequestMapping(value = "multiple_file_upload.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage multipleFileUpload(HttpServletRequest request){
        List<String> imageUrlList = new ArrayList<>();
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
        if(null != fileMap && fileMap.size() > 0){
            Collection<MultipartFile> files = fileMap.values();
            imageUrlList = uploadMultipleFile(files);
            if (CollectionUtils.isEmpty(imageUrlList)){
                MapMessage.errorMessage("文件上传失败");
            }
        }
        return MapMessage.successMessage().add("imageUrlList",imageUrlList);
    }

    private List<String> uploadMultipleFile(Collection<MultipartFile> files) {
        List<String> imageUrlList = new ArrayList<>();
        for(MultipartFile file:files){
            String originalFilename= file.getOriginalFilename();
            if(StringUtils.isNotBlank(originalFilename)){
                String fileUrl = AgentOssManageUtils.upload(file);
                imageUrlList.add(fileUrl);
            }
        }
        return imageUrlList;
    }


    @RequestMapping(value = "getsignature.vpage")
    @ResponseBody
    public MapMessage getSignature() {
        String ext = getRequestString("ext");
        UploadFileType uploadFileType;
        if (ext != null) {
            uploadFileType = UploadFileType.of(ext);
            if (uploadFileType.equals(UploadFileType.unsupported)) {
                return MapMessage.errorMessage("不支持的数据类型");
            }
        } else uploadFileType = UploadFileType.unsupported;

        MapMessage signatureResult = FlatVideoOssManageUtils.getSignature(uploadFileType, "agent", getResponse());
        if (signatureResult != null)
            return MapMessage.successMessage().add("data", signatureResult);
        return MapMessage.errorMessage();
    }

    @RequestMapping(value = "osscallback.vpage", method = RequestMethod.POST)
    @ResponseBody
    public void ossCallback(HttpServletRequest request, HttpServletResponse response) {
        FlatVideoOssManageUtils.handleOssCallback(request, response);
    }


}
