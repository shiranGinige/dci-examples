package v06.domain.entity;

import v06.api.DomainEntity;
import v06.domain.data.Transactions;

/**
 * Transactions
 * - Could have been called "Bank", "Transaction log" etc. too
 * - we access transactions from here
 * - we could have different transaction logs, but for this example only this one
 */
public interface TransactionsEntity
   extends DomainEntity,
      Transactions
{
   public static final String TRANSACTIONS = "transactions";
}
