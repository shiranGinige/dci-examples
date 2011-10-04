package v5.domain.context;

import org.qi4j.api.mixin.Mixins;
import v5.api.Contexts;
import v5.api.ThisContext;
import v5.domain.data.ActivityData;
import v5.domain.data.DependencyData;
import v5.domain.data.ProjectData;

import java.util.List;

import static infrastructure.TemporaryHelper.log;

/**
 * Frontloading example with Qi4j - v5
 *
 * - For less clutter: see FrontloadContextClean
 *
 * - Static Role binding in constructor of those (methodless) Roles that only need to be bound to a Data object once.
 * - Dynamic Role binding in method with intuitive Use Case related name.
 */
public class FrontloadContext
{
   private FrontloaderRole frontloader;
   private List<ActivityData> allActivities;
   private ActivityData activity;
   private List<DependencyData> predecessors;

   public FrontloadContext( ProjectData projectData )
   {
      // Static Role binding
      frontloader = (FrontloaderRole) projectData;
      allActivities = projectData.allActivities();
   }

   public void frontloadNetworkFrom( final Integer projectStart )
         throws IllegalArgumentException
   {
      // Context stack handling (will be simplified with next Qi4j version)
      Contexts.withContext( this, new Contexts.Command<FrontloadContext, IllegalArgumentException>()
      {
         public void command( FrontloadContext frontloadContext ) throws IllegalArgumentException
         {
            // Interactions trigger
            frontloader.frontloadFrom( projectStart );
         }
      } );
   }

   // Dynamic Role binding
   public boolean canFindUnplannedActivity()
   {
      log( "\nReselect" );

      activity = null;
      for (ActivityData activityCandidate : allActivities)
      {
         log( "\n   " + activityCandidate );

         // Get the early start time from the Activity (the ActivityData part of the ActivityEntity)
         if (activityCandidate.earlyStart().get() != null)
         {
            log( " is planned" );
            continue;
         }

         // The Activity Role can be casted to either DependencyData or ActivityData (see ActivityEntity)
         // To find its predecessors we cast it to DependencyData
         DependencyData activityAsDependencyData = (DependencyData) activityCandidate;
         Boolean allPredecessorsPlanned = true;
         for (DependencyData predecessor : activityAsDependencyData.getPredecessors())
         {
            // We need the start time of the Predecessor (available when casted as ActivityData)
            ActivityData predecessorAsActivityData = (ActivityData) predecessor;
            if (predecessorAsActivityData.earlyStart().get() == null)
            {
               allPredecessorsPlanned = false;
               break;
            }
         }

         // Activity can be planned if all Predecessors are already planned - leave the loop and plan the activity
         if (allPredecessorsPlanned)
         {
            activity = activityCandidate;
            break;
         }

         // This activity couldn't be planned - check next activity in the loop
         log( " has unplanned predecessors" );
      }

      // We didn't find any Activity that needs to get planned - we're done!
      if (activity == null)
         return false;

      // Same casting procedure as above to get the Predecessors of the Activity (as DependencyData)
      predecessors = ( (DependencyData) activity ).getPredecessors();

      return true;
   }

   @Mixins( FrontloaderRole.Mixin.class )
   public interface FrontloaderRole
   {
      void frontloadFrom( Integer start );

      class Mixin
            implements FrontloaderRole
      {
         @ThisContext
         FrontloadContext context;

         public void frontloadFrom( Integer projectStart )
         {
            // Reset start time for all Activities
            for (ActivityData activity : context.allActivities)
               activity.earlyStart().set( null );

            // Loop dynamic Role binding until all Activities have been planned
            while (context.canFindUnplannedActivity())
            {
               Integer activityStart = projectStart;
               for (DependencyData predecessor : context.predecessors)
               {
                  // Get the finish time of the current Predecessor (as ActivityData)
                  Integer predecessorFinish = ( (ActivityData) predecessor ).earlyFinish().get();

                  // Up the Activity start time if this Predecessor has a later finish time
                  if (predecessorFinish > activityStart)
                     activityStart = predecessorFinish;
               }
               context.activity.earlyStart().set( activityStart );

               log( " -> early start = " + activityStart );
            }
         }
      }
   }
}