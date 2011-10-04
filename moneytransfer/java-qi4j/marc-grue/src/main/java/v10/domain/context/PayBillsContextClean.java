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

package v10.domain.context;

import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import v10.api.Contexts;
import v10.api.ThisContext;
import v10.domain.data.BalanceData;
import v10.domain.data.BankData;

import java.util.List;


/**
 * Context for paying bills - v10
 *
 * Clean version without comments and logging
 */
public class PayBillsContextClean
{
   private BankRole Bank;
   private BalanceData SourceAccount;
   private BalanceData Creditor;


   public PayBillsContextClean( BankData bank, String sourceId )
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
      for (BalanceData creditorCandidate : Bank.getCreditors())
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
      BalanceData getAccount( String accountId );
      List<BalanceData> getCreditors();
      void payBills() throws Exception;

      class Mixin
            implements BankRole
      {
         @ThisContext
         PayBillsContextClean context;

         @This
         BankData self;

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

         public BalanceData getAccount( String accountId )
         {
            return self.getAccount( accountId );
         }

         public List<BalanceData> getCreditors()
         {
            return self.creditors().toList();
         }

         private Integer getSumOwed()
         {
            Integer sumOwed = 0;
            for (BalanceData creditor : self.creditors())
               sumOwed += creditor.getBalance() < 0 ? -creditor.getBalance() : 0;

            return sumOwed;
         }
      }
   }
}