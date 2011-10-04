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

import static v10.api.TemporaryHelper.log;


/**
 * Context for paying bills - v10
 *
 * A customer asks the #Bank (through a Teller Machine) to transfer money from a #SourceAccount
 * to each #Creditor that he/she owes money to. The Bank Role is orchestrating the interactions.
 */
public class PayBillsContext
{
   // Methodful Role
   private BankRole Bank;

   // Methodless Roles
   private BalanceData SourceAccount;
   private BalanceData Creditor;


   public PayBillsContext( BankData bank, String sourceId )
   {
      // Static Role binding (happens _once_ before enactment)
      Bank = (BankRole) bank;
      SourceAccount = Bank.getAccount( sourceId );
   }

   public void payBills() throws Exception
   {
      Contexts.withContext( this, new Contexts.Command<PayBillsContext, Exception>()
      {
         public void command( PayBillsContext transferMoneyContext ) throws Exception
         {
            Bank.payBills();
         }
      } );
   }

   public boolean canFindUnpaidCreditor()
   {
      // Dynamic Role binding (happens *during* enactment)
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

   // Methodful Role

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
         PayBillsContext context;

         @This
         BankData self; // Think in terms of BankRole (= self), not BankData

         public void payBills() throws IllegalArgumentException
         {
            log( "Paying all bills to creditors" );

            if (context.SourceAccount.getBalance() - getSumOwed() < 0)
               throw new IllegalArgumentException( "Insufficient funds to pay bills." );

            // Reselect Creditor Role player until all creditors are paid
            final TransferMoneyContext transferContext = new TransferMoneyContext();
            while (context.canFindUnpaidCreditor())
            {
               final Integer amountOwed = -context.Creditor.getBalance();
               transferContext.bind( self, context.SourceAccount, context.Creditor ).transfer( amountOwed );

               // Would be more intuitive to do:
               // new TransferMoneyContext( self, context.SourceAccount, context.Creditor ).transfer( amountOwed );
            }

            log( "All bills paid successfully" );
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
            {
               sumOwed += creditor.getBalance() < 0 ? -creditor.getBalance() : 0;
            }

            return sumOwed;
         }
      }
   }
}