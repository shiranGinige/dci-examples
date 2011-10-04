package v4.domain.data;

import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.mixin.Mixins;
import v4.domain.entity.ActivityEntity;

import java.util.List;

/**
 * Data for a project with activities
 *
 * (For simplicity we use the entity id as a name for the project)
 */
@Mixins(ProjectData.Mixin.class)
public interface ProjectData
{
   void addActivities( ActivityEntity... activities );

   List<ActivityData> allActivities();

   interface Data
   {
      ManyAssociation<ActivityData> activities();
   }

   abstract class Mixin
         implements ProjectData, Data
   {
      public void addActivities( ActivityEntity... activities )
      {
         for (ActivityData activity : activities)
         {
            activities().add( activity );
         }
      }

      public List<ActivityData> allActivities( )
      {
         return activities().toList();
      }
   }
}