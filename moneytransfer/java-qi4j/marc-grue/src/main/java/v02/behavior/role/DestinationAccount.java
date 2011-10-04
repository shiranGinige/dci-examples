package v02.behavior.role;

import v02.domain.data.CheckingAccountData;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.injection.scope.This;

/**
 * Destination account Role
 * - contract with the Entity of what Data the Role can use
 * - Only Data defined in Entity is available as an option
 */
@Mixins( DestinationAccount.Mixin.class )
public interface DestinationAccount
      extends TransientComposite
{
   void receive( Integer amount );

   // Allowed Data access (for SourceAccount Role)
   Integer getBalance();

   abstract class Mixin
         implements DestinationAccount
   {
      @This
      CheckingAccountData data;

      public void receive( Integer amount )
      {
         data.increaseBalance( amount );
         System.out.println( "Destination account +" + amount );
      }

      public Integer getBalance()
      {
         return data.balance().get();
      }
   }
}