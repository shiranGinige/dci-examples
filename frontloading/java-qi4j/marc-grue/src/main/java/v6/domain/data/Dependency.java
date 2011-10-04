package v6.domain.data;

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;

import java.util.ArrayList;
import java.util.List;

import static org.qi4j.api.entity.EntityReference.getEntityReference;

/**
 * Data for dependency relationships between activities
 *
 * - An activity can have many predecessors
 * - An activity can have only one successor
 */
@Mixins( Dependency.Mixin.class )
public interface Dependency
{
   void succeededBy( Dependency... successors ) throws Exception;

   List<Dependency> getPredecessors();
   List<Dependency> getSuccessors();
   Dependency getSuccessor();

   void setSuccessor( Dependency successor ) throws Exception;
   void addPredecessor( Dependency predecessor ) throws Exception;

   interface Data
   {
      ManyAssociation<Dependency> predecessors();

      @Optional
      Association<Dependency> successor();
   }

   abstract class Mixin
         implements Dependency, Data
   {
      @This
      Dependency thisActivity;

      public void succeededBy( Dependency... successors ) throws Exception
      {
         // move those to succeededByOne??
         if (successors.length == 0)
            throw new Exception( "Please add a comma-separated list of succeeding activity objects" );

         if (successor().get() != null)
            throw new Exception( "'" + getEntityReference( thisActivity ).identity() + "' is already planned" );

         try
         {
            // Add predecessors of this activity and this activity itself
            List<Dependency> accumulatedPredecessors = new ArrayList<Dependency>();
            for (Dependency predecessor : predecessors())
               accumulatedPredecessors.add( predecessor );
            accumulatedPredecessors.add( thisActivity );

            // Set dependencies on successors
            Dependency currentActivity = thisActivity;
            for (Dependency successor : successors)
            {
               currentActivity.setSuccessor( successor );

               for (Dependency predecessor : accumulatedPredecessors)
               {
                  successor.addPredecessor( predecessor );
               }

               // Predecessors to next activity
               accumulatedPredecessors.add( successor );

               // Move to next activity
               currentActivity = successor;
            }
         }
         catch (Exception e)
         {
            // We could roll back to a valid state here...
            throw new Exception(e);
         }
      }

      public void addPredecessor( Dependency predecessorCandidate ) throws Exception
      {
         predecessors().add( predecessorCandidate );
      }

      public void setSuccessor( Dependency successorCandidate ) throws Exception
      {
         // Avoid cyclic dependency
         for (Dependency successor : successorCandidate.getSuccessors())
            if (thisActivity.equals( successor ))
               throw new Exception( "Cyclic dependency: '" + getEntityReference( thisActivity ).identity()
                     + "' is currently a successor to '" + getEntityReference( successorCandidate ).identity() + "'" );

         successor().set( successorCandidate );
      }

      public List<Dependency> getPredecessors()
      {
         return predecessors().toList();
      }

      public Dependency getSuccessor()
      {
         return successor().get();
      }

      public List<Dependency> getSuccessors()
      {
         // Traverse to get chain of successors
         List<Dependency> successors = new ArrayList<Dependency>();
         Dependency currentActivity = thisActivity;
         Dependency successor = null;
         while (( successor = currentActivity.getSuccessor() ) != null)
         {
            successors.add( successor );
            currentActivity = successor;
         }

         return successors;
      }
   }
}
