package v05.domain.structure.entity;

import v05.api.DomainEntity;
import v05.domain.behavior.role.SourceAccount;
import v05.domain.structure.data.BalanceData;

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