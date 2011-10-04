package v11.domain.data;

import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.mixin.Mixins;

import static org.qi4j.api.entity.EntityReference.getEntityReference;

/**
 * Javadoc
 */
@Mixins( Bank.Mixin.class )
public interface Bank
{
   ManyAssociation<Balance> accounts();
   ManyAssociation<Balance> creditors();
   

   public Balance getAccount( String accountId );


   abstract class Mixin
         implements Bank
   {
      public Balance getAccount( String accountId )
      {
         for (Balance account : accounts())
         {
            if (getEntityReference( account ).toString().equals(accountId))
               return account;
         }
         return null;
//         throw new Exception("Couldn't find account '" + accountId + "'");
      }
   }
}
