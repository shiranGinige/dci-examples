package v3.domain.context;

import org.qi4j.api.mixin.Mixins;
import v3.api.Context;
import v3.api.Contexts;
import v3.api.ThisContext;
import v3.domain.data.ActivityData;
import v3.domain.data.DependencyData;
import v3.domain.data.ProjectData;

import java.util.List;

/**
 * Frontloading example with Qi4j - v3
 *
 * Without logging and comments
 */

public class FrontloadContextClean
      extends Context
{
   private ProjectData data;

   private FrontloaderRole Frontloader;
   private List<ActivityData> AllActivities;
   private ActivityData Activity;
   private List<DependencyData> Predecessors;

   public FrontloadContextClean( ProjectData projectData ) throws Exception
   {
      data = projectData;
   }

   public void frontloadNetworkFrom( final Integer projectStart ) throws Exception
   {
      Contexts.withContext( this, new Contexts.Command<FrontloadContextClean, Exception>()
      {
         public void command( FrontloadContextClean frontloadContext ) throws Exception
         {
            Frontloader.frontloadFrom( projectStart );
         }
      } );
   }

   public ActivityData Activity()
   {
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
            return activityCandidate;
      }

      return null;
   }

   public List<DependencyData> Predecessors()
   {
      if (Activity == null)
         return null;

      return ( (DependencyData) Activity ).getPredecessors();
   }

   public FrontloaderRole Frontloader()
   {
      return (FrontloaderRole) data;
   }

   public List<ActivityData> AllActivities()
   {
      return data.allActivities();
   }


   @Mixins( FrontloaderRole.Mixin.class )
   public interface FrontloaderRole
   {
      void frontloadFrom( Integer start ) throws Exception;

      class Mixin
            implements FrontloaderRole
      {
         @ThisContext
         FrontloadContextClean context;

         public void frontloadFrom( Integer projectStart ) throws Exception
         {
            for (ActivityData activity : context.AllActivities)
               activity.earlyStart().set( null );

            for (; context.Activity != null; context.reselectObjectsForRoles())
            {
               Integer activityStart = projectStart;
               for (DependencyData predecessor : context.Predecessors)
               {
                  Integer predecessorFinish = ( (ActivityData) predecessor ).earlyFinish().get();
                  if (predecessorFinish > activityStart)
                     activityStart = predecessorFinish;
               }
               context.Activity.earlyStart().set( activityStart );
            }
         }
      }
   }
}