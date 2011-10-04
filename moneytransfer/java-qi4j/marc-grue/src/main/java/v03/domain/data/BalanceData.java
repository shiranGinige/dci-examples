package v03.domain.data;

import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;

/**
 * Interface and mixin implementation to:
 * - do balance data manipulation
 * - enforce basic "dumb" business rules on balance data
 */
@Mixins( BalanceData.Mixin.class )
public interface BalanceData
{
   @UseDefaults
   Property<Integer> balance();

   void increaseBalance( Integer amount );

   void decreaseBalance( Integer amount ) throws RuntimeException;

   Integer getBalance();

   abstract class Mixin
         implements BalanceData
   {
      public void increaseBalance( Integer amount )
      {
         if (amount > 0)
         {
            balance().set( balance().get() + amount );
         }
      }

      public void decreaseBalance( Integer amount ) throws RuntimeException
      {
         if (amount > 0 && balance().get() >= amount)
         {
            balance().set( balance().get() - amount );
         }
      }

      public Integer getBalance()
      {
         return balance().get();
      }
   }
}