package com.voxlearning.washington.controller.connect;

import com.voxlearning.utopia.service.user.api.constants.SsoConnections;
import com.voxlearning.washington.controller.connect.impl.*;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by Alex on 14-10-15.
 */
@Named
@Deprecated
public class SsoConnectorFactory {
    @Inject
    private MdjeduSsoConnector mdjeduSsoConnector;
    @Inject
    private QqSsoConnector qqSsoConnector;
    @Inject
    private GwchinaSsoConnector gwchinaSsoConnector;
    @Inject
    private JiaozuoJyt123SsoConnector jiaozuoJyt123SsoConnector;
    @Inject
    private FjeduoSsoConnector fjeduoSsoConnector;
    @Inject
    private JxhlwSsoConnector jxhlwSsoConnector;
    @Inject
    private CzaeduSsoConnector czaeduSsoConnector;
    @Inject
    private CneduSsoConnector cneduSsoConnector;
    @Inject
    private YzeduSsoConnector yzeduSsoConnector;
    @Inject
    private NceduSsoConnector nceduSsoConnector;
    @Inject
    private ChengruiSsoConnector chengruiSsoConnector;

    public AbstractSsoConnector getSsoConnector(SsoConnections connectionInfo) {
        if (SsoConnections.MDJEDU == connectionInfo) {
            return mdjeduSsoConnector;
        } else if (SsoConnections.QQ == connectionInfo) {
            return qqSsoConnector;
        } else if (SsoConnections.GWCHINA == connectionInfo) {
            return gwchinaSsoConnector;
        } else if (SsoConnections.JiaozuoJyt == connectionInfo) {
            return jiaozuoJyt123SsoConnector;
        } else if (SsoConnections.Fjedu == connectionInfo) {
            return fjeduoSsoConnector;
        } else if (SsoConnections.Jxhlw == connectionInfo) {
            return jxhlwSsoConnector;
        } else if (SsoConnections.Czaedu == connectionInfo) {
            return czaeduSsoConnector;
        } else if (SsoConnections.Cnedu == connectionInfo) {
            return cneduSsoConnector;
        } else if (SsoConnections.Yzedu == connectionInfo) {
            return yzeduSsoConnector;
        } else if (SsoConnections.Ncedu == connectionInfo) {
            return nceduSsoConnector;
        } else if (SsoConnections.Chengrui == connectionInfo) {
            return chengruiSsoConnector;
        }
        return null;
    }
}
