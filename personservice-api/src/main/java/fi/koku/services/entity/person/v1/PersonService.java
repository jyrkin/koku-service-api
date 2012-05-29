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
/**
 * Service that consists of Person related operations
 */
package fi.koku.services.entity.person.v1;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.koku.KoKuFaultException;

/**
 * Facade class for accessing person information.
 * 
 * @author mikkope
 * @author hanhian
 */
public class PersonService {

  private static final Logger LOG = LoggerFactory
      .getLogger(PersonService.class);
  private PersonInfoProvider infoProvider;

  public PersonService() {
    try {
      infoProvider = (PersonInfoProvider) this.getClass().getClassLoader()
          .loadClass(PersonConstants.PERSON_PROVIDER_IMPL_CLASS_NAME)
          .newInstance();

      LOG.info("PersonInfoProvider impl: " + infoProvider);
    } catch (Exception e) {
      throw new KoKuFaultException(98989,
          "Cannot instantiate PersonInfoProvider with class name: "
              + PersonConstants.PERSON_PROVIDER_IMPL_CLASS_NAME, e);
    }
  }

  /**
   * Returns list of persons with corresponding pics (hetus)
   * 
   * @param pics
   *          , string array of person Ids
   * @param domain
   *          , string constants that allows to add new implementations for
   *          different domains or backends
   * @param auditUserId
   *          , id of service user
   * @param auditComponentId
   *          , id of component that is using the service
   * @return list of Person -objects
   */
  public List<Person> getPersonsByPics(List<String> pics, final String domain,
      final String auditUserId, final String auditComponentId) {

    List<Person> personList = null;

    if (pics != null && !pics.isEmpty()) {

      // Check input parameters
      if (isNotNullOrEmpty(auditUserId) & isNotNullOrEmpty(auditComponentId)) {

        if (PersonConstants.PERSON_SERVICE_DOMAIN_CUSTOMER.equals(domain)) {
          // Search using CustomerService
          personList = infoProvider.getPersonsFromCustomerDomainWithPicList(
              pics, auditUserId, auditComponentId);

        } else if (PersonConstants.PERSON_SERVICE_DOMAIN_OFFICER.equals(domain)) {
          // Search using LdapService
          personList = infoProvider.getPersonsFromOfficerDomainWithPicList(
              pics, auditUserId, auditComponentId);

        } else {
          LOG.error("No valid domain was set. Search cannot be done");
        }

      } else {
        LOG.debug("Input parameters were null or empty. domain=" + domain
            + ", auditUserId=" + auditUserId + ", auditComponentId="
            + auditComponentId);
      }

    } else {
      LOG.debug("List of pics is null or empty. Do nothing.");
    }

    return personList;
  }

  /**
   * Returns list of persons with corresponding uids (currect case Loora portal
   * uids)
   * 
   * NOTICE: Using DOMAIN_CUSTOMER&OFFICER (Both) here creates "uids.size()"
   * times WS calls, because UserInformationService has only
   * "getUserByUid"-method
   * 
   * @param uids
   *          , string array of personIds
   * @param domain
   *          , string constants that allows to add new implementations for
   *          different domains or backends
   * @param auditUserId
   *          , id of service user
   * @param auditComponentId
   *          , id of component that is using the service
   * @return list of Person -objects
   */
  public List<Person> getPersonsByUids(List<String> uids, final String domain,
      final String auditUserId, final String auditComponentId) {

    List<Person> personList = null;

    if (uids != null && !uids.isEmpty()) {

      personList = new ArrayList<Person>(uids.size());

      // Check input parameters
      if (isNotNullOrEmpty(auditUserId) & isNotNullOrEmpty(auditComponentId)) {

        if (PersonConstants.PERSON_SERVICE_DOMAIN_OFFICER.equals(domain)) {
          // Search using LdapService
          personList = infoProvider.getPersonsFromOfficerDomainWithUidList(
              uids, auditUserId, auditComponentId);

        } else if (PersonConstants.PERSON_SERVICE_DOMAIN_CUSTOMER
            .equals(domain)) {
          personList = infoProvider.getPersonsFromCustomerDomainWithUidList(
              uids, auditUserId, auditComponentId);

        } else {
          LOG.error("No valid domain was set. Search cannot be done");
        }

      } else {
        LOG.debug("Input parameters were null or empty. domain=" + domain
            + ", auditUserId=" + auditUserId + ", auditComponentId="
            + auditComponentId);
      }

    } else {
      LOG.debug("List of uids is null or empty. Do nothing.");
    }

    return personList;
  }

  /**
   * Gets group ids from the system
   * 
   * @param domain
   * @param auditUserId
   * @param auditComponentId
   * @return customer group ids
   */
  public List<String> getGroupIds(final String domain, final String auditUserId,
      final String auditComponentId) {
    LOG.debug("Getting customer group ids");
    if (isNotNullOrEmpty(domain) & isNotNullOrEmpty(auditUserId) & isNotNullOrEmpty(auditComponentId)) {
      return infoProvider.getGroupIds(domain, auditUserId, auditComponentId);
    }
    LOG.debug("Input parameters were null or empty. domain=" +  domain + " auditUserId=" + auditUserId
        + ", auditComponentId=" + auditComponentId);
    return null;
  }

  /**
   * Gets customer group
   * 
   * @param domain
   * @param id of the group
   * @param auditUserId
   * @param auditComponentId
   * @return customer group
   */
  public Group getGroup(final String domain, final String id, final String auditUserId,
      final String auditComponentId) {

    LOG.debug("Getting group: " + id);
    if (isNotNullOrEmpty(id) && isNotNullOrEmpty(auditUserId)
        && isNotNullOrEmpty(auditComponentId)) {
      return infoProvider.getGroup(domain, id, auditUserId, auditComponentId);
    }
    LOG.debug("Input parameters were null or empty. id=" + id + " auditUserId="
        + auditUserId + ", auditComponentId=" + auditComponentId);
    return null;
  }

  private boolean isNotNullOrEmpty(String s) {
    if (s != null && !s.isEmpty()) {
      return true;
    } else {
      return false;
    }
  }
}
