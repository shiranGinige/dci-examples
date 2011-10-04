package v5.domain.context;

import org.qi4j.api.mixin.Mixins;
import v5.api.Contexts;
import v5.api.ThisContext;
import v5.domain.data.ActivityData;
import v5.domain.data.DependencyData;
import v5.domain.data.ProjectData;

import java.util.List;

/**
 * Frontloading example with Qi4j - v5
 *
 * Without logging and comments, and with a more compressed castings
 */
public class FrontloadContextClean
{
   private FrontloaderRole frontloader;
   private List<ActivityData> allActivities;
   private ActivityData activity;
   private List<DependencyData> predecessors;

   public FrontloadContextClean( ProjectData projectData )
   {
      frontloader = (FrontloaderRole) projectData;
      allActivities = projectData.allActivities();
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

   public boolean canFindUnplannedActivity()
   {
      activity = null;
      for (ActivityData activityCandidate : allActivities)
      {
         if (activityCandidate.earlyStart().get() != null)
            continue;

         Boolean allPredecessorsPlanned = true;
         for (DependencyData predecessor : ((DependencyData) activityCandidate).getPredecessors())
         {
            if (((ActivityData) predecessor).earlyStart().get() == null)
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
         return false;

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
         FrontloadContextClean context;

         public void frontloadFrom( Integer projectStart )
         {
            for (ActivityData activity : context.allActivities)
               activity.earlyStart().set( null );

            while (context.canFindUnplannedActivity())
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