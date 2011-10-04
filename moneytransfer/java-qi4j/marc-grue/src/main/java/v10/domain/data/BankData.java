package v10.domain.data;

import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.mixin.Mixins;

import static org.qi4j.api.entity.EntityReference.getEntityReference;

/**
 * Javadoc
 */
@Mixins( BankData.Mixin.class )
public interface BankData
{
   ManyAssociation<BalanceData> accounts();
   ManyAssociation<BalanceData> creditors();
   

   public BalanceData getAccount( String accountId );


   abstract class Mixin
         implements BankData
   {
      public BalanceData getAccount( String accountId )
      {
         for (BalanceData account : accounts())
         {
            if (getEntityReference( account ).toString().equals(accountId))
               return account;
         }
         return null;
//         throw new Exception("Couldn't find account '" + accountId + "'");
      }
   }
}
