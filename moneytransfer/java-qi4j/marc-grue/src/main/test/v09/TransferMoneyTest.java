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

package v09;

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
import v09.api.ContextInjectionProviderFactory;
import v09.context.PayBillsContext;
import v09.context.TransferMoneyContext;
import v09.domain.data.BalanceData;
import v09.domain.entity.CheckingAccountEntity;
import v09.domain.entity.SavingsAccountEntity;
import v09.rolemap.CheckingAccountRolemap;
import v09.rolemap.CreditorRolemap;
import v09.rolemap.SavingsAccountRolemap;

import static org.qi4j.api.usecase.UsecaseBuilder.newUsecase;

/**
 * Test of TransferMoneyContext and PayBillsContext
 */
public class TransferMoneyTest
{
   private static SingletonAssembler assembler;
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
                  CheckingAccountRolemap.class,
                  SavingsAccountRolemap.class,
                  CreditorRolemap.class );

            module.addServices(
                  MemoryEntityStoreService.class,
                  UuidIdentityGeneratorService.class );
         }
      };

      bootstrapData( assembler );

      System.out.println( "MONEY TRANSFER TESTS - v9 \n========================================================" );
   }

   @Before
   public void beforeBalances() throws Exception
   {
      System.out.println( "### Before enactment:" );
      printBalances();
      System.out.println( "" );
   }

   @After
   public void afterBalances() throws Exception
   {
      System.out.println( "" );
      System.out.println( "### After enactment:" );
      printBalances();
      System.out.println( "====================================" );
   }

   public void printBalances()
   {
      UnitOfWork uow = assembler.unitOfWorkFactory().newUnitOfWork( UsecaseBuilder.newUsecase( "Print balances" ) );

      try
      {
         System.out.println( SAVINGS_ACCOUNT_ID + ":" + uow.get( BalanceData.class, SAVINGS_ACCOUNT_ID ).getBalance() );
         System.out.println( CHECKING_ACCOUNT_ID + ":" + uow.get( BalanceData.class, CHECKING_ACCOUNT_ID ).getBalance() );
         System.out.println( CREDITOR_ID1 + ":" + uow.get( BalanceData.class, CREDITOR_ID1 ).getBalance() );
         System.out.println( CREDITOR_ID2 + ":" + uow.get( BalanceData.class, CREDITOR_ID2 ).getBalance() );
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
         SavingsAccountEntity account = uow.newEntity( SavingsAccountEntity.class, SAVINGS_ACCOUNT_ID );
         account.increasedBalance( 1000 );

         uow.newEntity( CheckingAccountEntity.class, CHECKING_ACCOUNT_ID );

         // Create some creditor debt
         BalanceData bakerAccount = uow.newEntity( CreditorRolemap.class, CREDITOR_ID1 );
         bakerAccount.decreasedBalance( 50 );

         BalanceData butcherAccount = uow.newEntity( CreditorRolemap.class, CREDITOR_ID2 );
         butcherAccount.decreasedBalance( 90 );

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
      UnitOfWork uow = assembler.unitOfWorkFactory().newUnitOfWork( UsecaseBuilder.newUsecase( "Transfer from savings to checking" ) );

      try
      {
         // Select Data for source and destination accounts
         BalanceData sourceData = uow.get( BalanceData.class, SAVINGS_ACCOUNT_ID );
         BalanceData destinationData = uow.get( BalanceData.class, CHECKING_ACCOUNT_ID );

         // Instantiate context, bind roles and execute enactments
         TransferMoneyContext context = new TransferMoneyContext();
         context.bind( sourceData, destinationData );

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

   @Test( expected = IllegalArgumentException.class )
   public void transferTwiceOfMoneyFromSavingsToChecking() throws Exception
   {
      UnitOfWork uow = assembler.unitOfWorkFactory().newUnitOfWork( UsecaseBuilder.newUsecase( "Transfer from savings to checking" ) );

      try
      {
         BalanceData sourceData = uow.get( BalanceData.class, SAVINGS_ACCOUNT_ID );
         BalanceData destinationData = uow.get( BalanceData.class, CHECKING_ACCOUNT_ID );

         TransferMoneyContext context = new TransferMoneyContext().bind( sourceData, destinationData );

         // Query for double the balance
         final Integer amountToTransfer = context.availableFunds() * 2;

         context.transfer( amountToTransfer );

         uow.complete();
      }
      catch (Exception e)
      {
         System.out.println( e.getMessage() );
         uow.discard();
         throw e;
      }
   }

   @Test
   public void payAllBills() throws Exception
   {
      UnitOfWork uow = assembler.unitOfWorkFactory().newUnitOfWork( newUsecase( "Pay all bills from checking to creditors" ) );
      try
      {
         BalanceData sourceData = uow.get( BalanceData.class, CHECKING_ACCOUNT_ID );

         new PayBillsContext().bind( sourceData ).payBills();

         uow.complete();
      }
      catch (Exception e)
      {
         uow.discard();
         throw e;
      }
   }
}
