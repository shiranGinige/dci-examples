package v04.domain.structure.entity;

import v04.domain.structure.data.BalanceData;
import v04.domain.behavior.role.DestinationAccount;
import v04.api.DomainEntity;

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