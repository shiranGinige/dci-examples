package v10.domain.rolemap;

import v10.domain.context.PayBillsContext;
import v10.domain.context.TransferMoneyContext;
import v10.domain.entity.BankEntity;

/**
 * Javadoc
 */
public interface BankRolemap
      extends BankEntity,

      // Methodful Roles
      TransferMoneyContext.BankRole,

      PayBillsContext.BankRole
{
}
