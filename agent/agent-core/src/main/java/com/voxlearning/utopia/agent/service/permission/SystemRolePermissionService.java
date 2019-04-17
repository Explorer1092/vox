package com.voxlearning.utopia.agent.service.permission;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.persist.*;
import com.voxlearning.utopia.agent.persist.entity.permission.*;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.view.permission.ModuleAndOperationView;
import com.voxlearning.utopia.agent.bean.permission.SystemModuleAndOperationHolder;
import com.voxlearning.utopia.agent.view.permission.SystemOperationView;
import com.voxlearning.utopia.agent.view.permission.SystemPageElementView;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 *
 * @author song.wang
 * @date 2018/5/9
 */
@Named
public class SystemRolePermissionService {

    @Inject private SystemPageElementPersistence systemPageElementPersistence;
    @Inject private SystemRolePageElementPersistence systemRolePageElementPersistence;

    @Inject private SystemOperationPersistence systemOperationPersistence;
    @Inject private SystemRoleOperationPersistence systemRoleOperationPersistence;

    @Inject private BaseOrgService baseOrgService;


    public MapMessage editPageElement(Long elementId, String module, String subModule, String pageName, String elementCode, String elementName, String comment){
        if(StringUtils.isBlank(module) || StringUtils.isBlank(subModule) || StringUtils.isBlank(pageName) || StringUtils.isBlank(elementName)){
            return MapMessage.errorMessage("模块，子模块，页面名称和元素名称不能为空！");
        }
        SystemPageElement pageElement;
        if(elementId != null && elementId > 0){
            pageElement = systemPageElementPersistence.load(elementId);
            if(pageElement == null){
                return MapMessage.errorMessage("该页面元素不存在！");
            }
            if(!Objects.equals(pageElement.getModule(), module)
                    || !Objects.equals(pageElement.getSubModule(), subModule)
                    || !Objects.equals(pageElement.getPageName(), pageName)
                    || !Objects.equals(pageElement.getElementName(), elementName)
                    || !Objects.equals(pageElement.getComment(), comment)){
                pageElement.setModule(module);
                pageElement.setSubModule(subModule);
                pageElement.setPageName(pageName);
                pageElement.setElementName(elementName);
                pageElement.setComment(comment);
                systemPageElementPersistence.replace(pageElement);
            }
        }else {
            if(StringUtils.isBlank(elementCode)){
                return MapMessage.errorMessage("元素编码不能为空！");
            }
            pageElement = new SystemPageElement();
            pageElement.setModule(module);
            pageElement.setSubModule(subModule);
            pageElement.setPageName(pageName);
            pageElement.setElementCode(elementCode);
            pageElement.setElementName(elementName);
            pageElement.setComment(comment);
            pageElement.setDisabled(false);
            systemPageElementPersistence.insert(pageElement);
        }

        return MapMessage.successMessage().add("elementId", pageElement.getId());
    }

    public MapMessage deletePageElement(Long elementId){

        List<SystemRolePageElement> rolePageElementList = systemRolePageElementPersistence.findByPageElementId(elementId);
        if(CollectionUtils.isNotEmpty(rolePageElementList)){
            rolePageElementList.forEach(p -> {
                p.setDisabled(true);
                systemRolePageElementPersistence.replace(p);
            });
        }
        SystemPageElement pageElement = systemPageElementPersistence.load(elementId);
        if(pageElement != null){
            pageElement.setDisabled(true);
            systemPageElementPersistence.replace(pageElement);
        }
        return MapMessage.successMessage();
    }

    public List<SystemPageElementView> getPageElementsForRole(Integer roleId){

        List<SystemPageElement> allElementList = systemPageElementPersistence.findAll();
        if(CollectionUtils.isEmpty(allElementList)){
            return Collections.emptyList();
        }
        List<SystemPageElementView> pageElementList = allElementList.stream().map(SystemPageElement::toViewData).collect(Collectors.toList());
        List<SystemRolePageElement> rolePageElementList = systemRolePageElementPersistence.findByRoleId(roleId);
        if(CollectionUtils.isNotEmpty(rolePageElementList)){
            Set<Long> rolePageElementIds = rolePageElementList.stream().map(SystemRolePageElement::getPageElementId).collect(Collectors.toSet());
            pageElementList.forEach(p -> {
                if(rolePageElementIds.contains(p.getId())){
                    p.setSelected(true);
                }
            });
        }
        return pageElementList;
    }

