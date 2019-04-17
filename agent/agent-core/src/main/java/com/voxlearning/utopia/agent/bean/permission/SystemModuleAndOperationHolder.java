package com.voxlearning.utopia.agent.bean.permission;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.agent.annotation.Module;
import com.voxlearning.utopia.agent.utils.PathUtils;
import com.voxlearning.utopia.agent.view.permission.ModuleAndOperationView;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 保存系统中所有的功能模块和对应的操作
 *
 * @author song.wang
 * @date 2018/5/10
 */
public class SystemModuleAndOperationHolder {

    private List<ModuleAndOperation> moduleAndOperationList = null;
    private SystemModuleAndOperationHolder(){
        initSystemModuleAndOperation();
    }

    private void initSystemModuleAndOperation(){
        moduleAndOperationList = scanningModuleAndOperation("com.voxlearning.utopia.agent.controller");
    }

    private static class InnerHolder{
        private static final SystemModuleAndOperationHolder instance = new SystemModuleAndOperationHolder();
    }

    public static SystemModuleAndOperationHolder getInstance(){
        return InnerHolder.instance;
    }

    // 返回系统模块操作
    public List<ModuleAndOperationView> getSystemModuleAndOperation(){
        if(CollectionUtils.isEmpty(this.moduleAndOperationList)){
            return new ArrayList<>();
        }
        return this.moduleAndOperationList.stream().map(ModuleAndOperation::toViewData).collect(Collectors.toList());
    }

    private static List<ModuleAndOperation> scanningModuleAndOperation(String... basePackages){

        List<ModuleAndOperation> moduleAndOperationList = new ArrayList<>();
        if(basePackages == null){
            return moduleAndOperationList;
        }
        try {
            ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
            scanner.addIncludeFilter(new AnnotationTypeFilter(Controller.class));
            Set<BeanDefinition> beanSet = new HashSet<>();
            for(String basePackage : basePackages){
                if(StringUtils.isBlank(basePackage)){
                    continue;
                }
                beanSet.addAll(scanner.findCandidateComponents(basePackage));
            }

            for (BeanDefinition def : beanSet) {
                String beanClassName = def.getBeanClassName();
                Class<?> clazz = Class.forName(beanClassName);
                RequestMapping clazzReqMapping = clazz.getAnnotation(RequestMapping.class);
                Module clazzModule = clazz.getAnnotation(Module.class);
                Deprecated clazzDeprecated = clazz.getAnnotation(Deprecated.class);
                String clazzModuleName = clazzModule == null ? "" : clazzModule.value();
                String clazzSubModuleName = clazzModule == null ? "" : clazzModule.subModule();

                List<String> claReqMappingUrlList = new ArrayList<>();
                if(clazzReqMapping != null){
                    for(String reqMapping : clazzReqMapping.value()){
                        claReqMappingUrlList.add(PathUtils.resolvePath(reqMapping));
                    }
                }else {
                    claReqMappingUrlList.add("");
                }


                Method[] methods = clazz.getDeclaredMethods();
                for (Method method : methods){
                    RequestMapping methodReqMapping = method.getAnnotation(RequestMapping.class);
                    if(methodReqMapping == null || methodReqMapping.value().length == 0){
                        continue;
                    }
                    Module methodModule = method.getAnnotation(Module.class);
                    Deprecated methodDeprecated = method.getAnnotation(Deprecated.class);
                    String moduleName = clazzModuleName;
                    String subModuleName = clazzSubModuleName;
                    if(methodModule != null){
                        if(StringUtils.isNotBlank(methodModule.value())){
                            moduleName = methodModule.value();
                            subModuleName = methodModule.subModule();
                        }else if(StringUtils.isNotBlank(methodModule.subModule())){
                            subModuleName = methodModule.subModule();
                        }
                    }

                    List<String> urlPathList = new ArrayList<>();
                    for(String methodReqMappingUrl : methodReqMapping.value()){
                        methodReqMappingUrl = PathUtils.resolvePath(methodReqMappingUrl);
                        if(StringUtils.isNotBlank(methodReqMappingUrl)){
                            if(CollectionUtils.isEmpty(claReqMappingUrlList)){
                                urlPathList.add(methodReqMappingUrl);
                            }else {
                                for (String claReqMappingUrl : claReqMappingUrlList) {
                                    if(StringUtils.isNotBlank(claReqMappingUrl)){
                                        urlPathList.add(claReqMappingUrl + "/" + methodReqMappingUrl);
                                    }else {
                                        urlPathList.add(methodReqMappingUrl);
                                    }
                                }
                            }
                        }
                    }

                    boolean deprecated = clazzDeprecated != null || methodDeprecated != null;

                    for(String urlPath : urlPathList){
                        ModuleAndOperation moduleAndOperation = new ModuleAndOperation();
                        moduleAndOperation.setModule(moduleName);
                        moduleAndOperation.setSubModule(subModuleName);
                        moduleAndOperation.setPath(urlPath);
                        moduleAndOperation.setOperationDesc(methodReqMapping.name());
                        moduleAndOperation.setDeprecated(deprecated);
                        moduleAndOperationList.add(moduleAndOperation);
                    }
                }
            }
        } catch (Exception ignored) {

        }
        return moduleAndOperationList;
    }

}
