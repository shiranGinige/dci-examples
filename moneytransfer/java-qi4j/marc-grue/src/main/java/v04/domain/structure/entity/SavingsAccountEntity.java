package v04.domain.structure.entity;

import v04.api.DomainEntity;
import v04.domain.behavior.role.SourceAccount;
import v04.domain.structure.data.BalanceData;

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