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

package v08;

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
import v07.api.Contexts;
import v07.context.TransferMoneyContext;
import v07.domain.data.BalanceData;
import v08.atm.entity.CheckingAccountATM;
import v08.atm.entity.CreditorATM;
import v08.atm.entity.SavingsAccountATM;
import v08.domain.entity.CheckingAccountEntity;
import v08.domain.entity.CreditorEntity;
import v08.domain.entity.SavingsAccountEntity;

import static org.qi4j.api.usecase.UsecaseBuilder.newUsecase;

/**
 * Test of TransferMoneyContext
 */
public class TransferMoney
{
   private static SingletonAssembler assembler;
   public static final String SAVINGS_ACCOUNT_ID = "SavingsAccountId";
   public static final String CHECKING_ACCOUNT_ID = "CheckingAccountId";
   public static final String CREDITOR_ID = "CreditorId";

   @BeforeClass
   public static void setup() throws Exception
   {
      assembler = new SingletonAssembler()
      {
         public void assemble( ModuleAssembly module ) throws AssemblyException
         {
            module.addEntities(
                  CheckingAccountATM.class,
                  SavingsAccountATM.class,
                  CreditorATM.class );

            module.addServices(
                  MemoryEntityStoreService.class,
                  UuidIdentityGeneratorService.class );
         }
      };

      bootstrapData( assembler );
   }

   @Before
   public void beforeBalances()
   {
      System.out.println( "Before enactment:" );
      printBalances();
      System.out.println( "" );
   }

   @After
   public void afterBalances()
   {
      System.out.println( "After enactment:" );
      printBalances();
      System.out.println( "-----------------" );
   }

   public void printBalances()
   {
      UnitOfWork uow = assembler.unitOfWorkFactory().newUnitOfWork( UsecaseBuilder.newUsecase( "Transfer from savings to checking" ) );

      try
      {
         System.out.println( SAVINGS_ACCOUNT_ID + ":" + uow.get( BalanceData.class, SAVINGS_ACCOUNT_ID ).getBalance() );
         System.out.println( CHECKING_ACCOUNT_ID + ":" + uow.get( BalanceData.class, CHECKING_ACCOUNT_ID ).getBalance() );
         System.out.println( CREDITOR_ID + ":" + uow.get( BalanceData.class, CREDITOR_ID ).getBalance() );
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
         uow.newEntity( CreditorEntity.class, CREDITOR_ID );

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
         // Select source and destination
         BalanceData source = uow.get( BalanceData.class, SAVINGS_ACCOUNT_ID );
         BalanceData destination = uow.get( BalanceData.class, CHECKING_ACCOUNT_ID );

         // Instantiate context and execute enactments with that context
         TransferMoneyContext context = new TransferMoneyContext( source, destination );

         // Query for half the balance
         final Integer amountToTransfer = Contexts.withContext( context, new Contexts.Query<Integer, TransferMoneyContext, RuntimeException>()
         {
            public Integer query( TransferMoneyContext transferMoneyContext ) throws RuntimeException
            {
               return transferMoneyContext.availableFunds() / 2;
            }
         } );

         // Transfer from savings to checking
         Contexts.withContext( context, new Contexts.Command<TransferMoneyContext, IllegalArgumentException>()
         {
            public void command( TransferMoneyContext transferMoneyContext ) throws IllegalArgumentException
            {
               transferMoneyContext.transfer( amountToTransfer );
            }
         } );

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
         // Select source and destination
         BalanceData source = uow.get( BalanceData.class, SAVINGS_ACCOUNT_ID );
         BalanceData destination = uow.get( BalanceData.class, CHECKING_ACCOUNT_ID );

         // Instantiate context and execute enactments with that context
         TransferMoneyContext context = new TransferMoneyContext( source, destination );

         // Query for double the balance
         final Integer amountToTransfer = Contexts.withContext( context, new Contexts.Query<Integer, TransferMoneyContext, RuntimeException>()
         {
            public Integer query( TransferMoneyContext transferMoneyContext ) throws RuntimeException
            {
               return transferMoneyContext.availableFunds() * 2;
            }
         } );

         // Transfer from savings to checking
         Contexts.withContext( context, new Contexts.Command<TransferMoneyContext, IllegalArgumentException>()
         {
            public void command( TransferMoneyContext transferMoneyContext ) throws IllegalArgumentException
            {
               transferMoneyContext.transfer( amountToTransfer );
            }
         } );

         uow.complete();
      }
      catch (Exception e)
      {
         uow.discard();
         throw e;
      }
   }

   @Test
   public void transferAllMoneyFromCheckingToCreditor() throws Exception
   {
      UnitOfWork uow = assembler.unitOfWorkFactory().newUnitOfWork( UsecaseBuilder.newUsecase( "Transfer from savings to checking" ) );

      try
      {
         // Select source and destination
         BalanceData source = uow.get( BalanceData.class, CHECKING_ACCOUNT_ID );
         BalanceData destination = uow.get( BalanceData.class, CREDITOR_ID );

         // Instantiate context and execute enactments with that context
         TransferMoneyContext context = new TransferMoneyContext( source, destination );

         // Query for the balance
         final Integer amountToTransfer = Contexts.withContext( context, new Contexts.Query<Integer, TransferMoneyContext, RuntimeException>()
         {
            public Integer query( TransferMoneyContext transferMoneyContext ) throws RuntimeException
            {
               return transferMoneyContext.availableFunds();
            }
         } );

         // Transfer from checking to creditor
         Contexts.withContext( context, new Contexts.Command<TransferMoneyContext, IllegalArgumentException>()
         {
            public void command( TransferMoneyContext transferMoneyContext ) throws IllegalArgumentException
            {
               transferMoneyContext.transfer( amountToTransfer );
            }
         } );

         uow.complete();
      }
      catch (Exception e)
      {
         uow.discard();
         throw e;
      }
   }
}
