package com.voxlearning.ucenter.controller.connect;

import com.voxlearning.ucenter.controller.connect.impl.*;
import com.voxlearning.utopia.service.user.api.constants.SsoConnections;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Alex on 14-10-15.
 *
 * @author xinxin 2015-12-18
 */
@Named
public class SsoConnectorFactory implements InitializingBean, ApplicationContextAware {
    private ApplicationContext applicationContext;
    private Map<SsoConnections, AbstractSsoConnector> connectorMap;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        connectorMap = new HashMap<>();

        connectorMap.put(SsoConnections.MDJEDU, applicationContext.getBean(MdjeduSsoConnector.class));
        connectorMap.put(SsoConnections.QQ, applicationContext.getBean(QqSsoConnector.class));
        connectorMap.put(SsoConnections.GWCHINA, applicationContext.getBean(GwchinaSsoConnector.class));
        connectorMap.put(SsoConnections.JiaozuoJyt, applicationContext.getBean(JiaozuoJyt123SsoConnector.class));
        connectorMap.put(SsoConnections.Fjedu, applicationContext.getBean(FjeduoSsoConnector.class));
        connectorMap.put(SsoConnections.Jxhlw, applicationContext.getBean(JxhlwSsoConnector.class));
        connectorMap.put(SsoConnections.Czaedu, applicationContext.getBean(CzaeduSsoConnector.class));
        connectorMap.put(SsoConnections.Cnedu, applicationContext.getBean(CneduSsoConnector.class));
        connectorMap.put(SsoConnections.Yzedu, applicationContext.getBean(YzeduSsoConnector.class));
        connectorMap.put(SsoConnections.Ncedu, applicationContext.getBean(NceduSsoConnector.class));
        connectorMap.put(SsoConnections.Chengrui, applicationContext.getBean(ChengruiSsoConnector.class));

        // new added
        connectorMap.put(SsoConnections.Timeep, applicationContext.getBean(TimeepConnector.class));
        connectorMap.put(SsoConnections.Ustalk, applicationContext.getBean(UsTalkSsoConnector.class));
        connectorMap.put(SsoConnections.Xueba, applicationContext.getBean(XuebaSsoConnector.class));
        connectorMap.put(SsoConnections.A17xue, applicationContext.getBean(A17XueSsoConnector.class));
        connectorMap.put(SsoConnections.CjlSchool, applicationContext.getBean(CJLSsoConnector.class));
        connectorMap.put(SsoConnections.Seiue, applicationContext.getBean(SeiueSsoConnector.class));
        connectorMap.put(SsoConnections.Jmyedu, applicationContext.getBean(JmyeduSsoConnector.class));
        connectorMap.put(SsoConnections.JleduPs, applicationContext.getBean(JleduyunSsoConnector.class));
        connectorMap.put(SsoConnections.JleduMs, applicationContext.getBean(JleduyunSsoConnector.class));
        connectorMap.put(SsoConnections.A17xueyunketang, applicationContext.getBean(XueAppServerSsoConnector.class));
        connectorMap.put(SsoConnections.SxeduPs, applicationContext.getBean(SxeduSsoConnnector.class));
        connectorMap.put(SsoConnections.SxeduMs, applicationContext.getBean(SxeduSsoConnnector.class));
    }

    public AbstractSsoConnector getSsoConnector(SsoConnections connectionInfo) {
        return connectorMap.get(connectionInfo);
    }
}
