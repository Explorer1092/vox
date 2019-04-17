package com.voxlearning.utopia.agent.service.selfhelp;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.dao.mongo.AgentAppContentPacketDao;
import com.voxlearning.utopia.agent.dao.mongo.selfhelp.AgentSelfHelpDao;
import com.voxlearning.utopia.agent.dao.mongo.selfhelp.AgentSelfHelpTypeDao;
import com.voxlearning.utopia.agent.persist.entity.AgentAppContentPacket;
import com.voxlearning.utopia.agent.persist.entity.selfhelp.AgentSelfHelp;
import com.voxlearning.utopia.agent.persist.entity.selfhelp.AgentSelfHelpType;
import com.voxlearning.utopia.agent.service.AbstractAgentService;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AgentSelfHelpService
 *
 * @author song.wang
 * @date 2018/6/7
 */
@Named
public class AgentSelfHelpService  extends AbstractAgentService {

    @Inject
    private AgentSelfHelpDao agentSelfHelpDao;
    @Inject
    private AgentSelfHelpTypeDao selfHelpTypeDao;

    @Inject
    private AgentAppContentPacketDao agentAppContentPacketDao;
    public void method(){

    }

    public AgentSelfHelp getById(String id) {
        if (id == null ) {
            return null;
        }
        AgentSelfHelp agentSelfHelp = agentSelfHelpDao.load(id);
        if(agentSelfHelp != null && SafeConverter.toBoolean(agentSelfHelp.getDisabled())){
            return null;
        }
        return agentSelfHelp;
    }

    public List<AgentSelfHelp> findByType(String typeId){
        return agentSelfHelpDao.findByType(typeId);
    }
    /**
     * 保存事项列表接口
     * @param id
     * @param typeId
     * @param title
     * @param contact
     * @param email
     * @param wechatGroup
     * @param comment
     * @param packetIds
     * @return
     */
    public MapMessage saveData(String id ,String typeId,String title,String contact,String email,String wechatGroup,String comment,List<String> packetIds){
        AgentSelfHelp selfHelp = getById(id);
        if(selfHelp == null){
            selfHelp = new AgentSelfHelp();
            selfHelp.setDisabled(false);
        }
        selfHelp.setTypeId(typeId);
        selfHelp.setTitle(title);
        selfHelp.setContact(contact);
        selfHelp.setEmail(email);
        selfHelp.setWechatGroup(wechatGroup);
        selfHelp.setComment(comment);
        selfHelp.setContentPacketIds(packetIds);
        agentSelfHelpDao.upsert(selfHelp);
        return MapMessage.successMessage();
    }


    public MapMessage findHelpList(){
        MapMessage result = MapMessage.successMessage();
        List<AgentSelfHelpType> selfHelpTypeList = selfHelpTypeDao.findAllAvailable();
        //对sortId不为空的类型排序
        List<AgentSelfHelpType> selfHelpTypeListFinal = selfHelpTypeList.stream().filter(item -> null != item && null != item.getSortId()).sorted(Comparator.comparing(AgentSelfHelpType::getSortId)).collect(Collectors.toList());
        //拼接上sortId为空的类型
        selfHelpTypeListFinal.addAll(selfHelpTypeList.stream().filter(item -> null != item && null == item.getSortId()).collect(Collectors.toList()));

        //查询出事项类型并按类型名称分组
        Map<String, List<AgentSelfHelp>> selfHelpMap = agentSelfHelpDao.findAllAvailable().stream().filter(a -> a.getTypeId() != null).collect(Collectors.groupingBy( AgentSelfHelp ::getTypeId, Collectors.toList()));

        List<Map<String,Object>> list = new ArrayList<>();
        selfHelpTypeListFinal.forEach(item ->{
            if (null != item && selfHelpMap.containsKey(item.getId())){
                Map<String,Object> m1 = new HashMap<>();
                m1.put("typeName",item.getTypeName());
                m1.put("tlist",selfHelpMap.get(item.getId()));
                list.add(m1);
            }
        });

        result.set("data",list);
        return result;
    }


    public void delSelfHelpItem (String id){
        AgentSelfHelp selfHelp = getById(id);
        if (selfHelp != null ) {
            selfHelp.setDisabled(true);
            agentSelfHelpDao.upsert(selfHelp);
        }
    }
    /**********************************************事项类型接口************************************************/


    public AgentSelfHelpType getSelfHelpTypById(String id) {
        if (id == null ) {
            return null;
        }
        return selfHelpTypeDao.load(id);
    }

    /**
     * 保存事项类型
     * @param id 事项id
     * @param typeName 类型名称
     * @return
     */
    public MapMessage saveItemType(String id , String typeName,Integer sortId){
        AgentSelfHelpType selfHelpType = selfHelpTypeDao.load(id);
        if(selfHelpType == null){
            selfHelpType = new AgentSelfHelpType();
            selfHelpType.setDisabled(false);
        }
        selfHelpType.setTypeName(typeName);
        selfHelpType.setSortId(sortId);
        selfHelpTypeDao.upsert(selfHelpType);
        return MapMessage.successMessage();
    }


    /**
     * 删除事项类型
     * @param typeId
     */
    public void delType (String typeId){
        AgentSelfHelpType selfHelpType = selfHelpTypeDao.load(typeId);
        if (selfHelpType != null ) {
            selfHelpType.setDisabled(true);
            selfHelpTypeDao.upsert(selfHelpType);
        }
    }

    /**
     * 无参查询所有事项类型
     * @return
     */
    public List<AgentSelfHelpType> getSelfHelpTypeList(){
        List<AgentSelfHelpType> selfHelpTypeList = selfHelpTypeDao.findAllAvailable();
        //对sortId不为空的类型排序
        List<AgentSelfHelpType> selfHelpTypeListFinal = selfHelpTypeList.stream().filter(item -> null != item && null != item.getSortId()).sorted(Comparator.comparing(AgentSelfHelpType::getSortId)).collect(Collectors.toList());
        //拼接上sortId为空的类型
        selfHelpTypeListFinal.addAll(selfHelpTypeList.stream().filter(item -> null != item && null == item.getSortId()).collect(Collectors.toList()));
        return selfHelpTypeListFinal;
    }



    public List<Map<String,String>> assemblyPacketInfo(AgentSelfHelp selfHelp){
        List<Map<String,String>> list = new ArrayList<Map<String, String>>();
        if(selfHelp != null && selfHelp.getContentPacketIds() != null && selfHelp.getContentPacketIds().size() >0 ){
            Map<String, AgentAppContentPacket> map = agentAppContentPacketDao.loads(selfHelp.getContentPacketIds());
            map.forEach(((k,v) -> {
                Map<String, String> itemMap = new HashMap<>();
                itemMap.put("datumType", v.getDatumType() != null ? v.getDatumType().getDesc() : "");// 类型
                itemMap.put("contentTitle", v.getContentTitle());
                itemMap.put("contentId",v.getId());
                list.add(itemMap);
            }));
        }
        return list;
    }
}
