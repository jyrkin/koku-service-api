/**
 * Sample class to demonstrate usage of UserInfoService interface
 */
package fi.koku.services.entity.userinfo.sample;

import java.util.List;
import java.util.logging.Logger;

import fi.koku.service.entity.userinfo.v1.impl.UserInfoServiceDummyImpl;
import fi.koku.services.entity.userinfo.v1.UserInfoService;
import fi.koku.services.entity.userinfo.v1.model.Group;
import fi.koku.services.entity.userinfo.v1.model.OrgUnit;
import fi.koku.services.entity.userinfo.v1.model.Registry;
import fi.koku.services.entity.userinfo.v1.model.Role;

/**
 * @author mikkope
 * 
 */
public class SampleClientUsage {

  private static final Logger LOG = Logger.getAnonymousLogger();

  public static void main(String args[]) {

    UserInfoService serv = new UserInfoServiceDummyImpl();

    // Scenario-1: User wants to archive log
    // Requires: LOG_ADMIN_ROLE
    // Other: "log" defines role in "log" context or domain. This has to be
    // described somewhere.
    // The reason is to reduce queries to subsystems (performance) and avoid
    // namespace collisions

    // Check user role
    List<Role> roles = serv.getUsersRoles("log", "111111-1111");
    LOG.info("size=" + roles.size());
    Role neededRole = new Role();
    neededRole.setId("LOG_ADMIN_ROLE");
    if (roles.contains(neededRole)) {
      LOG.info("SUCCESS: User with uid=111111-1111 had role.id=LOG_ADMIN_ROLE. We may proceed archiving log.");
    } else {
      LOG.info("FAILURE: User does not have LOG_ADMIN_ROLE and we may not proceed.");
    }

    // Scenario-2: Nurse wants to edit KKS-4year-plan
    // Requires: User is authorized to access data in that registry and user's
    // organization unit is "oid1".
    // Other:

    // Check registry permission
    List<Registry> regs = serv.getUsersAuthorizedRegistries("111111-1111");
    Registry neededReqistry = new Registry();
    neededReqistry.setId("healthcareregistry");
    if (regs.contains(neededReqistry)) {
      LOG.info("SUCCESS: User has permission to access registry data");

      // Check organization unit
      List<OrgUnit> orgUnits = serv.getUsersOrgUnits("tampere", "111111-1111");
      OrgUnit neededOrgUnit = new OrgUnit();
      neededOrgUnit.setId("oid1");
      if (orgUnits.contains(neededOrgUnit)) {
        LOG.info("SUCCESS: User belongs to porolahtiPK organization unit.");
      } else {
        LOG.info("FAILURE: User does not belong to porolahtiPK organization unit");
      }
    } else {
      LOG.info("FAILURE: User has not permission to accesss registry data.");
    }

    
    // Scenario-3: User wants to access data belongin to userGroup=gid1
    // Requires: User has to belong to userGroup=gid1 
    // Other:
    
    List<Group> groups = serv.getUsersGroups("context", "111111-1111");
    Group neededGroup = new Group();
    neededGroup.setId("gid1");
    if(groups.contains(neededGroup)){
      LOG.info("SUCCESS: User belongs to gid1, we may proceed.");
    }else{
      LOG.info("FAILURE: User does not belong to gid1. No access.");
    }
    
    
  }

}
