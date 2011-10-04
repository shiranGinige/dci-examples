package v2.domain.context;

import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import v2.api.Context;
import v2.api.Contexts;
import v2.domain.data.ActivityData;
import v2.domain.data.DependencyData;
import v2.domain.data.ProjectData;

import java.util.List;

/**
 * Frontloading example with Qi4j - v2
 *
 * Without comments and logging
 */

public class FrontLoadContextClean
{
   private FrontLoaderRole frontloader;
   private AllActivitiesRole allActivities;
   private ActivityRole activity;
   private PredecessorsRole predecessors;

   public FrontLoadContextClean( ProjectData projectData )
   {
      frontloader = (FrontLoaderRole) projectData;
      allActivities = (AllActivitiesRole) projectData;
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

   public boolean canSelectUnplannedActivity()
   {
      activity = null;

      for (ActivityData activityCandidate : allActivities.getAll())
      {
         if (activityCandidate.earlyStart().get() != null)
            continue;

         predecessors = (PredecessorsRole) activityCandidate;

         if (predecessors.arePlanned())
         {
            activity = (ActivityRole) activityCandidate;
            return true;
         }
      }

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
         FrontLoadContextClean context;

         public void frontloadFrom( Integer projectStart )
         {
            context.allActivities.unplanAll();

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
               if (( (ActivityData) predecessor ).earlyStart().get() == null)
                  return false;
            
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
               activity.earlyStart().set( null );
         }

         public List<ActivityData> getAll()
         {
            return project.allActivities();
         }
      }
   }
}