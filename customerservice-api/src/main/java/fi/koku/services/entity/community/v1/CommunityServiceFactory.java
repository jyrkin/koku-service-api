/*
 * Copyright 2012 Ixonos Plc, Finland. All rights reserved.
 * 
 * This file is part of Kohti kumppanuutta.
 *
 * This file is licensed under GNU LGPL version 3.
 * Please see the 'license.txt' file in the root directory of the package you received.
 * If you did not receive a license, please contact the copyright holder
 * (http://www.ixonos.com/).
 *
 */
package fi.koku.services.entity.community.v1;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for community service. 
 * 
 * @author aspluma
 */
public class CommunityServiceFactory {
  private String uid;
  private String pwd;
  private String endpointBaseUrl;
  private final URL wsdlLocation = getClass().getClassLoader().getResource("wsdl/communityService.wsdl");
  private static Logger log = LoggerFactory.getLogger(CommunityServiceFactory.class);

  public CommunityServiceFactory(String uid, String pwd, String endpointBaseUrl) {
    this.uid = uid;
    this.pwd = pwd;
    this.endpointBaseUrl = endpointBaseUrl;
  }

  public CommunityServicePortType getCommunityService() {
    if(wsdlLocation == null)
      log.error("wsdllocation=null");
    CommunityService service = new CommunityService(wsdlLocation, new QName(
        "http://services.koku.fi/entity/community/v1", "communityService"));
    CommunityServicePortType port = service.getCommunityServiceSoap11Port();
    String epAddr = endpointBaseUrl + "/CommunityServiceEndpointBean";
    log.debug("ep addr: "+epAddr);

    ((BindingProvider) port).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, epAddr);
    ((BindingProvider) port).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, uid);
    ((BindingProvider) port).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, pwd);
    return port;
  }
  
  public static AuditInfoType createAuditInfoType(String component, String userPic) {
    AuditInfoType audit = new AuditInfoType();
    audit.setComponent(component);
    audit.setUserId(userPic); 
    return audit;
  }
}