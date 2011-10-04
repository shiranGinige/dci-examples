package v05.domain.structure.entity;

import v05.domain.structure.data.BalanceData;
import v05.domain.behavior.role.DestinationAccount;
import v05.api.DomainEntity;

/**
 * Javadoc
 */
public interface CreditorEntity
      extends DomainEntity,

      // Data
      BalanceData,

      // Roles
      DestinationAccount
{
}