package v1.domain.context;

import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import v1.api.Contexts;
import v1.api.Context;
import v1.domain.data.ActivityData;
import v1.domain.data.DependencyData;
import v1.domain.data.ProjectData;

import static infrastructure.TemporaryHelper.log;

/**
 * Frontloading example with Qi4j - v1
 *
 * - For less clutter: see FrontloadContextClean
 *
 * How to find the earliest start of all activities in an activity network for a project
 *
 * Preconditions / definitions:
 * - A Project has Activities
 * - An Activity has a start time, a duration and an end time
 * - An Activity A can be succeeded by an Activity B. A becomes a "predecessor" to B, and B a "successor" to A.
 * - An Activity can have one successor
 * - An Activity can have many predecessors
 * - An Activity can only start when all its predecessors have finished
 * - Frontloading is the process of finding the earliest start time for an activity
 * - When an activity has a start and end time it has been planned
 * - When all activities have been planned, the frontloading process is completed
 *
 * Use case algorithm:
 * 1. Find all Activities of a Project
 * 2. Reset start and end times of all Activities
 * 3. Traverse all Activities
 * 4. Find Predecessors of current Activity in the iteration
 * 5. Check if all of those Predecessors are planned
 * 6a. YES: Plan this activity - set start time to latest Predecessor finish time
 * 6b. NO:  Go to next Activity
 * 7. Exit traversal when all Activities are planned (we might loop existing Activities several times)
 *
 *
 * This example only has 2 Roles in the FrontLoadContext since the Data model operates with a unified entity
 * composite containing both Activity and Dependency Data (Trygves example has separated those and therefore
 * utilizes separate Roles - "Predecessors" (= dependencies here) and Activity.
 */

public class FrontLoadContext
{
   private FrontLoaderRole frontloader;
   private TraversedActivityRole traversedActivity;

   public FrontLoadContext( ProjectData projectData )
   {
      frontloader = (FrontLoaderRole) projectData;
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


   @Mixins( FrontLoaderRole.Mixin.class )
   public interface FrontLoaderRole
   {
      void frontloadFrom( Integer start );

      class Mixin
            implements FrontLoaderRole
      {
         @Context
         FrontLoadContext context;

         @This
         ProjectData project;

         public void frontloadFrom( Integer projectStart )
         {
            // Set all activities in project to unplanned (no start/finish)
            resetAllActivities();

            log( "\n### Execution flow:" );

            // Traverse Activities until all are planned
            while (unplannedActivitiesExist())
            {
               for (ActivityData activity : project.allActivities())
               {
                  log( "\n   for " + activity.toString() + ": " );

                  // Current Activity now plays the TraversedActivityRole
                  context.traversedActivity = (TraversedActivityRole) activity;

                  if (context.traversedActivity.hasOnlyPlannedPredecessors())
                  {
                     context.traversedActivity.computeEarlyStartFrom( projectStart );
                  }
               }
            }
         }

         private void resetAllActivities()
         {
            for (ActivityData activity : project.allActivities())
            {
               activity.earlyStart().set( null );
            }
         }

         private boolean unplannedActivitiesExist()
         {
            for (ActivityData activity : project.allActivities())
            {
               if (activity.earlyStart().get() == null)
               {
                  log( "\nwhile " + activity.toString() + " is unplanned" );
                  return true;
               }
            }
            return false;
         }
      }
   }


   @Mixins( TraversedActivityRole.Mixin.class )
   public interface TraversedActivityRole
   {
      boolean hasOnlyPlannedPredecessors();

      void computeEarlyStartFrom( Integer projectStart );

      class Mixin
            implements TraversedActivityRole
      {
         @This
         DependencyData dependencies;

         @This
         ActivityData activity;

         public boolean hasOnlyPlannedPredecessors()
         {
            for (DependencyData predecessor : dependencies.getPredecessors())
            {
               if (( (ActivityData) predecessor ).earlyStart().get() == null)
               {
                  log( "predecessor " + predecessor.toString() + " is unplanned" );
                  return false;
               }
            }
            return true;
         }

         public void computeEarlyStartFrom( Integer projectStart )
         {
            if (activity.earlyStart().get() != null)
               return;

            activity.earlyStart().set( projectStart );

            for (DependencyData predecessor : dependencies.getPredecessors())
            {

               Integer activityStart = activity.earlyStart().get();
               Integer predecessorFinish = ( (ActivityData) predecessor ).earlyFinish().get();

               if (predecessorFinish > activityStart)
               {
                  activity.earlyStart().set( predecessorFinish );
               }
            }

            log( " -> plan " + activity.toString() );
         }
      }
   }
}