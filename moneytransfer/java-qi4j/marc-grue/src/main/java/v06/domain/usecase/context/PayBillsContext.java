package v06.domain.usecase.context;

import v06.api.Context;
import v06.api.ContextMixin;
import v06.domain.data.Transactions;
import v06.domain.entity.AccountEntity;
import v06.domain.entity.TransactionsEntity;
import v06.domain.usecase.role.SourceAccountRole;
//import v6.domain.entity.CheckingAccountEntity;
import org.qi4j.api.mixin.Mixins;

/**
 * PayBills use case context
 */
@Mixins( PayBillsContext.Mixin.class )
public interface PayBillsContext
      extends Context
{
   void payBills( String sourceAccountId ) throws Exception;

   abstract class Mixin
         extends ContextMixin
         implements PayBillsContext
   {
      public void payBills( String sourceAccountId ) throws Exception
      {
         addRolePlayer( SourceAccountRole.class, AccountEntity.class, sourceAccountId );
         addRolePlayer( Transactions.class, TransactionsEntity.TRANSACTIONS );

         roleMap.get( SourceAccountRole.class).payBills( );
      }
   }
}