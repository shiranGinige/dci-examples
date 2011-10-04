package v06.domain.data;

import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.property.Property;

/**
 * Account data
 * - Could hold reference to account holder, account type etc.
 */
public interface Account
{
   @UseDefaults
   Property<Integer> allowedMinimum();
}
