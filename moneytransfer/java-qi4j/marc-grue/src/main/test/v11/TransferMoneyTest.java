/*
 * Copyright (c) 2010, Marc Grue. All Rights Reserved.
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

package v11;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.spi.uuid.UuidIdentityGeneratorService;
import v11.api.ContextInjectionProviderFactory;
import v11.domain.context.PayBillsContext;
import v11.domain.context.TransferMoneyContext;
import v11.domain.data.Balance;
import v11.domain.data.Bank;
import v11.domain.entity.BankEntity;
import v11.domain.entity.CheckingAccountEntity;
import v11.domain.entity.SavingsAccountEntity;
import v11.domain.rolemap.BankRolemap;
import v11.domain.rolemap.CheckingAccountRolemap;
import v11.domain.rolemap.CreditorRolemap;
import v11.domain.rolemap.SavingsAccountRolemap;

import static org.qi4j.api.usecase.UsecaseBuilder.newUsecase;
import static v11.api.TemporaryHelper.log;
import static v11.api.TemporaryHelper.setUowf;

/**
 * Test of TransferMoneyContext and PayBillsContext
 */
public class TransferMoneyTest
{
   private static SingletonAssembler assembler;

   public static final String BANK = "Bank";
   public static final String SAVINGS_ACCOUNT_ID = "SavingsAccountId";
   public static final String CHECKING_ACCOUNT_ID = "CheckingAccountId";
   public static final String CREDITOR_ID1 = "BakerAccount";
   public static final String CREDITOR_ID2 = "ButcherAccount";

   @BeforeClass
   public static void setup() throws Exception
   {
      assembler = new SingletonAssembler()
      {
         public void assemble( ModuleAssembly module ) throws AssemblyException
         {
            module.layerAssembly().applicationAssembly().setMetaInfo( new ContextInjectionProviderFactory() );

            module.addEntities(
                  BankRolemap.class,
                  SavingsAccountRolemap.class,
                  CheckingAccountRolemap.class,
                  CreditorRolemap.class );

            module.addServices(
                  MemoryEntityStoreService.class,
                  UuidIdentityGeneratorService.class );
         }
      };

      bootstrapData( assembler );

      // Hack to provide access to a uowf in the PayBillsContext
      setUowf( assembler.unitOfWorkFactory() );

      log( "MONEY TRANSFER TESTS - v11 \n========================================================" );
   }

   @Before
   public void beforeBalances() throws Exception
   {
      log( "### Before enactment:" );
      printBalances();
      log( "" );
   }

   @After
   public void afterBalances() throws Exception
   {
      log( "" );
      log( "### After enactment:" );
      printBalances();
      log( "====================================" );
   }

   public void printBalances()
   {
      UnitOfWork uow = assembler.unitOfWorkFactory().newUnitOfWork( UsecaseBuilder.newUsecase( "Print balances" ) );

      try
      {
         log( SAVINGS_ACCOUNT_ID + ":" + uow.get( Balance.class, SAVINGS_ACCOUNT_ID ).getBalance() );
         log( CHECKING_ACCOUNT_ID + ":" + uow.get( Balance.class, CHECKING_ACCOUNT_ID ).getBalance() );
         log( CREDITOR_ID1 + ":" + uow.get( Balance.class, CREDITOR_ID1 ).getBalance() );
         log( CREDITOR_ID2 + ":" + uow.get( Balance.class, CREDITOR_ID2 ).getBalance() );
      }
      finally
      {
         uow.discard();
      }
   }

   private static void bootstrapData( SingletonAssembler assembler ) throws Exception
   {
      UnitOfWork uow = assembler.unitOfWorkFactory().newUnitOfWork( newUsecase( "Bootstrap data" ) );
      try
      {
         // Accounts
         SavingsAccountEntity savingsAccount = uow.newEntity( SavingsAccountEntity.class, SAVINGS_ACCOUNT_ID );
         savingsAccount.increasedBalance( 1000 );

         CheckingAccountEntity checkingAccount = uow.newEntity( CheckingAccountEntity.class, CHECKING_ACCOUNT_ID );

         BankEntity bank = uow.newEntity( BankEntity.class, BANK );
         bank.accounts().add( savingsAccount );
         bank.accounts().add( checkingAccount );


         // Creditors
         Balance bakerAccount = uow.newEntity( CreditorRolemap.class, CREDITOR_ID1 );
         bakerAccount.decreasedBalance( 50 );

         Balance butcherAccount = uow.newEntity( CreditorRolemap.class, CREDITOR_ID2 );
         butcherAccount.decreasedBalance( 90 );

         bank.creditors().add( bakerAccount );
         bank.creditors().add( butcherAccount );

         // Save
         uow.complete();
      }
      catch (Exception e)
      {
         uow.discard();
         throw e;
      }
   }

   @Test
   public void transferHalfOfMoneyFromSavingsToChecking() throws Exception
   {
      UnitOfWork uow = assembler.unitOfWorkFactory().newUnitOfWork( newUsecase( "Transfer from savings to checking" ) );

      try
      {
         // Select Data for source and destination accounts
         Bank bank = uow.get( Bank.class, BANK );

         // Instantiate context, bind roles and execute enactments
         TransferMoneyContext context = new TransferMoneyContext( bank, SAVINGS_ACCOUNT_ID, CHECKING_ACCOUNT_ID );

         // Query for half the balance
         final Integer amountToTransfer = context.availableFunds() / 2;

         // Transfer from savings to checking
         context.transfer( amountToTransfer );

         uow.complete();
      }
      catch (Exception e)
      {
         uow.discard();
         throw e;
      }
   }

   @Test
   public void payAllBillsToCreditors() throws Exception
   {
      UnitOfWork uow = assembler.unitOfWorkFactory().newUnitOfWork( newUsecase( "Pay all bills from checking to creditors" ) );
      try
      {
         Bank bank = uow.get( Bank.class, BANK );

         new PayBillsContext( bank, CHECKING_ACCOUNT_ID ).payBills();

         uow.complete();
      }
      catch (Exception e)
      {
         uow.discard();
         throw e;
      }
   }
}
