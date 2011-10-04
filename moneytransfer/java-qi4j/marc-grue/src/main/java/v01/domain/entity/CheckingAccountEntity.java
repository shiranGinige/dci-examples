package v01.domain.entity;

import v01.behavior.role.MoneySinkRole;
import v01.domain.data.CheckingAccountData;
import org.qi4j.api.entity.EntityComposite;

/**
 * Checking account Entity
 * - Defines what Data (state) is associated with a checking account
 * - Contract with what Roles it can play
 */
public interface CheckingAccountEntity
      extends EntityComposite,

      // Data
      CheckingAccountData,

      // Roles
      MoneySinkRole
{
}
