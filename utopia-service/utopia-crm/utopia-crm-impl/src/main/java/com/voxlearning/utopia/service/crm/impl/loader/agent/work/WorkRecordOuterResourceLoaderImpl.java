package com.voxlearning.utopia.service.crm.impl.loader.agent.work;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.crm.api.entities.agent.signin.SignInRecord;
import com.voxlearning.utopia.service.crm.api.entities.agent.work.WorkRecordOuterResource;
import com.voxlearning.utopia.service.crm.api.entities.agent.work.WorkRecordResourceExtension;
import com.voxlearning.utopia.service.crm.api.loader.agent.work.WorkRecordOuterResourceLoader;
import com.voxlearning.utopia.service.crm.impl.dao.agent.signin.SignInRecordDao;
import com.voxlearning.utopia.service.crm.impl.dao.agent.work.WorkRecordOuterResourceDao;
import com.voxlearning.utopia.service.crm.impl.dao.agent.work.WorkRecordResourceExtensionDao;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * WorkRecordOuterResourceLoaderImpl
 *
 * @author deliang.che
 * @since  2018/12/17
 */
@Named
@Service(interfaceClass = WorkRecordOuterResourceLoader.class)
@ExposeService(interfaceClass = WorkRecordOuterResourceLoader.class)
public class WorkRecordOuterResourceLoaderImpl extends SpringContainerSupport implements WorkRecordOuterResourceLoader {

    @Inject
    WorkRecordOuterResourceDao workRecordOuterResourceDao;
    @Inject
    WorkRecordResourceExtensionDao workRecordResourceExtensionDao;
    @Inject
    SignInRecordDao signInRecordDao;

    @Override
    public WorkRecordOuterResource load(String id){
        return workRecordOuterResourceDao.load(id);
    }

    @Override
    public Map<String,WorkRecordOuterResource> loads(Collection<String> ids){
        return workRecordOuterResourceDao.loads(ids);
    }

    @Override
    public List<Map<String, Object>> resourceVisitList(Long resourceId) {
        List<WorkRecordOuterResource> recordList = workRecordOuterResourceDao.findByResourceId(resourceId);
        if(CollectionUtils.isEmpty(recordList)){
            return Collections.emptyList();
        }
        List<String> mainRecordIds = recordList.stream().map(WorkRecordOuterResource::getWorkRecordId).collect(Collectors.toList());
        Map<String, WorkRecordResourceExtension> extensionMap = workRecordResourceExtensionDao.loads(mainRecordIds);

        List<String> signIds =  extensionMap.values().stream().map(WorkRecordResourceExtension :: getSignInRecordId).collect(Collectors.toList());
        Map<String, SignInRecord> signInRecordMap = signInRecordDao.loads(signIds);
        List<Map<String, Object>> result = new ArrayList<>();
        recordList.forEach(p -> {
            Map<String, Object> data = new HashMap<>();
            data.put("recordId",p.getWorkRecordId());
            WorkRecordResourceExtension extension = extensionMap.get(p.getWorkRecordId());
            if(extension == null) {
                return;
            }
            SignInRecord signInRecord = signInRecordMap.get(extension.getSignInRecordId());

            data.put("intention", extension == null ? "" : extension.getVisitIntention());
            data.put("flow", extension == null ? "" : extension.getContent());
            data.put("place", signInRecord == null ? "" : null != signInRecord.getAddress() ? signInRecord.getAddress() : "");
            data.put("conclusion", p.getResult());
            data.put("workTime", p.getWorkTime());
            data.put("visitors", p.getUserName());
            data.put("imageList", extension == null ? "" : extension.getPhotoUrls());
            //TODO 其他事项 暂时没有  做新资源拜访管理时加上
            data.put("otherMatters","");
            result.add(data);
        });
        return result;
    }

}
