package v06.domain.entity;

import v06.api.DomainEntity;
import v06.domain.data.Transaction;
import v06.domain.data.TransactionEntries;

/**
 * Transaction entity
 * - A transaction has transaction entries
 */
public interface TransactionEntity
   extends DomainEntity,
      Transaction,
      TransactionEntries
{
}
