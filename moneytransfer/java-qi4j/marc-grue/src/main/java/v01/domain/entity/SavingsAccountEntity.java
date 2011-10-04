package v01.domain.entity;

import v01.behavior.role.MoneySourceRole;
import v01.domain.data.SavingsAccountData;
import org.qi4j.api.entity.EntityComposite;

/**
 * Javadoc
 */
public interface SavingsAccountEntity
      extends EntityComposite,

      // Data
      SavingsAccountData,

      // Roles
      MoneySourceRole
{
}