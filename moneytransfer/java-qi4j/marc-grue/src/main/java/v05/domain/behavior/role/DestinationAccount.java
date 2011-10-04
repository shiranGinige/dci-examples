package v05.domain.behavior.role;

import v05.api.Role;
import v05.api.RoleMixin;
import v05.domain.structure.data.BalanceData;
import org.qi4j.api.mixin.Mixins;

/**
 * Destination account Role
 * - contract with the Entity of what Data the Role can use
 * - Only Data defined in Entity is available as an option
 */
@Mixins( DestinationAccount.Mixin.class )
public interface DestinationAccount
      extends Role, BalanceData
{
   // Interaction
   void receive( Integer amount );

   abstract class Mixin
         extends RoleMixin
         implements DestinationAccount
   {
      public void receive( Integer amount )
      {
         increaseBalance( amount );
         System.out.println( "Destination account +" + amount );
      }
   }
}
