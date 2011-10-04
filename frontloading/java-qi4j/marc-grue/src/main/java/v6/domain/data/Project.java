package v6.domain.data;

import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.mixin.Mixins;
import v6.domain.entity.ActivityEntity;

import java.util.List;

/**
 * Data for a project with activities
 *
 * (For simplicity we use the entity id as a name for the project)
 */
@Mixins( Project.Mixin.class)
public interface Project
{
   void addActivities( ActivityEntity... activities );

   List<Activity> allActivities();

   interface Data
   {
      ManyAssociation<Activity> activities();
   }

   abstract class Mixin
         implements Project, Data
   {
      public void addActivities( ActivityEntity... activities )
      {
         for (Activity activity : activities)
         {
            activities().add( activity );
         }
      }

      public List<Activity> allActivities( )
      {
         return activities().toList();
      }
   }
}
