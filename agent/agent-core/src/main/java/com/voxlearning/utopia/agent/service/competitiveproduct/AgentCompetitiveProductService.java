package com.voxlearning.utopia.agent.service.competitiveproduct;


import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.agent.dao.mongo.AgentCompetitiveProductDao;
import com.voxlearning.utopia.agent.persist.entity.AgentCompetitiveProduct;
import com.voxlearning.utopia.service.school.client.SchoolExtServiceClient;
import com.voxlearning.utopia.service.user.api.entities.SchoolExtInfo;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;


/**
 * 学校竞品收集service层
 * @author deliang.che
 * @date 2018/3/9
 */
@Named
public class AgentCompetitiveProductService {
    
    @Inject
    private AgentCompetitiveProductDao agentCompetitiveProductDao;
    @Inject
    private SchoolExtServiceClient schoolExtServiceClient;

    /**
     * 学校批量标记为有无竞品
     * @param schoolIds                 学校ID
     * @param competitiveProductFlag    有无竞品标识（1：无竞品 2：有竞品）
     */
    public void updateCompetitiveProductFlag(Set<Long> schoolIds,Integer competitiveProductFlag){
        Map<Long, SchoolExtInfo> extInfoMap = schoolExtServiceClient.getSchoolExtService().loadSchoolsExtInfoAsMap(schoolIds).getUninterruptibly();
        schoolIds.forEach(p -> {
            SchoolExtInfo extInfo = extInfoMap.get(p);
            if(extInfo == null){
                extInfo = new SchoolExtInfo();
                extInfo.setId(p);
                extInfo.setCompetitiveProductFlag(competitiveProductFlag);
                schoolExtServiceClient.getSchoolExtService().upsertSchoolExtInfo(extInfo);
            }else {
                extInfo.setCompetitiveProductFlag(competitiveProductFlag);
                schoolExtServiceClient.getSchoolExtService().updateSchoolExtInfo(extInfo);
            }
        });
    }

    /**
     * 根据学校ID获取竞品列表信息
     * @param schoolId
     * @return
     */
    public List<AgentCompetitiveProduct> loadBySchoolId(Long schoolId){
        List<AgentCompetitiveProduct> competitiveProductList = agentCompetitiveProductDao.loadBySchoolId(schoolId);
        return competitiveProductList;
    }

    /**
     * 根据ID获取竞品信息
     * @param id
     * @return
     */
    public AgentCompetitiveProduct loadById(String id){
        AgentCompetitiveProduct competitiveProduct = agentCompetitiveProductDao.load(id);
        return competitiveProduct;
    }

    /**
     * 插入竞品信息
     * @param agentCompetitiveProduct
     */
    public void insert(AgentCompetitiveProduct agentCompetitiveProduct){
        agentCompetitiveProductDao.insert(agentCompetitiveProduct);
    }

    /**
     * 替换竞品信息
     * @param agentCompetitiveProduct
     */
    public void replace(AgentCompetitiveProduct agentCompetitiveProduct){
        agentCompetitiveProductDao.replace(agentCompetitiveProduct);
    }

    /**
     * 删除竞品信息
     * @param id
     */
    public void removeById(String id){
        //获取学校ID
        AgentCompetitiveProduct competitiveProduct = agentCompetitiveProductDao.load(id);
        Long schoolId = competitiveProduct.getSchoolId();
        //删除竞品信息
        agentCompetitiveProductDao.remove(id);
        //查看该学校是否还有其他竞品
        List<AgentCompetitiveProduct> competitiveProductList = agentCompetitiveProductDao.loadBySchoolId(schoolId);
        //如果没有其他竞品，更新学校竞品标志为“有反馈无竞品”
        if (!CollectionUtils.isNotEmpty(competitiveProductList)){
            SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService().loadSchoolExtInfo(schoolId).getUninterruptibly();
            schoolExtInfo.setCompetitiveProductFlag(1);
            schoolExtServiceClient.getSchoolExtService().updateSchoolExtInfo(schoolExtInfo);
        }
    }
}
