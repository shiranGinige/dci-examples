package v06.domain.entity;

import v06.api.DomainEntity;
import v06.domain.data.Account;
import v06.domain.usecase.role.SourceAccountRole;
import v06.domain.usecase.role.DestinationAccountRole;

/**
 * Account entity
 * - Knows nothing about transactions
 */
public interface AccountEntity
      extends DomainEntity,

      // Data
      Account,

      // Roles
      SourceAccountRole,
      DestinationAccountRole
{
}
