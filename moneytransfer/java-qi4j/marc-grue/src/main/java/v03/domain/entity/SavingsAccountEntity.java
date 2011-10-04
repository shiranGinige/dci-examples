package v03.domain.entity;

import v03.api.DomainEntity;
import v03.behavior.role.SourceAccount;
import v03.domain.data.BalanceData;

/**
 * Javadoc
 */
public interface SavingsAccountEntity
      extends DomainEntity,

      // Data
      BalanceData,

      // Roles
      SourceAccount
{
}