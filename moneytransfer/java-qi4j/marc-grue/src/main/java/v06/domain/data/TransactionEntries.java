package v06.domain.data;

import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;

/**
 * Transaction entries - holds a reference to the "owning" transaction
 */
@Mixins(TransactionEntries.Mixin.class)
public interface TransactionEntries
{
   ManyAssociation<TransactionEntry> transactionEntries();

   TransactionEntry createTransactionEntry(String accountId, Integer amount);

   abstract class Mixin
         implements TransactionEntries
   {
      @Service
      IdentityGenerator idGen;

      @Structure
      UnitOfWorkFactory uowf;

      @This
      Transaction transaction;

      public TransactionEntry createTransactionEntry(String accountId, Integer amount)
      {
         String entryId = idGen.generate( Identity.class );

         EntityBuilder<TransactionEntry> builder = uowf.currentUnitOfWork().newEntityBuilder( TransactionEntry.class, entryId );
         builder.instance().transaction().set( transaction );
         builder.instance().accountId().set( accountId );
         builder.instance().amount().set( amount );

         TransactionEntry entry = builder.newInstance();

         // Not booked yet...
         transactionEntries().add(entry);

         return entry;
      }
   }
}
