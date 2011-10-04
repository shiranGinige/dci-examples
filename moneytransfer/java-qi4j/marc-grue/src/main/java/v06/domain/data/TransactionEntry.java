package v06.domain.data;

import org.qi4j.api.property.Immutable;
import org.qi4j.api.property.Property;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.common.UseDefaults;

/**
 * Immutable transaction entry - I didn't find a way to make booked immutable once the entry has been booked...
 */
public interface TransactionEntry
{
   @Immutable
   Association<Transaction> transaction();

   @Immutable
   Property<String> accountId();

   @Immutable
   Property<Integer> amount();

   @UseDefaults
   Property<Boolean> booked();
}
