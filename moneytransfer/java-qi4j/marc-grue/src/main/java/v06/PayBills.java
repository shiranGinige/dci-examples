/* Copyright (c) 2010, Marc Grue. All Rights Reserved.
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

package v06;

import v06.api.RoleMap;
import v06.domain.data.Transactions;
import v06.domain.entity.AccountEntity;
import v06.domain.entity.TransactionEntity;
import v06.domain.entity.TransactionEntryEntity;
import v06.domain.entity.TransactionsEntity;
import v06.domain.usecase.context.PayBillsContext;
import v06.domain.usecase.context.TransferMoneyContext;
import v06.domain.usecase.role.DestinationAccountRole;
import v06.domain.usecase.role.SourceAccountRole;
import org.qi4j.api.composite.TransientBuilderFactory;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.spi.uuid.UuidIdentityGeneratorService;

import static org.qi4j.api.usecase.UsecaseBuilder.newUsecase;

/**
 * Java/Qi4j sample program for DCI context execution
 */
public class PayBills
{
   public static void main( String[] args ) throws Exception
   {
      SingletonAssembler assembler = new SingletonAssembler()
      {
         public void assemble( ModuleAssembly module ) throws AssemblyException
         {
            module.addEntities(
                  AccountEntity.class,
                  TransactionEntity.class,
                  TransactionsEntity.class,
                  TransactionEntryEntity.class );

            module.addTransients(
                  TransferMoneyContext.class,
                  PayBillsContext.class,
                  SourceAccountRole.class,
                  DestinationAccountRole.class );

            module.addServices(
                  MemoryEntityStoreService.class,
                  UuidIdentityGeneratorService.class,
                  RoleMap.class );
         }
      };

      bootstrapData( assembler );

      UnitOfWork uow = assembler.unitOfWorkFactory().newUnitOfWork();
      TransientBuilderFactory tbf = assembler.module().transientBuilderFactory();

      System.out.println( "MONEY TRANSFER v6 - Pay Bills" );

      // Create context and trigger interaction
      PayBillsContext context = tbf.newTransient( PayBillsContext.class );
      context.payBills( "CheckingAccountId" );

      uow.discard();
   }

   private static void bootstrapData( SingletonAssembler assembler ) throws Exception
   {
      UnitOfWork uow = assembler.unitOfWorkFactory().newUnitOfWork( newUsecase( "Bootstrap data" ) );
      try
      {
         uow.newEntity( AccountEntity.class, "CheckingAccountId" );
         uow.newEntity( AccountEntity.class, "BakerAccountId" );
         uow.newEntity( AccountEntity.class, "ButcherAccountId" );

         Transactions transactions = uow.newEntity( Transactions.class, TransactionsEntity.TRANSACTIONS );
         transactions.createTransaction( "InitialSourceAccountBalance" );
         transactions.addTransactionEntry( "CheckingAccountId", 800 );
         transactions.addTransactionEntry( "Cash", -800 );
         transactions.completeTransaction();

         transactions.createTransaction( "BakerDept" );
         transactions.addTransactionEntry( "BakerAccountId", -90 );
         transactions.addTransactionEntry( "BakerAsset", 90 );
         transactions.completeTransaction();

         transactions.createTransaction( "ButcherDept" );
         transactions.addTransactionEntry( "ButcherAccountId", -50 );
         transactions.addTransactionEntry( "ButcherAsset", 50 );
         transactions.completeTransaction();

         // Save
         uow.complete();
      } catch (Exception e)
      {
         e.printStackTrace();
         uow.discard();
      }
   }
}