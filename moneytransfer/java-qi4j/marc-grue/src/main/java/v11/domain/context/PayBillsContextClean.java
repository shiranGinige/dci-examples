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

import java.util.List;


/**
 * Context for paying bills - v11
 *
 * Identical to v10 except that Data postfix is skipped in Data interface names
 *
 * Clean version without comments and logging
 */
public class PayBillsContextClean
{
   private BankRole Bank;
   private Balance SourceAccount;
   private Balance Creditor;


   public PayBillsContextClean( Bank bank, String sourceId )
   {
      Bank = (BankRole) bank;
      SourceAccount = Bank.getAccount( sourceId );
   }

   public void payBills() throws Exception
   {
      Contexts.withContext( this, new Contexts.Command<PayBillsContextClean, Exception>()
      {
         public void command( PayBillsContextClean transferMoneyContext ) throws Exception
         {
            Bank.payBills();
         }
      } );
   }

   public boolean canFindUnpaidCreditor()
   {
      Creditor = null;
      for (Balance creditorCandidate : Bank.getCreditors())
      {
         if (creditorCandidate.getBalance() < 0)
         {
            Creditor = creditorCandidate;
            break;
         }
      }
      return Creditor != null;
   }

   @Mixins( BankRole.Mixin.class )
   public interface BankRole
   {
      Balance getAccount( String accountId );
      List<Balance> getCreditors();
      void payBills() throws Exception;

      class Mixin
            implements BankRole
      {
         @ThisContext
         PayBillsContextClean context;

         @This
         Bank self;

         public void payBills() throws IllegalArgumentException
         {
            if (context.SourceAccount.getBalance() - getSumOwed() < 0)
               throw new IllegalArgumentException( "Insufficient funds to pay bills." );

            final TransferMoneyContext transferContext = new TransferMoneyContext();
            while (context.canFindUnpaidCreditor())
            {
               final Integer amountOwed = -context.Creditor.getBalance();
               transferContext.bind( self, context.SourceAccount, context.Creditor ).transfer( amountOwed );
            }
         }

         public Balance getAccount( String accountId )
         {
            return self.getAccount( accountId );
         }

         public List<Balance> getCreditors()
         {
            return self.creditors().toList();
         }

         private Integer getSumOwed()
         {
            Integer sumOwed = 0;
            for (Balance creditor : self.creditors())
               sumOwed += creditor.getBalance() < 0 ? -creditor.getBalance() : 0;

            return sumOwed;
         }
      }
   }
}