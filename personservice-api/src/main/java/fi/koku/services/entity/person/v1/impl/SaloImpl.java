package fi.koku.services.entity.person.v1.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import fi.koku.services.utility.user.v1.GroupIdsQueryParamType;
import fi.koku.services.utility.user.v1.GroupType;
import fi.koku.services.utility.user.v1.GroupsType;
import fi.koku.services.utility.user.v1.UserIdsQueryParamType;
import fi.koku.services.utility.user.v1.UserInfoServicePortType;
import fi.koku.services.utility.user.v1.UserPicsQueryParamType;
import fi.koku.services.utility.user.v1.UserType;
import fi.koku.services.utility.user.v1.UsersType;
import fi.koku.services.utility.userinfo.v1.UserInfoServiceConstants;
import fi.koku.services.utility.userinfo.v1.UserInfoServiceFactory;

/**
 * Implementation class for PersonInfoProvider for Salo environment.
 * 
 * @author hanhian
 */
public class SaloImpl implements PersonInfoProvider {

  private static final Logger LOG = LoggerFactory.getLogger(SaloImpl.class);

  private CustomerServicePortType customerService;
  private UserInfoServicePortType userService;
  

  public SaloImpl() {
    // Initialize customerservice
    CustomerServiceFactory customerServiceFactory = new CustomerServiceFactory(
        PersonConstants.CUSTOMER_SERVICE_USER_ID, PersonConstants.CUSTOMER_SERVICE_PASSWORD,
        PersonConstants.CUSTOMER_SERVICE_ENDPOINT);
    customerService = customerServiceFactory.getCustomerService();

    UserInfoServiceFactory factory = new UserInfoServiceFactory(PersonConstants.USER_INFO_SERVICE_USER_ID,
        PersonConstants.USER_INFO_SERVICE_PASSWORD, PersonConstants.USER_INFO_SERVICE_ENDPOINT);
    userService = factory.getUserInfoService();
  }

  @Override
  public List<Person> getPersonsFromCustomerDomainWithUidList(List<String> uids, String auditUserId,
      String auditComponentId) {

    List<Person> personList = new ArrayList<Person>();
    UserIdsQueryParamType uidsQueryType = new UserIdsQueryParamType();
    uidsQueryType.setDomain(UserInfoServiceConstants.USER_INFO_SERVICE_DOMAIN_CUSTOMER);
    uidsQueryType.getId().addAll(uids);
    try {
      UsersType usersType = userService.opGetUsersByIds(uidsQueryType);
      for (UserType userType : usersType.getUser()) {
        personList.add(new Person(userType.getPic(), userType.getFirstname(), userType.getLastname()));
      }
    } catch (Exception e) {
      LOG.error("Failed to get persons from UserInfoService.", e);
    }

    // NOTICE: You could also ask Person information from CustomerService
    // using hetu
    // personList = getPersonsFromCustomerDomainWithPicList(pics,
    // auditUserId, auditComponentId);
    return personList;
  }

  @Override
  public List<Person> getPersonsFromOfficerDomainWithUidList(List<String> uids, String auditUserId,
      String auditComponentId) {

    UserIdsQueryParamType uidsQueryType = new UserIdsQueryParamType();
    uidsQueryType.setDomain(UserInfoServiceConstants.USER_INFO_SERVICE_DOMAIN_OFFICER);
    uidsQueryType.getId().addAll(uids);
    UsersType users = null;

    try {
      users = userService.opGetUsersByIds(uidsQueryType);
    } catch (Exception e) {
      LOG.error("Failed to get User data from user info service", e);
    }

    List<Person> personList = new ArrayList<Person>(uids.size());
    if (users != null) {
      for (UserType emp : users.getUser()) {
        personList.add(new Person(emp.getPic(), emp.getFirstname(), emp.getLastname()));
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

// Tällä metodilla kutsu saadaan tehtyä LDAP:piin
//  @Override
//  public List<Person> getPersonsFromCustomerDomainWithPicList(List<String> pics, String auditUserId,
//      String auditComponentId) {
//    
//    UserPicsQueryParamType picsQueryType = new UserPicsQueryParamType();
//    picsQueryType.setDomain(UserInfoServiceConstants.USER_INFO_SERVICE_DOMAIN_CUSTOMER);
//    picsQueryType.getPic().addAll(pics);
//    UsersType users = null;
//    
//    try {
//      users = userService.opGetUsersByPics(picsQueryType);
//    } catch (Exception e) {
//      LOG.error("Failed to get User data from user info service", e);
//    }
//
//    List<Person> personList = new ArrayList<Person>(pics.size());
//    if (users != null) {
//      for (UserType emp : users.getUser()) {
//        personList.add(new Person(emp.getPic(), emp.getFirstname(), emp.getLastname()));
//      }
//    }
//
//    return personList;    
//  }

  @Override
  public List<Person> getPersonsFromOfficerDomainWithPicList(List<String> pics, String auditUserId,
      String auditComponentId) {

    UserPicsQueryParamType picsQueryType = new UserPicsQueryParamType();
    picsQueryType.setDomain(UserInfoServiceConstants.USER_INFO_SERVICE_DOMAIN_OFFICER);
    picsQueryType.getPic().addAll(pics);
    UsersType users = null;

    try {
      users = userService.opGetUsersByPics(picsQueryType);
    } catch (Exception e) {
      LOG.error("Failed to get User data from user info service", e);
    }

    List<Person> personList = new ArrayList<Person>(pics.size());
    if (users != null) {
      for (UserType emp : users.getUser()) {
        personList.add(new Person(emp.getPic(), emp.getFirstname(), emp.getLastname()));
      }
    }

    return personList;
  }

  @Override
  public List<String> getGroupIds(String domain, String auditUserId,
      String auditComponentId ) {
    GroupsType groups = getGroups(domain,"*");
    
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
    
    param.setDomain( domain );
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
    Group group = null;
    
    if ( groups != null ) {
      List<String> list = new ArrayList<String>();
      for ( GroupType gt : groups.getGroup() ) {
         group = new Group();
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