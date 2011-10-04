package v2.domain.context;

import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import v2.api.Contexts;
import v2.api.Context;
import v2.domain.data.ActivityData;
import v2.domain.data.DependencyData;
import v2.domain.data.ProjectData;

import java.util.List;

import static infrastructure.TemporaryHelper.log;

/**
 * Frontloading example with Qi4j - v2
 *
 * - For less clutter: see FrontloadContextClean
 *
 * - All Roles implemented as methodful Roles having their "own" methods (apart from the Data methods available).
 */

public class FrontLoadContext
{
   private FrontLoaderRole frontloader;
   private AllActivitiesRole allActivities;
   private ActivityRole activity;
   private PredecessorsRole predecessors;

   public FrontLoadContext( ProjectData projectData )
   {
      frontloader = (FrontLoaderRole) projectData;
      allActivities = (AllActivitiesRole) projectData;
   }

   public void frontloadNetworkFrom( final Integer projectStart )
         throws IllegalArgumentException
   {
      // Context stack handling (will be simplified with next Qi4j version)      
      Contexts.withContext( this, new Contexts.Command<FrontLoadContext, IllegalArgumentException>()
      {
         public void command( FrontLoadContext frontloadContext ) throws IllegalArgumentException
         {
            // Use case trigger
            frontloader.frontloadFrom( projectStart );
         }
      } );
   }

   public boolean canSelectUnplannedActivity()
   {
      activity = null;

      log( "\nCheck" );

      // Find an activity that is unplanned and has only planned predecessors
      for (ActivityData activityCandidate : allActivities.getAll())
      {
         log( "\n   " + activityCandidate );// + ": " );

         // Try next activity if this one is already planned
         if (activityCandidate.earlyStart().get() != null)
         {
            log( " is planned" );
            continue;
         }

         // Assign new PredecessorsRole
         predecessors = (PredecessorsRole) activityCandidate;

         if (predecessors.arePlanned())
         {
            log( " -> plan " );// + activityCandidate );

            // Assign new ActivityRole
            activity = (ActivityRole) activityCandidate;
            return true;
         }
      }

      // No more activities left to plan
      return false;
   }


   @Mixins( FrontLoaderRole.Mixin.class )
   public interface FrontLoaderRole
   {
      void frontloadFrom( Integer start );

      class Mixin
            implements FrontLoaderRole
      {
         @Context
         FrontLoadContext context;

         // (This Role needs no Data)

         public void frontloadFrom( Integer projectStart )
         {
            // Set all activities in project to unplanned (no start/finish)
            context.allActivities.unplanAll();

            log( "\n### Execution flow:" );

            // Traverse Activities until all are planned
            while (context.canSelectUnplannedActivity())
            {
               Integer activityStart = projectStart;
               for (DependencyData predecessor : context.predecessors.get())
               {
                  Integer predecessorFinish = ( (ActivityData) predecessor ).earlyFinish().get();
                  if (predecessorFinish > activityStart)
                     activityStart = predecessorFinish;
               }
               context.activity.setEarlyStart( activityStart );
            }
         }
      }
   }

   @Mixins( ActivityRole.Mixin.class )
   public interface ActivityRole
   {
      void setEarlyStart( Integer projectStart );

      class Mixin
            implements ActivityRole
      {
         @This
         ActivityData activity;

         public void setEarlyStart( Integer projectStart )
         {
            activity.earlyStart().set( projectStart );
         }
      }
   }

   @Mixins( PredecessorsRole.Mixin.class )
   public interface PredecessorsRole
   {
      List<DependencyData> get();
      boolean arePlanned();

      class Mixin
            implements PredecessorsRole
      {
         @This
         DependencyData dependencies;

         public List<DependencyData> get()
         {
            return dependencies.getPredecessors();
         }

         public boolean arePlanned()
         {
            for (DependencyData predecessor : dependencies.getPredecessors())
            {
               if (( (ActivityData) predecessor ).earlyStart().get() == null)
               {
                  log( ": predecessor " + predecessor.toString() + " unplanned" );
                  return false;
               }
            }
            return true;
         }
      }
   }

   @Mixins( AllActivitiesRole.Mixin.class )
   public interface AllActivitiesRole
   {
      void unplanAll();

      List<ActivityData> getAll();

      class Mixin
            implements AllActivitiesRole
      {
         @This
         ProjectData project;

         public void unplanAll()
         {
            for (ActivityData activity : project.allActivities())
            {
               activity.earlyStart().set( null );
            }
         }

         public List<ActivityData> getAll()
         {
            return project.allActivities();
         }
      }
   }
}