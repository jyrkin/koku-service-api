/*
 * Copyright 2012 Ixonos Plc, Finland. All rights reserved.
 * 
 * This file is part of Kohti kumppanuutta.
 *
 * This file is licensed under GNU LGPL version 3.
 * Please see the 'license.txt' file in the root directory of the package you received.
 * If you did not receive a license, please contact the copyright holder
 * (kohtikumppanuutta@ixonos.com).
 *
 */
package fi.koku.services.utility.portaluser.v1;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.koku.services.utility.portal.v1.PortalUserService;
import fi.koku.services.utility.portal.v1.PortalUserServicePortType;

/**
 * Simple factory class to provide access to Portal User service.
 * 
 * @author hekkata
 */
public class PortalUserServiceFactory {

  private String uid;
  private String pwd;
  private String endpointBaseUrl;
  private final URL wsdlLocation = getClass().getClassLoader().getResource("wsdl/portalUserService.wsdl");

  private static Logger log = LoggerFactory.getLogger(PortalUserServiceFactory.class);

  public PortalUserServiceFactory(String uid, String pwd, String endpointBaseUrl) {
    this.uid = uid;
    this.pwd = pwd;
    this.endpointBaseUrl = endpointBaseUrl;
  }

  public PortalUserServicePortType getPortalUserService() {
    if (wsdlLocation == null)
      log.error("wsdllocation=null");
    System.out.println("AAJOJFOASIJFOAISJFAOIJOFIJsojifO");
//    PortalUserService service = new PortalUserService(wsdlLocation, new QName(
//        "http://services.koku.fi/utility/portaluser/v1", "PortalUserService"));
    PortalUserService service = new PortalUserService(wsdlLocation, new QName(
     "http://services.koku.fi/utility/portal/v1", "portalUserService"));
    
    PortalUserServicePortType port = service.getPortalUserServiceSoap11Port();
    String epAddr = endpointBaseUrl + "/PortalUserServiceEndpointBean";
    log.debug("ep addr: " + epAddr);

    ((BindingProvider) port).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, epAddr);
    ((BindingProvider) port).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, uid);
    ((BindingProvider) port).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, pwd);
    log.debug("Created PortalUserServiceClient with epAddr=" + epAddr + ", uid=" + uid);

    return port;
  }
}
