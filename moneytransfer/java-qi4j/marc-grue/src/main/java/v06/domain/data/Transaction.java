package v06.domain.data;

import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.property.Immutable;
import org.qi4j.api.property.Property;

import java.util.Date;

public interface Transaction
   extends TransactionEntries
{
   @Immutable
   Property<Date> timeStamp();

   @UseDefaults
   Property<String> text();

   @UseDefaults
   Property<Boolean> booked();
}
