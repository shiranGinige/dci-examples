/*
 * Copyright (c) 2010, Marc Grue. All Rights Reserved.
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

package v11.domain.context;

import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import v11.api.Contexts;
import v11.api.ThisContext;
import v11.domain.data.Balance;
import v11.domain.data.Bank;

import static v11.api.TemporaryHelper.log;


/**
 * Context for transfer of money in a bank - v11
 *
 * Identical to v10 except that Data postfix is skipped in Data interface names
 *
 * A customer asks the #Bank (through a Teller Machine) to transfer money from a #SourceAccount
 * to a #DestinationAccount. The Bank Role is orchestrating the interactions.
 */
public class TransferMoneyContext
//      extends Context
{
   // Role Players (all methodful Roles)
   private BankRole Bank;
   private SourceAccountRole SourceAccount;
   private DestinationAccountRole DestinationAccount;

   public TransferMoneyContext( Bank bank, String sourceAccountId, String destinationAccountId )
   {
      // Static Role binding (happens _once_ before enactment)
      Bank = (BankRole) bank;
      SourceAccount = (SourceAccountRole) Bank.getAccount( sourceAccountId );
      DestinationAccount = (DestinationAccountRole) Bank.getAccount( destinationAccountId );
   }

   // Constructor + bind method for PayBillsContext
   
   public TransferMoneyContext() {}

   public TransferMoneyContext bind( Bank bank, Balance sourceData, Balance destinationData )
   {
      // Static Role binding from outside (happens _once_ before enactment)
      Bank = (BankRole) bank;
      SourceAccount = (SourceAccountRole) sourceData;
      DestinationAccount = (DestinationAccountRole) destinationData;

      return this;
   }
   public TransferMoneyContext( Bank bank, Balance sourceData, Balance destinationData )
   {
      // Static Role binding from outside (happens _once_ before enactment)
      Bank = (BankRole) bank;
      SourceAccount = (SourceAccountRole) sourceData;
      DestinationAccount = (DestinationAccountRole) destinationData;
   }

   // Interactions

   public Integer availableFunds()
   {
      // (The next 7 lines will become a 1-liner (line 5) when Qi4j implements Concerns/Advices on POJOs)
      return Contexts.withContext( this, new Contexts.Query<Integer, TransferMoneyContext, RuntimeException>()
      {
         public Integer query( TransferMoneyContext transferMoneyContext ) throws RuntimeException
         {
            return SourceAccount.availableFunds();
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
            Bank.transfer( amount );
         }
      } );
   }

   // Methodful Roles

   @Mixins( BankRole.Mixin.class )
   public interface BankRole
   {
      void transfer( Integer amount ) throws IllegalArgumentException;

      Balance getAccount( String accountId );

      class Mixin
            implements BankRole
      {
         @ThisContext
         TransferMoneyContext context;

         @This
         Bank self;

         public void transfer( Integer amount )
               throws IllegalArgumentException
         {
            log( "Transfer initiated" );

            context.SourceAccount.transfer( amount );

            log( "Transfer completed successfully" );
         }

         public Balance getAccount( String accountId )
         {
            return self.getAccount( accountId );
         }
      }
   }

   @Mixins( SourceAccountRole.Mixin.class )
   public interface SourceAccountRole
   {
      void transfer( Integer amount ) throws IllegalArgumentException;

      Integer availableFunds();

      class Mixin
            implements SourceAccountRole
      {
         @ThisContext
         TransferMoneyContext context;

         @This
         Balance self;

         public Integer availableFunds()
         {
            return self.getBalance();
         }

         public void transfer( Integer amount )
               throws IllegalArgumentException
         {
            if (!( self.getBalance() >= amount ))
               throw new IllegalArgumentException( "Not enough available funds" );

            self.decreasedBalance( amount );

            log( "Source account      -" + amount );

            context.DestinationAccount.deposit( amount );
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
         Balance self;

         public void deposit( Integer amount )
         {
            self.increasedBalance( amount );

            log( "Destination account +" + amount );
         }
      }
   }
}
