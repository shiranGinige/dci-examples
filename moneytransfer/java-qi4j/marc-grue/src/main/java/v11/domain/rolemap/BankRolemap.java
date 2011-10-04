package v11.domain.rolemap;

import v11.domain.context.PayBillsContext;
import v11.domain.context.TransferMoneyContext;
import v11.domain.entity.BankEntity;

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
