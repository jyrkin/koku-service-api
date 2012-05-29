package fi.koku.services.entity.person.v1.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.arcusys.tampere.hrsoa.entity.User;
import fi.arcusys.tampere.hrsoa.ws.ldap.LdapService;
import fi.koku.services.common.kahva.LdapServiceFactory;
import fi.koku.services.entity.customer.v1.AuditInfoType;
import fi.koku.services.entity.customer.v1.CustomerQueryCriteriaType;
import fi.koku.services.entity.customer.v1.CustomerServiceFactory;
import fi.koku.services.entity.customer.v1.CustomerServicePortType;
import fi.koku.services.entity.customer.v1.CustomerType;
import fi.koku.services.entity.customer.v1.CustomersType;
import fi.koku.services.entity.customer.v1.PicsType;
import fi.koku.services.entity.customer.v1.ServiceFault;
import fi.koku.services.entity.person.v1.Group;
import fi.koku.services.entity.person.v1.Person;
import fi.koku.services.entity.person.v1.PersonConstants;
import fi.koku.services.entity.person.v1.PersonInfoProvider;
import fi.koku.services.entity.person.v1.PersonService;
import fi.koku.services.entity.userinformation.UserInformationConstants;
import fi.koku.services.entity.userinformation.v1.UserInformationServiceFactory;
import fi.koku.services.utility.user.v1.GroupIdsQueryParamType;
import fi.koku.services.utility.user.v1.GroupType;
import fi.koku.services.utility.user.v1.GroupsType;
import fi.koku.services.utility.user.v1.UserInfoServicePortType;
import fi.koku.services.utility.user.v1.UserType;
import fi.koku.services.utility.userinfo.v1.UserInfoServiceConstants;
import fi.koku.services.utility.userinfo.v1.UserInfoServiceFactory;
import fi.tampere.contract.municipalityportal.uis.UserInformationFault;
import fi.tampere.contract.municipalityportal.uis.UserInformationServicePortType;
import fi.tampere.schema.municipalityportal.uis.UserInformationType;

/**
 * Implementation class for PersonInfoProvider for Tampere environment.
 * 
 * @author hanhian
 */
public class TampereImpl implements PersonInfoProvider {

  private static final Logger LOG = LoggerFactory.getLogger(PersonService.class);
  private static String endpoint;

  private CustomerServicePortType customerService;
  private LdapService ldapService;
  private UserInformationServicePortType userInformationService;
  
  //for groups
  private UserInfoServicePortType userService;

  public TampereImpl() {
    
    // Initialize customerservice
    CustomerServiceFactory customerServiceFactory = new CustomerServiceFactory(
        PersonConstants.CUSTOMER_SERVICE_USER_ID, PersonConstants.CUSTOMER_SERVICE_PASSWORD,
        PersonConstants.CUSTOMER_SERVICE_ENDPOINT);
    customerService = customerServiceFactory.getCustomerService();

    // Initialize LdapService (aka KahvaService)
    LdapServiceFactory f = new LdapServiceFactory(PersonConstants.KAHVA_SERVICE_FULL_URL);
    ldapService = f.getOrganizationService();
    endpoint = PersonConstants.KAHVA_SERVICE_FULL_URL;

    // Initialize UserInformationService
    UserInformationServiceFactory uisFactory = new UserInformationServiceFactory(
        PersonConstants.USER_INFORMATION_SERVICE_USER_ID, PersonConstants.USER_INFORMATION_SERVICE_PASSWORD,
        UserInformationConstants.USER_INFORMATION_SERVICE_FULL_URL);
    userInformationService = uisFactory.getUserInformationService();
    
    UserInfoServiceFactory factory = new UserInfoServiceFactory(PersonConstants.USER_INFO_SERVICE_USER_ID,
        PersonConstants.USER_INFO_SERVICE_PASSWORD, PersonConstants.USER_INFO_SERVICE_ENDPOINT);
    userService = factory.getUserInfoService();
  }

  @Override
  public List<Person> getPersonsFromCustomerDomainWithUidList(List<String> uids, String auditUserId,
      String auditComponentId) {
    // Init array
    List<Person> personList = new ArrayList<Person>(uids.size());

    // Search first from UserInformationService (Kunpo) to get hetu with uid
    // #TODO# If UserInformationService (external) is updated to accept list of
    // uids, this implementation
    // could also be changed to use that.
    UserInformationType u = null;
    for (String uid : uids) {
      try {
        u = userInformationService.getSsnByUsername(uid);
        if (u != null) {
          personList.add(new Person(u.getSsn(), u.getFirstName(), u.getLastName()));
        }
      } catch (UserInformationFault e) {
        LOG.error("Failed to get person from UserInformationService with uid=" + uid, e);
      }
    }

    // NOTICE: You could also ask Person information from CustomerService using
    // hetu
    // personList = getPersonsFromCustomerDomainWithPicList(pics, auditUserId,
    // auditComponentId);

    return personList;
  }

