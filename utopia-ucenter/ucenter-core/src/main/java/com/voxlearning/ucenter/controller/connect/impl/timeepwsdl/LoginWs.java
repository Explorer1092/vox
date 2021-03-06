
/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.ucenter.controller.connect.impl.timeepwsdl;

import com.voxlearning.utopia.core.config.CommonConfiguration;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.2.9-b130926.1035
 * Generated source version: 2.2
 * 
 */
@WebServiceClient(name = "LoginWs", targetNamespace = "http://webservice.xueshe17.com", wsdlLocation = "http://60.174.236.86:9000/services/LoginWs?wsdl")
public class LoginWs
    extends Service
{

    private final static URL LOGINWS_WSDL_LOCATION;
    private final static WebServiceException LOGINWS_EXCEPTION;
    private final static QName LOGINWS_QNAME = new QName("http://webservice.xueshe17.com", "LoginWs");

    static {
        URL url = null;
        WebServiceException e = null;
        try {
            String timeepLoginWsUrl = CommonConfiguration.getInstance().getTimeepLoginWsUrl();
            url = new URL(timeepLoginWsUrl);
        } catch (MalformedURLException ex) {
            e = new WebServiceException(ex);
        }
        LOGINWS_WSDL_LOCATION = url;
        LOGINWS_EXCEPTION = e;
    }

    public LoginWs() {
        super(__getWsdlLocation(), LOGINWS_QNAME);
    }

    public LoginWs(WebServiceFeature... features) {
        super(__getWsdlLocation(), LOGINWS_QNAME, features);
    }

    public LoginWs(URL wsdlLocation) {
        super(wsdlLocation, LOGINWS_QNAME);
    }

    public LoginWs(URL wsdlLocation, WebServiceFeature... features) {
        super(wsdlLocation, LOGINWS_QNAME, features);
    }

    public LoginWs(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public LoginWs(URL wsdlLocation, QName serviceName, WebServiceFeature... features) {
        super(wsdlLocation, serviceName, features);
    }

    /**
     * 
     * @return
     *     returns LoginWsPortType
     */
    @WebEndpoint(name = "LoginWsHttpPort")
    public LoginWsPortType getLoginWsHttpPort() {
        return super.getPort(new QName("http://webservice.xueshe17.com", "LoginWsHttpPort"), LoginWsPortType.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns LoginWsPortType
     */
    @WebEndpoint(name = "LoginWsHttpPort")
    public LoginWsPortType getLoginWsHttpPort(WebServiceFeature... features) {
        return super.getPort(new QName("http://webservice.xueshe17.com", "LoginWsHttpPort"), LoginWsPortType.class, features);
    }

    private static URL __getWsdlLocation() {
        if (LOGINWS_EXCEPTION!= null) {
            throw LOGINWS_EXCEPTION;
        }
        return LOGINWS_WSDL_LOCATION;
    }

}
