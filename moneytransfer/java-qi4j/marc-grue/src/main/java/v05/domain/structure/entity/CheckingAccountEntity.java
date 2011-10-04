package v05.domain.structure.entity;

import v05.api.DomainEntity;
import v05.domain.behavior.role.DestinationAccount;
import v05.domain.behavior.role.SourceAccount;
import v05.domain.structure.data.BalanceData;

/**
 * Checking account Entity
 * - Defines what Data (state) is associated with a checking account
 * - Contract with what Roles it can play
 */
public interface CheckingAccountEntity
      extends DomainEntity,

      // Data
      BalanceData,

      // Roles
      DestinationAccount,
      SourceAccount
{
}
