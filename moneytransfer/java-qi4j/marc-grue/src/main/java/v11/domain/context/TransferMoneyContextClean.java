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


/**
 * Context for transfer of money in a bank - v11
 *
 * Identical to v10 except that Data postfix is skipped in Data interface names
 *
 * Clean version without comments and logging
 */
public class TransferMoneyContextClean
{
   private BankRole Bank;
   private SourceAccountRole SourceAccount;
   private DestinationAccountRole DestinationAccount;

   public TransferMoneyContextClean( Bank bank, String sourceAccountId, String destinationAccountId )
   {
      Bank = (BankRole) bank;
      SourceAccount = (SourceAccountRole) Bank.getAccount( sourceAccountId );
      DestinationAccount = (DestinationAccountRole) Bank.getAccount( destinationAccountId );
   }

   public TransferMoneyContextClean() {}

   public TransferMoneyContextClean bind( Bank bank, Balance sourceData, Balance destinationData )
   {
      Bank = (BankRole) bank;
      SourceAccount = (SourceAccountRole) sourceData;
      DestinationAccount = (DestinationAccountRole) destinationData;

      return this;
   }

   public Integer availableFunds()
   {
      return Contexts.withContext( this, new Contexts.Query<Integer, TransferMoneyContextClean, RuntimeException>()
      {
         public Integer query( TransferMoneyContextClean transferMoneyContext ) throws RuntimeException
         {
            return SourceAccount.availableFunds();
         }
      } );
   }

   public void transfer( final Integer amount )
         throws IllegalArgumentException
   {
      Contexts.withContext( this, new Contexts.Command<TransferMoneyContextClean, IllegalArgumentException>()
      {
         public void command( TransferMoneyContextClean transferMoneyContext ) throws IllegalArgumentException
         {
            Bank.transfer( amount );
         }
      } );
   }


   @Mixins( BankRole.Mixin.class )
   public interface BankRole
   {
      void transfer( Integer amount ) throws IllegalArgumentException;

      Balance getAccount( String accountId );

      class Mixin
            implements BankRole
      {
         @ThisContext
         TransferMoneyContextClean context;

         @This
         Bank self;

         public void transfer( Integer amount )
               throws IllegalArgumentException
         {
            context.SourceAccount.transfer( amount );
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
         TransferMoneyContextClean context;

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
         }
      }
   }
}
