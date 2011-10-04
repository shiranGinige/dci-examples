package v6.domain.context;

import org.qi4j.api.mixin.Mixins;
import v6.api.Contexts;
import v6.api.ThisContext;
import v6.domain.data.Activity;
import v6.domain.data.Dependency;
import v6.domain.data.Project;

import java.util.List;

/**
 * Frontloading example with Qi4j - v6
 *
 * Without logging and comments, and with a more compressed castings
 */
public class FrontloadContextClean
{
   private FrontloaderRole frontloader;
   private List<Activity> allActivities;
   private Activity activity;
   private List<Dependency> predecessors;

   public FrontloadContextClean( Project projectData )
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
      for (Activity activityCandidate : allActivities)
      {
         if (activityCandidate.earlyStart().get() != null)
            continue;

         Boolean allPredecessorsPlanned = true;
         for (Dependency predecessor : ((Dependency) activityCandidate).getPredecessors())
         {
            if (((Activity) predecessor).earlyStart().get() == null)
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

      predecessors = ( (Dependency) activity ).getPredecessors();

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
            for (Activity activity : context.allActivities)
               activity.earlyStart().set( null );

            while (context.canFindUnplannedActivity())
            {
               Integer activityStart = projectStart;
               for (Dependency predecessor : context.predecessors)
               {
                  Integer predecessorFinish = ( (Activity) predecessor ).earlyFinish().get();

                  if (predecessorFinish > activityStart)
                     activityStart = predecessorFinish;
               }
               context.activity.earlyStart().set( activityStart );
            }
         }
      }
   }
}