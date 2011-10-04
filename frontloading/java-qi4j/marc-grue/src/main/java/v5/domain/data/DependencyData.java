package v5.domain.data;

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
@Mixins( DependencyData.Mixin.class )
public interface DependencyData
{
   void succeededBy( DependencyData... successors ) throws Exception;

   List<DependencyData> getPredecessors();
   List<DependencyData> getSuccessors();
   DependencyData getSuccessor();

   void setSuccessor( DependencyData successor ) throws Exception;
   void addPredecessor( DependencyData predecessor ) throws Exception;

   interface Data
   {
      ManyAssociation<DependencyData> predecessors();

      @Optional
      Association<DependencyData> successor();
   }

   abstract class Mixin
         implements DependencyData, Data
   {
      @This
      DependencyData thisActivity;

      public void succeededBy( DependencyData... successors ) throws Exception
      {
         // move those to succeededByOne??
         if (successors.length == 0)
            throw new Exception( "Please add a comma-separated list of succeeding activity objects" );

         if (successor().get() != null)
            throw new Exception( "'" + getEntityReference( thisActivity ).identity() + "' is already planned" );

         try
         {
            // Add predecessors of this activity and this activity itself
            List<DependencyData> accumulatedPredecessors = new ArrayList<DependencyData>();
            for (DependencyData predecessor : predecessors())
               accumulatedPredecessors.add( predecessor );
            accumulatedPredecessors.add( thisActivity );

            // Set dependencies on successors
            DependencyData currentActivity = thisActivity;
            for (DependencyData successor : successors)
            {
               currentActivity.setSuccessor( successor );

               for (DependencyData predecessor : accumulatedPredecessors)
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

      public void addPredecessor( DependencyData predecessorCandidate ) throws Exception
      {
         predecessors().add( predecessorCandidate );
      }

      public void setSuccessor( DependencyData successorCandidate ) throws Exception
      {
         // Avoid cyclic dependency
         for (DependencyData successor : successorCandidate.getSuccessors())
            if (thisActivity.equals( successor ))
               throw new Exception( "Cyclic dependency: '" + getEntityReference( thisActivity ).identity()
                     + "' is currently a successor to '" + getEntityReference( successorCandidate ).identity() + "'" );

         successor().set( successorCandidate );
      }

      public List<DependencyData> getPredecessors()
      {
         return predecessors().toList();
      }

      public DependencyData getSuccessor()
      {
         return successor().get();
      }

      public List<DependencyData> getSuccessors()
      {
         // Traverse to get chain of successors
         List<DependencyData> successors = new ArrayList<DependencyData>();
         DependencyData currentActivity = thisActivity;
         DependencyData successor = null;
         while (( successor = currentActivity.getSuccessor() ) != null)
         {
            successors.add( successor );
            currentActivity = successor;
         }

         return successors;
      }
   }
}
