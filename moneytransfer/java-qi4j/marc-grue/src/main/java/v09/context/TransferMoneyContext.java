/*
 * Copyright (c) 2010, Rickard Öberg. All Rights Reserved.
 *
 * MODIFIED by Marc Grue
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package v09.context;

import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import v09.api.Context;
import v09.api.Contexts;
import v09.domain.data.BalanceData;

import static v09.api.Contexts.log;


/**
 * Context for transfer of money between two accounts. Roles are defined within the context,
 * and only the entities know about them outside of this context.
 */
public class TransferMoneyContext
{
   // Object->Role mappings
   private SourceAccountRole sourceAccount;
   private DestinationAccountRole destinationAccount;

   // Context setup
   // Objects are specified using their data interface, and then cast to the role interfaces
   public TransferMoneyContext bind( BalanceData sourceData, BalanceData destinationData )
   {
      sourceAccount = (SourceAccountRole) sourceData;
      destinationAccount = (DestinationAccountRole) destinationData;
      return this; // for fluent interface
   }

   // Interactions

   public Integer availableFunds()
   {
      // (The next 7 lines will become a 1-liner (line 5) when Qi4j implements Concerns/Advices on POJOs)
      return Contexts.withContext( this, new Contexts.Query<Integer, TransferMoneyContext, RuntimeException>()
      {
         public Integer query( TransferMoneyContext transferMoneyContext ) throws RuntimeException
         {
            return sourceAccount.availableFunds();
         }
      } );
   }

   public void transfer( final Integer amount )
         throws IllegalArgumentException
   {
      Contexts.withContext( this, new Contexts.Command<TransferMoneyContext, IllegalArgumentException>()
      {
         public void command( TransferMoneyContext transferMoneyContext ) throws IllegalArgumentException
         {
            sourceAccount.transfer( amount );
         }
      } );
   }

   // More interactions could go here...


   /**
    * The SourceAccountRole orchestrates the Transfer Money use case interactions.
    *
    * Code matches the use case text carefully (see references below).
    *
    * Transfer Money use case Scenario:
    *
    * 1) Source Account verifies funds available
    * 2) Source Account and Destination Account update their balances
    * 3) Source Account updates statement information
    *
    * Algorithm (steps to implement the scenario):
    *
    * 1a) Source Account begins transaction
    * 1b) Source Account verifies that its current balance is greater than
    * the minimum account balance plus the withdrawal amount, and throws an exception if not
    *
    * 2a) Source Account reduces its own balance by the amount
    * 2b) Source Account requests that Destination Account increase its balance
    *
    * 3a) Source Account updates its log to note that this was a transfer
    * 3b) Source Account requests that Destination Account update its log
    * 3c) Source Account ends transaction
    * 3d) Source Account returns status that the transfer has succeeded
    */

   // Roles defined by this context, with default implementations
   @Mixins( SourceAccountRole.Mixin.class )
   public interface SourceAccountRole
   {
      // Role Methods

      void transfer( Integer amount )
            throws IllegalArgumentException;

      Integer availableFunds();

      // Default implementation

      class Mixin
            implements SourceAccountRole
      {
         @This
         BalanceData data;

         @Context
         TransferMoneyContext context; // injected current Context

         public Integer availableFunds()
         {
            // Could be balance, or balance - non-confirmed transfers, or somesuch
            return data.getBalance();
         }

         public void transfer( Integer amount )
               throws IllegalArgumentException
         {
            log( "Transfer initiated" );                    // 1a

            // Validate command
            if (!( data.getBalance() >= amount ))             // 1b
               throw new IllegalArgumentException( "Not enough available funds" );

            // Command is ok - create events in the data
            data.decreasedBalance( amount );                // 2a

            log( "Source account      -" + amount );        // (not in algorithm)

            // Look up the destination account from the current transfer context
            context.destinationAccount.deposit( amount );   // 2b + 3a-3c

            log( "Transfer completed successfully" );       // 3d
         }
      }
   }

   @Mixins( DestinationAccountRole.Mixin.class )
   public interface DestinationAccountRole
   {
      public void deposit( Integer amount );

      class Mixin
            implements DestinationAccountRole
      {
         @This
         BalanceData data;

         public void deposit( Integer amount )
         {
            data.increasedBalance( amount );

            log( "Destination account +" + amount );
         }
      }
   }
}
