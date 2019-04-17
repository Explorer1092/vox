package com.voxlearning.utopia.cjlschool.support;

import com.unitever.cif.core.CifConfigInfo;
import com.unitever.cif.operation.AgentTemplate;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.cjlschool.data.CJLDataReceiveListener;

import javax.inject.Named;

/**
 * 陈经纶学校的一个账号只能一个服务使用
 * 暂时先这么处理吧
 * Created by Yuechen.Wang on 2017/8/14.
 */
@Named
public class CJLDataRequestTemplate extends SpringContainerSupport {

    private AgentTemplate agentTemplate;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();

        if (RuntimeMode.isTest()) {
            initTest();
        } else if (RuntimeMode.isProduction()) {
            agentTemplate = null;
        } else {
            agentTemplate = null;
        }
    }

    public AgentTemplate getTemplate() {
        return agentTemplate;
    }

    private void initTest() {
        CifConfigInfo config = new CifConfigInfo();
        agentTemplate = new AgentTemplate();
        //设置代理配置信息
        config.setAgentId("yiqizuoye");
        config.setAgentPassword("000000");
        config.setServerAddress("58.132.20.19");
        config.setServerPort(5672);
        config.setEncryptMessage(false);
        config.setSignMessage(false);
        config.setLogMessage(false);
        config.setTimeout(60);
        agentTemplate.setConfig(config);
        //设置代理监听器，监听数据（1、事件[增，删，改] 2、数据请求，数据响应）
        CJLDataReceiveListener agentListener = new CJLDataReceiveListener();
        CJLDataProcessorFactory factory = applicationContext.getBean(CJLDataProcessorFactory.class);
        agentListener.setDataProcessorFactory(factory);
        agentTemplate.setAgentListener(agentListener);
        //设置权限范围，可不做设置；（注意：1、若此处权限不设置，则获取服务端对此代理开放的所有权限;2、若设置，权限范围一定要在服务端开放的权限范围之内，否则权限申请失败）
//    	agentTemplate.addProvisionItem(new ProvisionItem("CIFStudents", "CIFDefault", false, false, false, false, false, true, true));
//    	agentTemplate.addProvisionItem(new ProvisionItem("CIFTeacher", "CIFDefault", false, false, false, false, false, true, true));
        //代理初始化
        agentTemplate.start();
    }

    private void initProduction() {
        CifConfigInfo config = new CifConfigInfo();
        agentTemplate = new AgentTemplate();
        //设置代理配置信息
        config.setAgentId("yiqizuoyeformal");
        config.setAgentPassword("000000");
        config.setServerAddress("58.132.20.19");
        config.setServerPort(5672);
        config.setEncryptMessage(false);
        config.setSignMessage(false);
        config.setLogMessage(false);
        config.setTimeout(60);
        agentTemplate.setConfig(config);
        //设置代理监听器，监听数据（1、事件[增，删，改] 2、数据请求，数据响应）
        CJLDataReceiveListener agentListener = new CJLDataReceiveListener();
        CJLDataProcessorFactory factory = applicationContext.getBean(CJLDataProcessorFactory.class);
        agentListener.setDataProcessorFactory(factory);
        agentTemplate.setAgentListener(agentListener);
        //设置权限范围，可不做设置；（注意：1、若此处权限不设置，则获取服务端对此代理开放的所有权限;2、若设置，权限范围一定要在服务端开放的权限范围之内，否则权限申请失败）
//    	agentTemplate.addProvisionItem(new ProvisionItem("CIFStudents", "CIFDefault", false, false, false, false, false, true, true));
//    	agentTemplate.addProvisionItem(new ProvisionItem("CIFTeacher", "CIFDefault", false, false, false, false, false, true, true));
        //代理初始化
        agentTemplate.start();
    }

}
