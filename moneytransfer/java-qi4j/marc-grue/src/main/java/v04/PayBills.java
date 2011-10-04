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

package v04;

import v04.domain.behavior.context.PayBillsContext;
import v04.domain.behavior.context.TransferMoneyContext;
import v04.domain.behavior.role.DestinationAccount;
import v04.domain.behavior.role.SourceAccount;
import v04.domain.structure.entity.CheckingAccountEntity;
import v04.domain.structure.entity.CreditorEntity;
import v04.domain.structure.entity.SavingsAccountEntity;
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
                  SavingsAccountEntity.class,
                  CheckingAccountEntity.class,
                  CreditorEntity.class );

            module.addTransients(
                  TransferMoneyContext.class,
                  PayBillsContext.class,
                  SourceAccount.class,
                  DestinationAccount.class );

            module.addServices(
                  MemoryEntityStoreService.class,
                  UuidIdentityGeneratorService.class );
         }
      };

      bootstrapData( assembler );

      UnitOfWork uow = assembler.unitOfWorkFactory().newUnitOfWork();
      TransientBuilderFactory tbf = assembler.module().transientBuilderFactory();

      System.out.println( "MONEY TRANSFER v4 - Pay Bills" );

      // Run use case
      PayBillsContext context = tbf.newTransient( PayBillsContext.class );
      context.init( "CheckingAccountId" );
      context.run();

      uow.discard();
   }

   private static void bootstrapData( SingletonAssembler assembler ) throws Exception
   {
      UnitOfWork uow = assembler.unitOfWorkFactory().newUnitOfWork( newUsecase( "Bootstrap data" ) );
      try
      {
         CheckingAccountEntity checkingAccount = uow.newEntity( CheckingAccountEntity.class, "CheckingAccountId" );
         checkingAccount.increaseBalance( 800 );

         CreditorEntity bakerAccount = uow.newEntity( CreditorEntity.class, "BakerAccountId" );
         bakerAccount.allowedMinimum().set( -200 );
         bakerAccount.decreaseBalance( 50 );

         CreditorEntity butcherAccount = uow.newEntity( CreditorEntity.class, "ButcherAccountId" );
         butcherAccount.allowedMinimum().set( -200 );
         butcherAccount.decreaseBalance( 90 );

         // Save
         uow.complete();
      } catch (Exception e)
      {
         e.printStackTrace();
         uow.discard();
      }
   }
}