/**
 * Service factory class for LogService
 */
package fi.koku.services.utility.log.v1;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author mikkope
 *
 */
public class LogServiceFactory {


private String uid;
private String pwd;
private String endpointBaseUrl;
private String implVersion = "0.0.1-SNAPSHOT";
private final URL wsdlLocation = getClass().getClassLoader().getResource("/wsdl/logService.wsdl");

private static Logger log = LoggerFactory.getLogger(LogServiceFactory.class);

public LogServiceFactory(String uid, String pwd, String endpointBaseUrl) {
  this.uid = uid;
  this.pwd = pwd;
  this.endpointBaseUrl = endpointBaseUrl;
}


public LogServicePortType getLogService() {
  LogService service = new LogService(wsdlLocation, new QName(
      "http://services.koku.fi/utility/log/v1", "logService"));
  LogServicePortType port = service.getLogServiceSoap11Port();
  String epAddr = endpointBaseUrl + "/lok-service-" + implVersion + "/LogServiceEndpointBean";
  
  ((BindingProvider) port).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, epAddr);
  ((BindingProvider) port).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, uid);
  ((BindingProvider) port).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, pwd);

  //#TODO# Check if log level should be info -> debug and decide if uid and pwd should be logged at all!
  log.info("Created logServiceClient with epAddr="+epAddr+", uid="+uid+" and pwd="+pwd);
  
  return port;
}

}
