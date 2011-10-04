package v4.domain.context;

import org.qi4j.api.mixin.Mixins;
import v4.api.Context;
import v4.api.Contexts;
import v4.api.ThisContext;
import v4.domain.data.ActivityData;
import v4.domain.data.DependencyData;
import v4.domain.data.ProjectData;

import java.util.List;

import static infrastructure.TemporaryHelper.log;

/**
 * Frontloading example with Qi4j - v4   
 *
 * - For less clutter: see FrontloadContextClean
 *
 * - Unified Role binding in one method with generic name "reselectObjectsForRoles".
 */
public class FrontloadContext
      extends Context
{
   private ProjectData data;

   // Role players
   private FrontloaderRole frontloader;
   private List<ActivityData> allActivities;
   private ActivityData activity;
   private List<DependencyData> predecessors;

   public FrontloadContext( ProjectData projectData )
   {
      data = projectData;
   }

   public void frontloadNetworkFrom( final Integer projectStart )
         throws IllegalArgumentException
   {
      // Context stack handling (will be simplified with next Qi4j version)      
      Contexts.withContext( this, new Contexts.Command<FrontloadContext, IllegalArgumentException>()
      {
         public void command( FrontloadContext frontloadContext ) throws IllegalArgumentException
         {
            frontloader.frontloadFrom( projectStart );
         }
      } );
   }

   public void reselectObjectsForRoles()
   {
      log( "\nReselect" );

      frontloader = (FrontloaderRole) data;
      allActivities = data.allActivities();

      // Find an unplanned activity with only planned predecessors
      activity = null;
      for (ActivityData activityCandidate : data.allActivities())
      {
         log( "\n   " + activityCandidate );

         // Try next activity if this one is already planned
         if (activityCandidate.earlyStart().get() != null)
         {
            log( " is planned" );
            continue;
         }

         Boolean allPredecessorsPlanned = true;
         for (DependencyData predecessor : ( (DependencyData) activityCandidate ).getPredecessors())
         {
            if (( (ActivityData) predecessor ).earlyStart().get() == null)
            {
               allPredecessorsPlanned = false;
               break;
            }
         }
         if (allPredecessorsPlanned)
         {
            activity = activityCandidate;
            break;
         }

         log( " has unplanned predecessors" );
      }

      if (activity == null)
         return;

      predecessors = ( (DependencyData) activity ).getPredecessors();
   }

   // Methodful Role

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
            // Reset all Activities
            for (ActivityData activity : context.allActivities)
               activity.earlyStart().set( null );

            // Traverse Activities until all are planned
            for (; context.activity != null; context.reselectObjectsForRoles())
            {
               Integer activityStart = projectStart;
               for (DependencyData predecessor : context.predecessors)
               {
                  Integer predecessorFinish = ( (ActivityData) predecessor ).earlyFinish().get();
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