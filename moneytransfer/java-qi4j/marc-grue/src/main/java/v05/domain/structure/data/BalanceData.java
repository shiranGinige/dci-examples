package v05.domain.structure.data;

import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;

/**
 * Javadoc
 */
@Mixins( BalanceData.Mixin.class )
public interface BalanceData
{
   void increaseBalance( Integer amount ) throws IllegalArgumentException;
   void decreaseBalance( Integer amount ) throws IllegalArgumentException;
   Integer getBalance();

   @UseDefaults
   Property<Integer> allowedMinimum();

   interface Data
   {
      @UseDefaults
      Property<Integer> balance();
   }

   abstract class Mixin
         implements BalanceData, Data
   {
      public void increaseBalance( Integer amount ) throws IllegalArgumentException
      {
         if (amount < 0)
            throw new IllegalArgumentException( "Amount is expected to be a positive number" );

         balance().set( balance().get() + amount );
      }

      public void decreaseBalance( Integer amount ) throws IllegalArgumentException
      {
         if (amount < 0)
            throw new IllegalArgumentException( "Amount is expected to be a positive number" );

         // basic data validation
         if (balance().get().equals( allowedMinimum().get() ))
            throw new IllegalArgumentException( "Credit limit is reached." );
         if (( balance().get() - amount ) < allowedMinimum().get())
            throw new IllegalArgumentException( "Insufficient funds. Available funds: " + allowedMinimum().get() + balance().get() );

         balance().set( balance().get() - amount );
      }

      public Integer getBalance()
      {
         return balance().get();
      }
   }
}
