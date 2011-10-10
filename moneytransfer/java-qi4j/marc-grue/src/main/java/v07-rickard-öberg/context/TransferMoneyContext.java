/*
 * Copyright (c) 2010, Rickard Öberg. All Rights Reserved.
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

package v07.context;

import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import v07.domain.data.BalanceData;

import static v07.api.Contexts.*;

/**
 * Context for transfer of money between two accounts. Roles are defined within the context,
 * and only the entities know about them.
 */
public class TransferMoneyContext
{
   // Object->Role mappings
   public SourceAccountRole source;
   public DestinationAccountRole destination;

   // Context setup
   // Objects are specified using their data interface, and then cast to the role interfaces
   public TransferMoneyContext( BalanceData source, BalanceData destination )
   {
      this.source = (SourceAccountRole) source;
      this.destination = (DestinationAccountRole) destination;
   }

   // Interactions
   public Integer availableFunds()
   {
      return source.availableFunds();
   }

   public void transfer( Integer amount )
         throws IllegalArgumentException
   {
      source.transfer( amount );
   }

   // More interactions could go here...

   // Roles
   @Mixins(SourceAccountRole.Mixin.class)
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

         public Integer availableFunds()
         {
            // Could be balance, or balance - non-confirmed transfers, or somesuch
            return data.getBalance();
         }

         public void transfer( Integer amount )
               throws IllegalArgumentException
         {
            // Validate command
            if (!(data.getBalance() >= amount))
               throw new IllegalArgumentException("Not enough available funds");

            // Command is ok - create events in the data
            data.decreasedBalance( amount );

            // Look up the destination account from the current transfer context
            context( TransferMoneyContext.class ).destination.deposit( amount );
         }
      }
   }

   @Mixins(DestinationAccountRole.Mixin.class)
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
         }
      }
   }
}
