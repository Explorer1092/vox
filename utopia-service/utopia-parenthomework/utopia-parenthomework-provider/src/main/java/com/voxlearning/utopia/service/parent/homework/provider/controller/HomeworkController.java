package com.voxlearning.utopia.service.parent.homework.provider.controller;

import com.voxlearning.alps.annotation.common.Singleton;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.api.context.ApplicationContextScanner;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author xuesong.zhang
 * @since 2018/4/9
 */

@Singleton
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@RequestMapping("/parenthomework-provider")
public class HomeworkController {

    public static final HomeworkController INSTANCE = new HomeworkController();

    @RequestMapping(value = "index.do", method = RequestMethod.GET)
    public String index(Model model, HttpServletRequest httpServletRequest) {

        String beanType = httpServletRequest.getParameter("beanType");
        if (beanType == null) {
            beanType = "";
        }

        Set<String> beans = ApplicationContextScanner.getInstance()
                .getBeansOfType(HomeworkController.class)
                .values()
                .stream()
                .map(p -> p.getClass().getName())
                .collect(Collectors.toSet());

        switch (beanType) {
            case "client":
                List<String> clientList = ApplicationContextScanner.getInstance().getBeansOfType(IPingable.class)
                        .values()
                        .stream()
                        .map(p -> p.getClass().getName())
                        .filter(p -> p.contains("Client"))
                        .collect(Collectors.toList());
                beans.addAll(clientList);
                model.addAttribute("serviceClassNameList", beans);
                break;
            case "impl":
                List<String> implList = ApplicationContextScanner.getInstance().getBeansOfType(IPingable.class)
                        .values()
                        .stream()
                        .map(p -> p.getClass().getName())
                        .filter(p -> !p.contains("Client"))
                        .collect(Collectors.toList());
                beans.addAll(implList);
                model.addAttribute("serviceClassNameList", beans);
                break;
            default:
//                model.addAttribute("commandCounter", CommandCounter.getInstance());
                break;
        }
        return "newhomework-provider/index";
    }

    @RequestMapping(value = "methods.do", method = RequestMethod.GET)
    public String methods(String serviceClassName, Model model) {
        model.addAttribute("serviceClassName", serviceClassName);
        try {
            Class<?> aClass = Class.forName(serviceClassName);
            if (aClass != null) {
//                model.addAttribute("methodMappers", MethodUtils.fetchAvailableEntranceMethods(aClass));
            }
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        return "newhomework-provider/methods";
    }

    @RequestMapping(value = "methoddetail.do", method = RequestMethod.GET)
    public String methodlist(String serviceClassName, String methodId, Model model, HttpServletRequest httpServletRequest) {
        model.addAttribute("serviceClassName", serviceClassName);
        model.addAttribute("methodId", methodId);

        try {
            Class<?> aClass = Class.forName(serviceClassName);
            if (aClass != null) {
//                MethodUtils.fetchAvailableEntranceMethods(aClass)
//                        .stream()
//                        .filter(p -> Objects.equals(p.getMethodId(), methodId))
//                        .map(MethodDetailMapper::getMethod)
//                        .findFirst()
//                        .ifPresent(invokeMethod -> model.addAttribute("parameterMappers", MethodUtils.fetchParameterMapper(httpServletRequest, invokeMethod)));
            }

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }

        return "newhomework-provider/methoddetail";
    }

    @RequestMapping(value = "methodinvoke.do", method = RequestMethod.POST)
    public String methodInvoke(String serviceClassName, String methodId, Model model, HttpServletRequest httpServletRequest) {
        model.addAttribute("serviceClassName", serviceClassName);
        model.addAttribute("methodId", methodId);
        Object resultData = null;

        try {

            Class<?> aClass = Class.forName(serviceClassName);
            Object bean = ApplicationContextScanner.getInstance().getBean(aClass);

            if (aClass != null) {
//                Method invokeMethod = MethodUtils.fetchAvailableEntranceMethods(aClass)
//                        .stream()
//                        .filter(p -> Objects.equals(p.getMethodId(), methodId))
//                        .map(MethodDetailMapper::getMethod)
//                        .findFirst()
//                        .orElse(null);
//                if (invokeMethod != null) {
//                    List<ParameterMapper> parameterMappers = MethodUtils.fetchParameterMapper(httpServletRequest, invokeMethod);
//                    model.addAttribute("parameterMappers", parameterMappers);
//                    Object invokeResult = invokeMethod.invoke(bean, parameterMappers.stream().map(ParameterMapper::getValue).toArray());
//                    if (invokeResult == null) {
//                        resultData = null;
//                    } else if (invokeResult instanceof AlpsFuture) {
//                        Object result = ((AlpsFuture) invokeResult).getUninterruptibly();
//                        resultData = JsonUtils.toJson(result);
//                    } else {
//                        resultData = JsonUtils.toJson(invokeResult);
//                    }
//                } else {
//                    resultData = JsonUtils.toJson(MapMessage.errorMessage("method not exist"));
//                }
            } else {
                resultData = JsonUtils.toJson(MapMessage.errorMessage("service not exist"));
            }
        } catch (Exception e) {
            resultData = JsonUtils.toJson(MapMessage.errorMessage(e.getMessage()));
        }
        if (resultData != null) {
            model.addAttribute("resultData", resultData);
        }
        return "newhomework-provider/methoddetail";
    }
}

