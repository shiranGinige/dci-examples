package v3.domain.data;

import org.qi4j.api.common.Optional;
import org.qi4j.api.injection.scope.State;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Computed;
import org.qi4j.api.property.ComputedPropertyInstance;
import org.qi4j.api.property.Property;
import org.qi4j.library.constraints.annotation.GreaterThan;

/**
 * Basic data for an activity
 *
 * Start/finish times are set by the frontloading process
 *
 * (For simplicity we use the entity id as a name for the activity)
 */
@Mixins( ActivityData.EarlyFinishMixin.class)
public interface ActivityData
{     
   @Optional
   @GreaterThan( 0 )
   Property<Integer> duration();

   // The earliest time an activity can start
   @Optional
   @GreaterThan( 0 )
   Property<Integer> earlyStart();

   // The earliest time an activity can end - is automatically computed by EarlyFinishMixin
   @Computed
   Property<Integer> earlyFinish();


   public static abstract class EarlyFinishMixin
        implements ActivityData
    {
        @This
        ActivityData data;

        @State
        Property<Integer> earlyFinish;

        public Property<Integer> earlyFinish()
        {
            return new ComputedPropertyInstance<Integer>( earlyFinish )
            {
                @Override
                public Integer get()
                {
                    return data.earlyStart().get() + data.duration().get();
                }
            };
        }
    }
}