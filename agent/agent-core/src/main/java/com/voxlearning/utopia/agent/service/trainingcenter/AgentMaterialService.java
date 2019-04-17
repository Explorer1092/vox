package com.voxlearning.utopia.agent.service.trainingcenter;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.dao.mongo.trainingcenter.AgentMaterialDao;
import com.voxlearning.utopia.agent.persist.entity.trainingcenter.AgentMaterial;
import com.voxlearning.utopia.agent.utils.FlatVideoOssManageUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.stream.Collectors;

@Named
public class AgentMaterialService{
    @Inject
    private AgentMaterialDao agentMaterialDao;

    public MapMessage saveMaterial(String title, String  url, String  picUrl, Double fileSize, String introduction,Integer videoTime){
        AgentMaterial agentMaterial = new AgentMaterial();
        agentMaterial.setTitle(title);
        agentMaterial.setUrl(url);
        agentMaterial.setPicUrl(picUrl);
        agentMaterial.setFileSize(fileSize);
        agentMaterial.setIntroduction(introduction);
        agentMaterial.setDisabled(false);
        agentMaterial.setVideoTime(videoTime);
        agentMaterialDao.upsert(agentMaterial);
        return MapMessage.successMessage();
    }

    //更新素材标题和备注
    public MapMessage updateMaterial(String id , String title, String introduction,Integer videoTime){
        AgentMaterial agentMaterial = agentMaterialDao.load(id);
        if(agentMaterial == null || (agentMaterial != null && agentMaterial.getDisabled() == true )){
            return MapMessage.errorMessage("当前素材状态为删除，无法更新!");
        }
        agentMaterial.setVideoTime(videoTime);
        agentMaterial.setTitle(title);
        agentMaterial.setIntroduction(introduction);
        agentMaterialDao.upsert(agentMaterial);
        return MapMessage.successMessage();
    }

    public List<AgentMaterial> findMaterialList(){
        return agentMaterialDao.query().stream().filter(p -> p.getDisabled() == false).collect(Collectors.toList());
    }

    //删除素材
    public MapMessage deleteMaterial (String id){
        AgentMaterial agentMaterial = agentMaterialDao.load(id);
        if (agentMaterial != null && Boolean.FALSE.equals(agentMaterial.getDisabled())) {
            agentMaterial.setDisabled(true);
//            AgentOssManageUtils.deleteFile(agentMaterial.getUrl());
            agentMaterialDao.upsert(agentMaterial);
        }
        return MapMessage.successMessage();
    }

//    public void download(String id,String localPath){
//        AgentMaterial agentMaterial = agentMaterialDao.load(id);
//        String url = agentMaterial.getUrl();
//        //TODO  截取bukent之前的域名 做url
//        FlatVideoOssManageUtils.download(url,localPath);
//    }

    public AgentMaterial findById(String id){
        AgentMaterial agentMaterial = agentMaterialDao.load(id);
        return agentMaterial;
    }

}
