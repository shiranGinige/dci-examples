package v01.behavior.role;

import v01.domain.data.SavingsAccountData;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.composite.TransientComposite;

/**
 * Money source role
 */
@Mixins( MoneySourceRole.Mixin.class )
public interface MoneySourceRole
      extends TransientComposite,
      SavingsAccountData
{
   // Interaction
   void transfer( Integer amount, MoneySinkRole moneySinkRole );

   public abstract class Mixin
         implements MoneySourceRole
   {
      public void transfer( Integer amount, MoneySinkRole moneySinkRole )
      {
         decreaseBalance( amount ); // (Savings)AccountData method
         moneySinkRole.transferFrom( amount );
      }
   }
}