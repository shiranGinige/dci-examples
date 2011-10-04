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

package v01;

import org.qi4j.api.composite.TransientBuilderFactory;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.spi.uuid.UuidIdentityGeneratorService;
import v01.behavior.context.TransferMoneyContext;
import v01.behavior.role.MoneySinkRole;
import v01.behavior.role.MoneySourceRole;
import v01.domain.entity.CheckingAccountEntity;
import v01.domain.entity.SavingsAccountEntity;

import static org.qi4j.api.usecase.UsecaseBuilder.newUsecase;

/**
 * Java/Qi4j sample program for DCI context execution
 */
public class Main
{
   public static void main( String[] args ) throws Exception
   {
      // Create Qi4j application with one layer and one module
      SingletonAssembler assembler = new SingletonAssembler()
      {
         public void assemble( ModuleAssembly module ) throws AssemblyException
         {
            module.addEntities(
                  SavingsAccountEntity.class,
                  CheckingAccountEntity.class );

            module.addTransients(
                  TransferMoneyContext.class,
                  MoneySourceRole.class,
                  MoneySinkRole.class );

            // services for persisting entities
            module.addServices(
                  MemoryEntityStoreService.class,
                  UuidIdentityGeneratorService.class );
         }
      };

      bootstrapData( assembler );

      UnitOfWork uow = assembler.unitOfWorkFactory().newUnitOfWork();
      TransientBuilderFactory tbf = assembler.module().transientBuilderFactory();

      System.out.println( "MONEY TRANSFER v1" );

      // Run use case
      TransferMoneyContext context = tbf.newTransient( TransferMoneyContext.class );
      context.init( 500, "SavingsAccountId", "CheckingAccountId" );
      context.run();

      uow.discard();
   }

   private static void bootstrapData( SingletonAssembler assembler ) throws Exception
   {
      UnitOfWork uow = assembler.unitOfWorkFactory().newUnitOfWork( newUsecase( "Bootstrap data" ) );
      try
      {
         SavingsAccountEntity savingsAccount = uow.newEntity( SavingsAccountEntity.class, "SavingsAccountId" );
         savingsAccount.increaseBalance( 800 );

         uow.newEntity( CheckingAccountEntity.class, "CheckingAccountId" );

         // Save
         uow.complete();
      } catch (Exception e)
      {
         e.printStackTrace();
         uow.discard();
      }
   }
}