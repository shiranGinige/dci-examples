package v06.domain.data;

import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;

import java.util.Date;

/**
 * Transactions - access to and build up of transactions
 */
@Mixins( Transactions.Mixin.class )
public interface Transactions
{
   ManyAssociation<Transaction> transactions();

   Transaction createTransaction( String text ) throws IllegalArgumentException;

   void completeTransaction();

   void addTransactionEntry( String accountId, Integer amount );

   Integer getBalance( String accountId );

   abstract class Mixin
         implements Transactions
   {
      @Service
      IdentityGenerator idGen;

      @Structure
      UnitOfWorkFactory uowf;

      // Temporary transaction holder. Only saved if entries balance upon transaction completion
      private Transaction tempTransaction;

      public Transaction createTransaction( String text ) throws IllegalArgumentException
      {
         if (text.length() < 3)
            throw new IllegalArgumentException( "Missing transaction text." );

         String id = idGen.generate( Identity.class );
         EntityBuilder<Transaction> transactionBuilder = uowf.currentUnitOfWork().newEntityBuilder( Transaction.class, id );
         transactionBuilder.instance().timeStamp().set( new Date() );
         transactionBuilder.instance().text().set( text );
         tempTransaction = transactionBuilder.newInstance();

         return tempTransaction;
      }

      public void addTransactionEntry( String accountId, Integer amount )
      {
         tempTransaction.createTransactionEntry( accountId, amount );
      }

      public void completeTransaction() throws IllegalStateException
      {
         if (tempTransaction == null)
            throw new IllegalStateException( "No transaction created. Please create transaction first." );
         if (tempTransaction.transactionEntries().count() == 0)
            throw new IllegalStateException( "No entries in transaction." );

         Integer sum = 0;
         for (TransactionEntry entry : tempTransaction.transactionEntries())
         {
            sum += entry.amount().get();
         }

         if (sum != 0)
            throw new IllegalStateException( "Entries don't balance." );

         for (TransactionEntry entry : tempTransaction.transactionEntries())
         {
            entry.booked().set( true );
         }
         tempTransaction.booked().set( true );

         transactions().add( tempTransaction );

         tempTransaction = null;
      }

      // TODO probably a smarter way to calculate the balance... maybe a query on the TransactionEntries?
      public Integer getBalance( String accountId )
      {
         Integer sum = 0;
         for (Transaction transaction : transactions())
         {
            for (TransactionEntry entry : transaction.transactionEntries())
            {
               if (entry.accountId().get().equals( accountId ))
                  sum += entry.amount().get();
            }
         }
         return sum;
      }
   }
}
