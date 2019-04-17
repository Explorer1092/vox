package com.voxlearning.utopia.agent.service.trainingcenter;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.dao.mongo.trainingcenter.AgentArticleDao;
import com.voxlearning.utopia.agent.dao.mongo.trainingcenter.AgentTitleColumnDao;
import com.voxlearning.utopia.agent.persist.entity.trainingcenter.AgentTitleColumn;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

@Named
public class AgentTitleColumnService {
    @Inject
    private AgentTitleColumnDao agentTitleColumnDao;

    @Inject
    private AgentArticleDao agentArticleDao;

    public MapMessage findColumnList(Set<Integer> levels){
        MapMessage mapMessage = MapMessage.successMessage();
        List<Object> resultList = new ArrayList<>();
        List<AgentTitleColumn> list = agentTitleColumnDao.query();
        List<Map<String,Object>> firstLevelList = findFirstColumnList(list,1);
        List<Map<String,Object>> secondLevelList = findFirstColumnList(list,2);
        if(levels.contains(1)){
            if(CollectionUtils.isNotEmpty(firstLevelList)){
                resultList.addAll(firstLevelList);
            }
        }
        if(levels.contains(2)){
            if(CollectionUtils.isNotEmpty(secondLevelList)){
                resultList.addAll(secondLevelList);
            }
        }
        mapMessage.put("data",resultList);
        return mapMessage;
    }

    //过滤栏目级别并排序
    public List<Map<String,Object>> findFirstColumnList(List<AgentTitleColumn> list,int level){
        List<Map<String,Object>> resultList = new ArrayList<>();
        List<AgentTitleColumn> columns = list.stream().filter(p -> p.getDisabled()==false && p.getLevel() == level ).sorted(Comparator.comparing(AgentTitleColumn :: getSortId )).collect(Collectors.toList());
        columns.forEach(p->{
            Map<String,Object> map = new HashMap<>();
            map.put("id",p.getId());
            map.put("name",p.getName());
            map.put("level",p.getLevel());
            map.put("parentId",p.getParentId());
            map.put("sortId",p.getSortId());
            map.put("disabled",p.getDisabled());
            AgentTitleColumn agentTitleColumn = agentTitleColumnDao.load(p.getParentId());
            map.put("parentName",agentTitleColumn == null ? "" : agentTitleColumn.getName());
            resultList.add(map);
        });
        return resultList;
    }
    //保存栏目信息
    public MapMessage saveColumn(String id ,String name,Integer level,String parentId,Integer sortId){
        List<AgentTitleColumn> allColumn = agentTitleColumnDao.findAll();
        boolean flag = false;
        for(AgentTitleColumn column : allColumn){
            if(StringUtils.isBlank(id)){
                if(column.getName().equals(name)){
                    flag = true;
                    break;
                }
            }else{
                if(column.getName().equals(name) && !column.getId().equals(id)){
                    flag = true;
                    break;
                }
            }
        }
        if(flag){
           return MapMessage.errorMessage("栏目名称不能重复");
        }
        AgentTitleColumn agentTitleColumn = agentTitleColumnDao.findById(id);
        if(agentTitleColumn == null){
            agentTitleColumn = new AgentTitleColumn();
            agentTitleColumn.setDisabled(false);
        }
        agentTitleColumn.setName(name);
        agentTitleColumn.setLevel(level);
        agentTitleColumn.setParentId(parentId);
        agentTitleColumn.setSortId(sortId);
        agentTitleColumnDao.upsert(agentTitleColumn);
        return MapMessage.successMessage();
    }

    //删除栏目
    public MapMessage delColumn (String id){
        AgentTitleColumn agentTitleColumn = agentTitleColumnDao.findById(id);
        if (agentTitleColumn != null ) {
            if(agentTitleColumn.getLevel() == 1){
                //获取二级栏目列表
                List<AgentTitleColumn> secondList = agentTitleColumnDao.findByParentId(agentTitleColumn.getId());
                if(CollectionUtils.isNotEmpty(secondList)){
                    boolean delFlag = true;
                    for(AgentTitleColumn column : secondList){
                        if(CollectionUtils.isNotEmpty(agentArticleDao.loadArticleByTwoLevelColumnId(column.getId()))){
                            delFlag = false;
                            break;
                        }
                    }
                    //所有的二级下都没有文章  删除一级时把所有二级删除
                    if(!delFlag){
                        return MapMessage.errorMessage("包含的二级栏目下有未删除的文章，无法删除");
                    }
                    //所有二级没有文章  先删二级
                    secondList.forEach( p->{
                        p.setDisabled(true);
                        agentTitleColumnDao.upsert(p);
                    });
                }

            }else{
                if(CollectionUtils.isNotEmpty(agentArticleDao.loadArticleByTwoLevelColumnId(id))){
                    return MapMessage.errorMessage("栏目下有未删除的文章，无法删除");
                }
            }
            agentTitleColumn.setDisabled(true);
            agentTitleColumnDao.upsert(agentTitleColumn);
        }
        return MapMessage.successMessage();
    }

    /**
     * 一级二级栏目联动

     */
    public MapMessage findLinkageColumnList(){
        MapMessage mapMessage = MapMessage.successMessage();
        List<Map<String,Object>> result = new ArrayList<>();
        List<AgentTitleColumn> list = agentTitleColumnDao.query();
        List<Map<String,Object>> firstLevelList = findFirstColumnList(list,1);
        List<Map<String,Object>> secondLevelList = findFirstColumnList(list,2);
        Map<String,List<Map<String,Object>>> secondLevelListMap = secondLevelList.stream().collect(Collectors.groupingBy(e -> e.get("parentId").toString()));
        firstLevelList.forEach(p ->{
            Map<String,Object> map  = new HashMap<>();
            map.put("first",p);
            map.put("second",secondLevelListMap.get(p.get("id")));
            result.add(map);
        });
        mapMessage.put("data",result);
        return mapMessage;
    }

    public AgentTitleColumn findColumnById(String id){
        return agentTitleColumnDao.findById(id);
    }
}
