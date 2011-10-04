package v4.domain.context;

import org.qi4j.api.mixin.Mixins;
import v4.api.Context;
import v4.api.Contexts;
import v4.api.ThisContext;
import v4.domain.data.ActivityData;
import v4.domain.data.DependencyData;
import v4.domain.data.ProjectData;

import java.util.List;

/**
 * Frontloading example with Qi4j - v4
 *
 * Whitout comments and logging
 */
public class FrontloadContextClean
      extends Context
{
   private ProjectData data;

   // Role players
   private FrontloaderRole frontloader;
   private List<ActivityData> allActivities;
   private ActivityData activity;
   private List<DependencyData> predecessors;

   public FrontloadContextClean( ProjectData projectData )
   {
      data = projectData;
   }

   public void frontloadNetworkFrom( final Integer projectStart )
         throws IllegalArgumentException
   {
      Contexts.withContext( this, new Contexts.Command<FrontloadContextClean, IllegalArgumentException>()
      {
         public void command( FrontloadContextClean frontloadContext ) throws IllegalArgumentException
         {
            frontloader.frontloadFrom( projectStart );
         }
      } );
   }

   public void reselectObjectsForRoles()
   {
      frontloader = (FrontloaderRole) data;
      allActivities = data.allActivities();

      activity = null;
      for (ActivityData activityCandidate : data.allActivities())
      {
         if (activityCandidate.earlyStart().get() != null)
            continue;

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
      }

      if (activity == null)
         return;

      predecessors = ( (DependencyData) activity ).getPredecessors();
   }

   @Mixins( FrontloaderRole.Mixin.class )
   public interface FrontloaderRole
   {
      void frontloadFrom( Integer start );

      class Mixin
            implements FrontloaderRole
      {
         @ThisContext
         FrontloadContextClean context;

         public void frontloadFrom( Integer projectStart )
         {
            for (ActivityData activity : context.allActivities)
               activity.earlyStart().set( null );

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
            }
         }
      }
   }
}