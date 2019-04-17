package com.voxlearning.utopia.agent.controller.trainingcenter;


import com.aliyun.oss.model.OSSObject;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.persist.entity.trainingcenter.AgentMaterial;
import com.voxlearning.utopia.agent.service.trainingcenter.AgentMaterialService;
import com.voxlearning.utopia.agent.utils.FlatVideoOssManageUtils;
import lombok.Cleanup;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;


/**
 * 培训中心-素材管理
 * @author deliang.che
 * @since 2018/7/6
 */
@Controller
@RequestMapping(value = "/trainingcenter/material")
public class MaterialController extends AbstractAgentController {

    @Inject
    private AgentMaterialService agentMaterialService;
    //跳页面
    @RequestMapping("materialList.vpage")
    public String materialList(Model model){
        return "/trainingcenter/material/materialList";
    }


    // 保存功能
    @RequestMapping(value = "saveMaterialData.vpage",method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveMaterialData() {

        String id = getRequestString("id");
        String title = getRequestString("title");//标题
        String url = getRequestString("url");//视频url
        String picUrl = getRequestString("picUrl");//预览图url
        Double fileSize = getRequestDouble("fileSize");//视频大小
        String introduction = getRequestString("introduction");     // 简介
        Integer videoTime = getRequestInt("videoTime");
        if(StringUtils.isBlank(title)){
            return MapMessage.errorMessage("请填写标题！");
        }
        if(StringUtils.isBlank(introduction)){
            return MapMessage.errorMessage("请输入视频简介！");
        }
        if(videoTime <= 0 ){
            videoTime = 1;
        }
        if(StringUtils.isBlank(id)){
            if(StringUtils.isBlank(url)){
                return MapMessage.errorMessage("请先上传视频！");
            }
            if(fileSize <= 0 ){
                return MapMessage.errorMessage("视频大小不能为空！");
            }
        }
        if(StringUtils.isBlank(id)){
            agentMaterialService.saveMaterial(title,url,picUrl,fileSize,introduction,videoTime);
        }else {
            agentMaterialService.updateMaterial(id,title,introduction,videoTime);
        }
        return MapMessage.successMessage();
    }
    /**
     * 查询列表
     * @return
     */
    @RequestMapping(value = "findMaterialList.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage findMaterialList(){
        MapMessage mapMessage = MapMessage.successMessage();
        List<AgentMaterial> list = agentMaterialService.findMaterialList();
        mapMessage.put("data",list);
        return mapMessage;
    }
    /**
     * 删除素材
     * @return
     */
    @RequestMapping(value = "deleteMaterial.vpage",method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteMaterial(){
        String id = getRequestString("id");
        return agentMaterialService.deleteMaterial(id);
    }

    /**
     * 下载接口
     */
    @RequestMapping(value = "downloadVideo.vpage",method = RequestMethod.GET)
    @ResponseBody
    public void downloadVideo(HttpServletResponse response){
        try{
            String id = getRequestString("id");
            String localPath = getRequestString("localPath");
            AgentMaterial agentMaterial = agentMaterialService.findById(id);
            try {
                OSSObject ossObject = FlatVideoOssManageUtils.download(agentMaterial.getUrl(),localPath);
                InputStream inStream = ossObject.getObjectContent();

                @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();

                int byteRead;
                byte[] buffer = new byte[1024];
                while ((byteRead = inStream.read(buffer)) != -1) {
                    outStream.write(buffer, 0, byteRead);
                }
                outStream.flush();
                HttpRequestContextUtils.currentRequestContext().downloadFile(
                        agentMaterial.getUrl().substring(agentMaterial.getUrl().lastIndexOf("/")),
                        "video/mpeg4",
                        outStream.toByteArray());
            } catch (IOException ignored) {
                ignored.printStackTrace();
                response.getWriter().write("下载失败");
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            }
        }catch (Exception e){
            logger.error("下载失败!", e.getMessage(), e);
        }


    }

}