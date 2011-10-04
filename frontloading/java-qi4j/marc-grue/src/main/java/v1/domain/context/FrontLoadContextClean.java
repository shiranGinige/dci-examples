package v1.domain.context;

import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import v1.api.Context;
import v1.api.Contexts;
import v1.domain.data.ActivityData;
import v1.domain.data.DependencyData;
import v1.domain.data.ProjectData;

/**
 * Frontloading example with Qi4j - v1
 *
 * Without comments and logging
 */

public class FrontLoadContextClean
{
   private FrontLoaderRole frontloader;
   private TraversedActivityRole traversedActivity;

   public FrontLoadContextClean( ProjectData projectData )
   {
      frontloader = (FrontLoaderRole) projectData;
   }

   public void frontloadNetworkFrom( final Integer projectStart )
         throws IllegalArgumentException
   {
      Contexts.withContext( this, new Contexts.Command<FrontLoadContextClean, IllegalArgumentException>()
      {
         public void command( FrontLoadContextClean frontloadContext ) throws IllegalArgumentException
         {
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
         FrontLoadContextClean context;

         @This
         ProjectData project;

         public void frontloadFrom( Integer projectStart )
         {
            resetAllActivities();

            while (unplannedActivitiesExist())
            {
               for (ActivityData activity : project.allActivities())
               {
                  context.traversedActivity = (TraversedActivityRole) activity;

                  if (context.traversedActivity.hasOnlyPlannedPredecessors())
                     context.traversedActivity.computeEarlyStartFrom( projectStart );
               }
            }
         }

         private void resetAllActivities()
         {
            for (ActivityData activity : project.allActivities())
               activity.earlyStart().set( null );
         }

         private boolean unplannedActivitiesExist()
         {
            for (ActivityData activity : project.allActivities())
               if (activity.earlyStart().get() == null)
                  return true;

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
               if (( (ActivityData) predecessor ).earlyStart().get() == null)
                  return false;

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
                  activity.earlyStart().set( predecessorFinish );
            }
         }
      }
   }
}