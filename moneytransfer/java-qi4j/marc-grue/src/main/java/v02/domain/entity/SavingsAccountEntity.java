package v02.domain.entity;

import v02.behavior.role.SourceAccount;
import v02.domain.data.SavingsAccountData;
import org.qi4j.api.entity.EntityComposite;

/**
 * Javadoc
 */
public interface SavingsAccountEntity
      extends EntityComposite,

      // Data
      SavingsAccountData,

      // Roles
      SourceAccount
{
}