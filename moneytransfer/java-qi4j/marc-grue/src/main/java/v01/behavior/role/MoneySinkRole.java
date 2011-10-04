package v01.behavior.role;

import v01.domain.data.CheckingAccountData;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.composite.TransientComposite;

/**
 * Money sink role
 */
@Mixins( MoneySinkRole.Mixin.class )
public interface MoneySinkRole
      extends TransientComposite, CheckingAccountData
{
   // Interaction
   void transferFrom( Integer amount );

   abstract class Mixin
         implements MoneySinkRole
   {
      public void transferFrom( Integer amount )
      {
         increaseBalance( amount );
      }
   }
}