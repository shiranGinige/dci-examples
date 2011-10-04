package v02.domain.data;

import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;

/**
 * Interface and mixin implementation to:
 * - do balance data manipulation
 * - enforce basic "dumb" business rules on balance data
 */
@Mixins( AccountData.Mixin.class )
public interface AccountData
{
   @UseDefaults
   Property<Integer> balance();

   void increaseBalance( Integer amount );

   void decreaseBalance( Integer amount ) throws RuntimeException;

   abstract class Mixin
         implements AccountData
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
   }
}