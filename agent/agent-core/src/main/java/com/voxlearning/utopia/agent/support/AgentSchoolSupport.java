package com.voxlearning.utopia.agent.support;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.utopia.agent.service.mobile.resource.AgentResourceService;
import com.voxlearning.utopia.entity.crm.CrmSchoolSummary;
import com.voxlearning.utopia.mapper.SchoolEsInfo;
import com.voxlearning.utopia.mapper.SchoolEsQuery;
import com.voxlearning.utopia.service.school.client.SchoolEsInfoServiceClient;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.consumer.CrmSummaryLoaderClient;
import org.apache.commons.collections4.ListUtils;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * AgentSchoolSupport
 *
 * @author song.wang
 * @date 2018/9/17
 */
@Named
public class AgentSchoolSupport {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    private SchoolEsInfoServiceClient schoolEsInfoServiceClient;
    @Inject
    private CrmSummaryLoaderClient crmSummaryLoaderClient;
    @Inject
    private SchoolLoaderClient schoolLoaderClient;

    // 注：高并发的情况下， es服务调用失败的概率比较高
    public Map<Long, SchoolEsInfo> loadSchoolEsInfo(Collection<Long> schoolIds){
        if(CollectionUtils.isEmpty(schoolIds)){
            return Collections.emptyMap();
        }
        List<SchoolEsInfo> esInfoList = new ArrayList<>();

        List<Future<List<SchoolEsInfo>>> futureList = new ArrayList<>();
        List<List<Long>> splitList = ListUtils.partition(new ArrayList<>(schoolIds), 3000);
        for(List<Long> itemList : splitList){
            futureList.add(AlpsThreadPool.getInstance().submit(() -> loadEsData(itemList)));
        }
        for(Future<List<SchoolEsInfo>> future : futureList){
            try{
                List<SchoolEsInfo> dataList = future.get();
                if(CollectionUtils.isNotEmpty(dataList)){
                    esInfoList.addAll(dataList);
                }
            }catch (Exception e){
                logger.error("学校es查询失败", e);
            }
        }
        return esInfoList.stream().collect(Collectors.toMap(p -> SafeConverter.toLong(p.getId()), Function.identity(), (o1, o2) -> o1));
    }

    private List<SchoolEsInfo> loadEsData(Collection<Long> schoolIds){
        if(CollectionUtils.isEmpty(schoolIds)){
            return Collections.emptyList();
        }
        SchoolEsQuery esQuery = new SchoolEsQuery();
        esQuery.setIds(schoolIds);
        esQuery.setPage(0);
        esQuery.setLimit(schoolIds.size());
        Page<SchoolEsInfo> page = schoolEsInfoServiceClient.getSchoolEsService().loadSchoolEs(esQuery);
        return page.getContent();
    }

    /**
     * 分批查询，优先从SchoolSummary中查询，如果SchoolSummary查不到，则从VoxSchool查询，但结果都封装成CrmSchoolSummary对象
     * 需要注意的是，只填充了部分字段
     * @param schoolIds
     * @return
     */
    public Map<Long, CrmSchoolSummary> batchLoadCrmSchoolSummaryAndSchool(Collection<Long> schoolIds) {
        if (CollectionUtils.isNotEmpty(schoolIds)) {
            List<Long> copySchoolIds = new ArrayList<>(schoolIds);
            Map<Long, CrmSchoolSummary> crmSchoolSummaryMap = batchLoadCrmSchoolSummary(copySchoolIds);
            //移除summary数据存在的
            copySchoolIds.removeAll(crmSchoolSummaryMap.keySet());
            Map<Long, School> schoolMap = batchLoadSchool(copySchoolIds);
            if (MapUtils.isNotEmpty(schoolMap)){
                schoolMap.forEach((k,v) -> crmSchoolSummaryMap.put(k,toCrmSchoolSummary(v)));
            }
            return crmSchoolSummaryMap;
        }
        return new HashMap<>();
    }

    /**
     * 只封装了部分字段
     * @param school
     * @return
     */
    private CrmSchoolSummary toCrmSchoolSummary(School school){
        if (null != school){
            CrmSchoolSummary crmSchoolSummary = new CrmSchoolSummary();
            crmSchoolSummary.setSchoolId(school.getId());
            crmSchoolSummary.setSchoolName(school.getCname());
            if (null != school.getLevel()){
                crmSchoolSummary.setSchoolLevel(SchoolLevel.safeParse(school.getLevel()));
            }
            crmSchoolSummary.setCountyCode(school.getRegionCode());
            crmSchoolSummary.setDisabled(school.getDisabled());
            return crmSchoolSummary;
        }
        return null;
    }

    /**
     * 分批查询，如果一次查询会把缓存查死
     * @param schoolIds
     * @return
     */
    public Map<Long, CrmSchoolSummary> batchLoadCrmSchoolSummary(Collection<Long> schoolIds) {
        Map<Long, CrmSchoolSummary> schoolMapTemp = new HashMap<>();
        if (CollectionUtils.isNotEmpty(schoolIds)){
            AgentResourceService.batchIds(schoolIds,2000).forEach((k, v) -> {
                if (CollectionUtils.isNotEmpty(v)){
                    Map<Long, CrmSchoolSummary> longCrmSchoolSummaryMap = crmSummaryLoaderClient.loadSchoolSummary(v);
                    schoolMapTemp.putAll(longCrmSchoolSummaryMap);
                }
            });
        }
        return schoolMapTemp;
    }

    public Map<Long, School> batchLoadSchool(Collection<Long> schoolIds){
        Map<Long, School> schoolMapTemp = new HashMap<>();
        if (CollectionUtils.isNotEmpty(schoolIds)){
            AgentResourceService.batchIds(schoolIds,2000).forEach((k,v) -> {
                if (CollectionUtils.isNotEmpty(v)){
                    Map<Long, School> schoolMap = schoolLoaderClient.getSchoolLoader()
                            .loadSchools(v)
                            .getUninterruptibly();
                    schoolMapTemp.putAll(schoolMap);
                }
            });
        }
        return schoolMapTemp;
    }

}