    public MapMessage setPageElementsForRole(Integer roleId, Collection<Long> pageElementIds){
        Set<Long> targetPageElementIds = new HashSet<>();
        if(CollectionUtils.isNotEmpty(pageElementIds)){
            targetPageElementIds.addAll(pageElementIds.stream().filter(Objects::nonNull).collect(Collectors.toSet()));
        }
        List<SystemRolePageElement> rolePageElementList = systemRolePageElementPersistence.findByRoleId(roleId);
        Set<Long> dbIds = rolePageElementList.stream().map(SystemRolePageElement::getPageElementId).collect(Collectors.toSet());

        rolePageElementList.forEach(p -> {
            if(!targetPageElementIds.contains(p.getPageElementId())){
                p.setDisabled(true);
                systemRolePageElementPersistence.replace(p);
            }
        });

        Set<Long> insertIds = targetPageElementIds.stream().filter(p -> !dbIds.contains(p)).collect(Collectors.toSet());
        if(CollectionUtils.isNotEmpty(insertIds)){
            List<SystemRolePageElement> insertList = insertIds.stream().map(p -> {
                SystemRolePageElement rolePageElement = new SystemRolePageElement();
                rolePageElement.setRoleId(roleId);
                rolePageElement.setPageElementId(p);
                rolePageElement.setDisabled(false);
                return rolePageElement;
            }).collect(Collectors.toList());
            systemRolePageElementPersistence.inserts(insertList);
        }
        return MapMessage.successMessage();
    }

    public List<SystemPageElementView> getPageElementsWithRoles(){
        List<SystemPageElement> allElementList = systemPageElementPersistence.findAll();
        if(CollectionUtils.isEmpty(allElementList)){
            return Collections.emptyList();
        }
        List<SystemPageElementView> pageElementList = allElementList.stream().map(SystemPageElement::toViewData).collect(Collectors.toList());
        List<SystemRolePageElement> allRolePageElements = systemRolePageElementPersistence.findAll();
        Map<Long, List<Integer>> elementRolesMap = allRolePageElements.stream().collect(Collectors.groupingBy(SystemRolePageElement::getPageElementId, Collectors.mapping(SystemRolePageElement::getRoleId, Collectors.toList())));

        pageElementList.forEach(p -> {
            if(elementRolesMap.containsKey(p.getId())){
                p.setRoleTypeList(elementRolesMap.get(p.getId()).stream().map(AgentRoleType::of).filter(Objects::nonNull).collect(Collectors.toList()));
            }
        });
        return pageElementList;
    }

    public SystemPageElementView getPageElementByElementId(Long elementId){
        SystemPageElement pageElement = systemPageElementPersistence.load(elementId);
        if(pageElement == null){
            return null;
        }
        SystemPageElementView view = pageElement.toViewData();
        List<SystemRolePageElement> roleElementList = systemRolePageElementPersistence.findByPageElementId(elementId);
        if(CollectionUtils.isNotEmpty(roleElementList)){
            view.setRoleTypeList(roleElementList.stream().map(p -> AgentRoleType.of(p.getRoleId())).filter(Objects::nonNull).collect(Collectors.toList()));
        }
        return view;
    }

    public MapMessage setPageElementForRoles(Long elementId, Collection<AgentRoleType> roleTypes){
        Set<Integer> roleIds = new HashSet<>();
        if(CollectionUtils.isNotEmpty(roleTypes)){
            roleIds.addAll(roleTypes.stream().map(AgentRoleType::getId).collect(Collectors.toList()));
        }
        List<SystemRolePageElement> roleElementList = systemRolePageElementPersistence.findByPageElementId(elementId);
        Set<Integer> dbRoleList = roleElementList.stream().map(SystemRolePageElement::getRoleId).collect(Collectors.toSet());

        roleElementList.forEach(p -> {
            if(!roleIds.contains(p.getRoleId())){
                p.setDisabled(true);
                systemRolePageElementPersistence.replace(p);
            }
        });

        Set<Integer> insertIds = roleIds.stream().filter(p -> !dbRoleList.contains(p)).collect(Collectors.toSet());
        if(CollectionUtils.isNotEmpty(insertIds)){
            List<SystemRolePageElement> insertList = insertIds.stream().map(p -> {
                SystemRolePageElement rolePageElement = new SystemRolePageElement();
                rolePageElement.setRoleId(p);
                rolePageElement.setPageElementId(elementId);
                rolePageElement.setDisabled(false);
                return rolePageElement;
            }).collect(Collectors.toList());

            systemRolePageElementPersistence.inserts(insertList);
        }
        return MapMessage.successMessage();
    }


