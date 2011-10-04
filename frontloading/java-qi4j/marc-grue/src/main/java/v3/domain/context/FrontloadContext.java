package v3.domain.context;

import org.qi4j.api.mixin.Mixins;
import v3.api.Context;
import v3.api.Contexts;
import v3.api.ThisContext;
import v3.domain.data.ActivityData;
import v3.domain.data.DependencyData;
import v3.domain.data.ProjectData;

import java.util.List;

import static infrastructure.TemporaryHelper.log;

/**
 * Frontloading example with Qi4j - v3
 *
 * - For less clutter: see FrontloadContextClean
 *
 * The closest imitation of Trygves SmallTalk implementation in:
 *
 * @See http://heim.ifi.uio.no/~trygver/2009/bb4plan.pdf
 *
 * See also excellent SmallTalk analysis from Brian (candlerb) in order to understand Trygves example:
 * @See http://groups.google.com/group/object-composition/browse_thread/thread/854df3a328e1c263/5f6985a4cebf86b5
 *
 * - Context base class introduced
 * - Each Role has a Role binding method for assigning a Data Object.
 * - Observe that Role fields are reselected in the given field order! (Predecessors has to come after Activity!)
 * - Methodless Roles are here basically references to Data Objects.
 *
 * - OBSERVE: Role identifiers are with capitalized first letter as an experiment in order to see if
 * it helps readability to distinguish those "Role fields" from "data fields" of the Context. It's not
 * conventional java, so it's probably not a good idea.
 */

public class FrontloadContext
      extends Context
{
   private ProjectData data;

   // Methodful Role
   public FrontloaderRole Frontloader;

   // Methodless Roles
   public List<ActivityData> AllActivities;
   public ActivityData Activity;
   public List<DependencyData> Predecessors;

   public FrontloadContext( ProjectData projectData ) throws Exception
   {
      data = projectData;
   }

   public void frontloadNetworkFrom( final Integer projectStart ) throws Exception
   {
      // Context stack handling (will be simplified with next Qi4j version)      
      Contexts.withContext( this, new Contexts.Command<FrontloadContext, Exception>()
      {
         public void command( FrontloadContext frontloadContext ) throws Exception
         {
            Frontloader.frontloadFrom( projectStart );
         }
      } );
   }

   // Role binding methods for each Role (called by reselectObjectsForRoles in Context base class)

   public ActivityData Activity()
   {
      log( "\nReselect" );

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
            return activityCandidate;

         log( " has unplanned predecessors" );
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


   // Methodful Role

   @Mixins( FrontloaderRole.Mixin.class )
   public interface FrontloaderRole
   {
      void frontloadFrom( Integer start ) throws Exception;

      class Mixin
            implements FrontloaderRole
      {
         @ThisContext
         FrontloadContext context;

         public void frontloadFrom( Integer projectStart ) throws Exception
         {
            // Set all activities in project to unplanned (no start/finish)
            for (ActivityData activity : context.AllActivities)
               activity.earlyStart().set( null );

            // Traverse Activities until all are planned
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

               log( " -> early start = " + activityStart );
            }
         }
      }
   }
}