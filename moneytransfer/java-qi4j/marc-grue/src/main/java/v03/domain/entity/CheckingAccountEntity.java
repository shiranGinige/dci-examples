package v03.domain.entity;

import v03.api.DomainEntity;
import v03.behavior.role.DestinationAccount;
import v03.domain.data.BalanceData;

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
      DestinationAccount
{
}