    public List<String> loadUserPageElementCodes(Long userId){
        List<AgentRoleType> roleList = baseOrgService.getUserRoleList(userId);

        Set<Long> elementIds = new HashSet<>();
        roleList.forEach(p -> {
            List<SystemRolePageElement> rolePageElementList = systemRolePageElementPersistence.findByRoleId(p.getId());
            if(CollectionUtils.isNotEmpty(rolePageElementList)){
                elementIds.addAll(rolePageElementList.stream().map(SystemRolePageElement::getPageElementId).collect(Collectors.toSet()));
            }
        });
        Set<String> elementCodes = new HashSet<>();
        if(CollectionUtils.isNotEmpty(elementIds)){
            Map<Long, SystemPageElement> elementMap = systemPageElementPersistence.loads(elementIds);
            if(MapUtils.isNotEmpty(elementMap)){
                elementCodes.addAll(elementMap.values().stream().map(SystemPageElement::getElementCode).filter(StringUtils::isNotBlank).collect(Collectors.toSet()));
            }
        }
        return new ArrayList<>(elementCodes);
    }


    // 操作权限配置

    public List<SystemOperationView> getOperationsForRole(Integer roleId){

        List<SystemOperation> allOperationList = systemOperationPersistence.findAll();
        if(CollectionUtils.isEmpty(allOperationList)){
            return Collections.emptyList();
        }
        List<SystemOperationView> operationList = allOperationList.stream().map(SystemOperation::toViewData).collect(Collectors.toList());
        List<SystemRoleOperation> roleOperationList = systemRoleOperationPersistence.findByRoleId(roleId);
        if(CollectionUtils.isNotEmpty(roleOperationList)){
            Set<Long> roleOperationIds = roleOperationList.stream().map(SystemRoleOperation::getOperationId).collect(Collectors.toSet());
            operationList.forEach(p -> {
                if(roleOperationIds.contains(p.getId())){
                    p.setSelected(true);
                }
            });
        }
        return operationList;
    }

    public MapMessage setOperationsForRole(Integer roleId, Collection<Long> operationIds){
        Set<Long> targetOperationIds = new HashSet<>();
        if(CollectionUtils.isNotEmpty(operationIds)){
            targetOperationIds.addAll(operationIds.stream().filter(Objects::nonNull).collect(Collectors.toSet()));
        }
        List<SystemRoleOperation> roleOperationList = systemRoleOperationPersistence.findByRoleId(roleId);
        Set<Long> dbIds = roleOperationList.stream().map(SystemRoleOperation::getOperationId).collect(Collectors.toSet());

        roleOperationList.forEach(p -> {
            if(!targetOperationIds.contains(p.getOperationId())){
                p.setDisabled(true);
                systemRoleOperationPersistence.replace(p);
            }
        });

        Set<Long> insertIds = targetOperationIds.stream().filter(p -> !dbIds.contains(p)).collect(Collectors.toSet());
        if(CollectionUtils.isNotEmpty(insertIds)){
            List<SystemRoleOperation> insertList = insertIds.stream().map(p -> {
                SystemRoleOperation roleOperation = new SystemRoleOperation();
                roleOperation.setRoleId(roleId);
                roleOperation.setOperationId(p);
                roleOperation.setDisabled(false);
                return roleOperation;
            }).collect(Collectors.toList());
            systemRoleOperationPersistence.inserts(insertList);
        }
        return MapMessage.successMessage();
    }

    public List<SystemOperationView> getOperationsWithRoles(){
        List<SystemOperation> allOperationList = systemOperationPersistence.findAll();
        if(CollectionUtils.isEmpty(allOperationList)){
            return Collections.emptyList();
        }
        List<SystemOperationView> operationList = allOperationList.stream().map(SystemOperation::toViewData).collect(Collectors.toList());
        List<SystemRoleOperation> allRoleOperations = systemRoleOperationPersistence.findAll();
        Map<Long, List<Integer>> operationRolesMap = allRoleOperations.stream().collect(Collectors.groupingBy(SystemRoleOperation::getOperationId, Collectors.mapping(SystemRoleOperation::getRoleId, Collectors.toList())));

        operationList.forEach(p -> {
            if(operationRolesMap.containsKey(p.getId())){
                p.setRoleTypeList(operationRolesMap.get(p.getId()).stream().map(AgentRoleType::of).filter(Objects::nonNull).collect(Collectors.toList()));
            }
        });
        return operationList;
    }


