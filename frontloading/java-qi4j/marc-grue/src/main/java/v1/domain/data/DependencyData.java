package v1.domain.data;

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
 */
@Mixins( DependencyData.Mixin.class )
public interface DependencyData
{
   void succeededBy( DependencyData successor ) throws Exception;
   void addPredecessor( DependencyData predecessor ) throws Exception;

   DependencyData getSuccessor();
   List<DependencyData> getSuccessors();
   List<DependencyData> getPredecessors();

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

      public void succeededBy( DependencyData successor ) throws Exception
      {
         if (successor().get() != null)
            throw new Exception( "'" + getEntityReference( thisActivity ).identity() + "' is already planned" );

         // Successor has this activity as a predecessor (an activity can have many predecessors)
         successor.addPredecessor( thisActivity );

         // Avoid cyclic dependency
         for (DependencyData checkedSuccessor : successor.getSuccessors())
            if (thisActivity.equals( checkedSuccessor ))
               throw new Exception( "Cyclic dependency: '" + getEntityReference( thisActivity ).identity()
                       + "' is currently a successor to '" + getEntityReference( successor ).identity() + "'" );

         successor().set( successor );
      }

      public void addPredecessor( DependencyData predecessorCandidate ) throws Exception
      {
         predecessors().add( predecessorCandidate );
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

      public DependencyData getSuccessor()
      {
         return successor().get();
      }

      public List<DependencyData> getPredecessors()
      {
         return predecessors().toList();
      }
   }
}
