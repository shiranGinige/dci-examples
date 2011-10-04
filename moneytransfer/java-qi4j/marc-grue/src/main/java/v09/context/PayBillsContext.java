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

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import v09.api.Contexts;
import v09.domain.data.BalanceData;
import v09.rolemap.CreditorRolemap;

import java.util.ArrayList;
import java.util.List;

import static v09.api.Contexts.log;

/**
 * Context for paying bills from an account to a list of creditor accounts.
 *
 * Roles are defined within the context
 * A RoleMap lists what Roles an entity can play.
 */
public class PayBillsContext
{
   private SourceAccountRole sourceAccount;

   public PayBillsContext bind( BalanceData sourceData )
   {
      sourceAccount = (SourceAccountRole) sourceData;
      return this;
   }

   public void payBills()
         throws Exception
   {
      Contexts.withContext( this, new Contexts.Command<PayBillsContext, Exception>()
      {
         public void command( PayBillsContext transferMoneyContext ) throws Exception
         {
            sourceAccount.payBills();
         }
      } );
   }

   /**
    * The SourceAccountRole orchestrates the Pay Bills use case interactions.
    *
    * Code matches the use case text carefully (see references below).
    *
    * Pay Bills use case scenario:
    *
    * 1) Bank finds creditors (could be a use case scenario in itself)
    * 2) Bank calculates the amount owed to creditors
    * 3) Bank verifies sufficient funds
    * 4) Bank transfer money to each creditor
    *
    * Algorithm (steps to implement the scenario):
    *
    * 1a) Source Account starts use case
    * 1a) Source Account finds list of creditors
    *
    * 2a) Source Account loops creditors to find the sum owed
    *
    * 3a) Source Account verifies that its current balance is greater than the sum owed, and throws an exception if not
    *
    * 4a) Source Account update its log to not that it starts paying bills
    * 4b) Source Account loops creditors
    * 4c) Make a MoneyTransfer of the amount owed to each creditor
    * 4d) Source Account updates its log to note that all bills were paid successfully
    */

   @Mixins( SourceAccountRole.Mixin.class )
   public interface SourceAccountRole
   {
      void payBills() throws Exception;


      class Mixin
            implements SourceAccountRole
      {
         @Structure
         UnitOfWorkFactory uowf;

         @This
         BalanceData data;

         public void payBills() throws IllegalArgumentException
         {
            log( "Paying all bills to creditors" );                                         // 1a

            List<BalanceData> creditors = getCreditors();                                   // 1b

            Integer sumOwed = getSumOwedTo( creditors );                                    // 2a

            if (data.getBalance() - sumOwed < 0)                                            // 3a
               throw new IllegalArgumentException( "Insufficient funds to pay bills." );

            final TransferMoneyContext transferMoney = new TransferMoneyContext();
            for (BalanceData creditor : creditors)                                          // 4b
            {
               if (creditor.getBalance() < 0)
               {
                  final Integer amountOwed = -creditor.getBalance();

                  // Bind Data in nested context and execute enactment
                  // Note that the same context is used with new role bindings
                  transferMoney.bind( data, creditor ).transfer( amountOwed );              // 4c
               }
            }
            log( "All bills paid successfully" );                                           // 4d
         }

         // Internal helper methods to make the code above more readable / comparable to the algorithm text

         private List<BalanceData> getCreditors()
         {
            // Creditor retrieval could be a use case in itself...
            List<BalanceData> creditors = new ArrayList<BalanceData>();
            creditors.add( uowf.currentUnitOfWork().get( CreditorRolemap.class, "BakerAccount" ) );
            creditors.add( uowf.currentUnitOfWork().get( CreditorRolemap.class, "ButcherAccount" ) );
            return creditors;
         }

         private Integer getSumOwedTo( List<BalanceData> creditors )
         {
            Integer sumOwed = 0;
            for (BalanceData creditor : creditors)
            {
               sumOwed += creditor.getBalance() < 0 ? -creditor.getBalance() : 0;
            }

            return sumOwed;
         }
      }
   }
}