    public SystemOperationView getOperationById(Long operationId){
        SystemOperation operation = systemOperationPersistence.load(operationId);
        if(operation == null){
            return null;
        }
        SystemOperationView view = operation.toViewData();
        List<SystemRoleOperation> roleOperationList = systemRoleOperationPersistence.findByOperationId(operationId);
        if(CollectionUtils.isNotEmpty(roleOperationList)){
            view.setRoleTypeList(roleOperationList.stream().map(p -> AgentRoleType.of(p.getRoleId())).filter(Objects::nonNull).collect(Collectors.toList()));
        }
        return view;
    }

    public MapMessage editOperation(Long operationId, String module, String subModule, String operationCode, String operationName, String comment){
        if(StringUtils.isBlank(module) || StringUtils.isBlank(subModule) || StringUtils.isBlank(operationName)){
            return MapMessage.errorMessage("模块，子模块，操作名称不能为空！");
        }
        SystemOperation operation;
        if(operationId != null && operationId > 0){
            operation = systemOperationPersistence.load(operationId);
            if(operation == null){
                return MapMessage.errorMessage("该页面元素不存在！");
            }
            if(!Objects.equals(operation.getModule(), module)
                    || !Objects.equals(operation.getSubModule(), subModule)
                    || !Objects.equals(operation.getOperationName(), operationName)
                    || !Objects.equals(operation.getComment(), comment)){
                operation.setModule(module);
                operation.setSubModule(subModule);
                operation.setOperationName(operationName);
                operation.setComment(comment);
                systemOperationPersistence.replace(operation);
            }
        }else {
            if(StringUtils.isBlank(operationCode)){
                return MapMessage.errorMessage("操作码不能为空！");
            }
            operation = new SystemOperation();
            operation.setModule(module);
            operation.setSubModule(subModule);
            operation.setOperationCode(operationCode);
            operation.setOperationName(operationName);
            operation.setComment(comment);
            operation.setDisabled(false);
            systemOperationPersistence.insert(operation);
        }
        return MapMessage.successMessage().add("operationId", operation.getId());
    }

    public MapMessage setOperationForRoles(Long operationId, Collection<AgentRoleType> roleTypes){
        Set<Integer> roleIds = new HashSet<>();
        if(CollectionUtils.isNotEmpty(roleTypes)){
            roleIds.addAll(roleTypes.stream().map(AgentRoleType::getId).collect(Collectors.toList()));
        }
        List<SystemRoleOperation> roleOperationList = systemRoleOperationPersistence.findByOperationId(operationId);
        Set<Integer> dbRoleList = roleOperationList.stream().map(SystemRoleOperation::getRoleId).collect(Collectors.toSet());

        roleOperationList.forEach(p -> {
            if(!roleIds.contains(p.getRoleId())){
                p.setDisabled(true);
                systemRoleOperationPersistence.replace(p);
            }
        });

        Set<Integer> insertIds = roleIds.stream().filter(p -> !dbRoleList.contains(p)).collect(Collectors.toSet());
        if(CollectionUtils.isNotEmpty(insertIds)){
            List<SystemRoleOperation> insertList = insertIds.stream().map(p -> {
                SystemRoleOperation roleOperation = new SystemRoleOperation();
                roleOperation.setRoleId(p);
                roleOperation.setOperationId(operationId);
                roleOperation.setDisabled(false);
                return roleOperation;
            }).collect(Collectors.toList());

            systemRoleOperationPersistence.inserts(insertList);
        }
        return MapMessage.successMessage();
    }

    public List<String> loadUserOperationCodes(Long userId){
        List<AgentRoleType> roleList = baseOrgService.getUserRoleList(userId);

        Set<Long> operationIds = new HashSet<>();
        roleList.forEach(p -> {
            List<SystemRoleOperation> roleOperationList = systemRoleOperationPersistence.findByRoleId(p.getId());
            if(CollectionUtils.isNotEmpty(roleOperationList)){
                operationIds.addAll(roleOperationList.stream().map(SystemRoleOperation::getOperationId).collect(Collectors.toSet()));
            }
        });
        Set<String> operationCodes = new HashSet<>();
        if(CollectionUtils.isNotEmpty(operationIds)){
            Map<Long, SystemOperation> elementMap = systemOperationPersistence.loads(operationIds);
            if(MapUtils.isNotEmpty(elementMap)){
                operationCodes.addAll(elementMap.values().stream().map(SystemOperation::getOperationCode).filter(StringUtils::isNotBlank).collect(Collectors.toSet()));
            }
        }
        return new ArrayList<>(operationCodes);
    }
}
