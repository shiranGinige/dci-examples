package v07.domain.data;

import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.library.constraints.annotation.GreaterThan;

/**
 * Maintain a balance for an account, of any type
 */
@Mixins(BalanceData.Mixin.class)
public interface BalanceData
{
   void increasedBalance( @GreaterThan(0) Integer amount );

   void decreasedBalance( @GreaterThan(0) Integer amount );

   Integer getBalance();

   // Default implementation

   interface Data
   {
      @UseDefaults
      Property<Integer> balance();
   }

   abstract class Mixin
         implements BalanceData
   {
      @This
      Data data;

      public void increasedBalance( Integer amount )
      {
         data.balance().set( data.balance().get() + amount );
      }

      public void decreasedBalance( Integer amount )
      {
         data.balance().set( data.balance().get() - amount );
      }

      public Integer getBalance()
      {
         return data.balance().get();
      }
   }
}