  @Override
  public List<Person> getPersonsFromOfficerDomainWithUidList(List<String> uids, String auditUserId,
      String auditComponentId) {
    // Init array
    List<Person> personList = new ArrayList<Person>(uids.size());

    for (String uid : uids) {

      try {
        User userFromWS = ldapService.getUserById(uid);
        personList.add(new Person(userFromWS.getSsn(), userFromWS.getFirstName(), userFromWS.getLastName()));
      } catch (Exception e) {
        LOG.error("Failed to get Person data from external source: WS.endpoint=" + endpoint, e);
      }

    }
    return personList;
  }

  @Override
  public List<Person> getPersonsFromCustomerDomainWithPicList(List<String> pics, String auditUserId,
      String auditComponentId) {
    List<Person> personList = new ArrayList<Person>(pics.size());

    AuditInfoType customerAuditInfo = new AuditInfoType();
    customerAuditInfo.setComponent(auditComponentId);
    customerAuditInfo.setUserId(auditUserId);

    CustomerQueryCriteriaType query = new CustomerQueryCriteriaType();

    // Add pics to criteria
    PicsType picsType = new PicsType();
    for (String pic : pics) {
      picsType.getPic().add(pic);
    }
    query.setPics(picsType);
    try {
      CustomersType customersType = customerService.opQueryCustomers(query, customerAuditInfo);

      // Traverse data from WS and put pic(hetu), firstname and lastname to
      // personList -array
      for (CustomerType c : customersType.getCustomer()) {
        personList.add(new Person(c.getHenkiloTunnus(), c.getEtuNimi(), c.getSukuNimi()));
      }

    } catch (ServiceFault e) {
      LOG.error("Failed to query users from customerService. pics.size=" + pics.size() + ", auditUserId=" + auditUserId
          + ", auditComponentId=" + auditComponentId, e);
      personList = null;
    }

    return personList;
  }

  @Override
  public List<Person> getPersonsFromOfficerDomainWithPicList(List<String> pics, String auditUserId,
      String auditComponentId) {
    // Initialize array
    List<Person> personList = new ArrayList<Person>(pics.size());

    // Loop the pic array
    // #TODO# This could be a place to improve
    // Current external LdapService interface is limited to query one user at
    // time, which isn't efficient.
    // Usual usage is still only one user
    for (String pic : pics) {

      try {
        User userFromWS = ldapService.getUserBySSN(pic);
        personList.add(new Person(userFromWS.getSsn(), userFromWS.getFirstName(), userFromWS.getLastName()));
      } catch (Exception e) {
        LOG.error("Failed to get User data from external source: WS.endpoint=" + endpoint, e);
      }

    }
    return personList;
  }
  
  @Override
  public List<String> getGroupIds(String domain, String auditUserId,
      String auditComponentId ) {
    GroupsType groups = getGroups(domain, "*");
    
    List<String> tmp = new ArrayList<String>();
    
    if ( groups != null ) {
      for ( GroupType gt : groups.getGroup() ) {
        tmp.add(gt.getGroupId() );
      }
    }
    
    return tmp;
  }

  private GroupsType getGroups( String domain, String filter  ) {
    GroupsType groups = null;
    GroupIdsQueryParamType param = new GroupIdsQueryParamType();
    domain = domain.equals(PersonConstants.PERSON_SERVICE_DOMAIN_OFFICER) ? UserInfoServiceConstants.USER_INFO_SERVICE_DOMAIN_OFFICER 
                                                                          : UserInfoServiceConstants.USER_INFO_SERVICE_DOMAIN_CUSTOMER;
    param.setDomain(domain );
    param.getGroupId().add(filter);
    
    try {
      groups = userService.opGetGroupsByIds(param);
    } catch ( Exception e ) {
      LOG.error("Failed to get group data from user info service", e);
    }
    return groups;
  }

  @Override
  public Group getGroup(String domain, String groupId, String auditUserId, String auditComponentId ) {
    GroupsType groups = getGroups(domain, groupId);
    Group group = new Group();
    
    if ( groups != null ) {
      List<String> list = new ArrayList<String>();
      for ( GroupType gt : groups.getGroup() ) {
         group.setId(gt.getGroupId());
         List<UserType> users = gt.getMembers();
         
         if ( users != null ) {
           for ( UserType u : users ) {
             list.add( u.getUserId() );
           }
           
           List<Person> persons = getPersonsFromCustomerDomainWithUidList(list, auditUserId, auditComponentId);
           group.setPersons(persons);
         }
      }
    }    
    return group;
  }
}
