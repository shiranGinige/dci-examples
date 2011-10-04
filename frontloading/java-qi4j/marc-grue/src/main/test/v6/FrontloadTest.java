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

package v6;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.spi.uuid.UuidIdentityGeneratorService;
import v6.api.ContextInjectionProviderFactory;
import v6.domain.data.Activity;
import v6.domain.data.Project;
import v6.domain.entity.ActivityEntity;
import v6.domain.rolemap.ProjectRolemap;
import v6.domain.context.FrontloadContext;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.qi4j.api.entity.EntityReference.getEntityReference;
import static org.qi4j.api.usecase.UsecaseBuilder.newUsecase;


public class FrontloadTest
{
   private static SingletonAssembler assembler;
   public static final String PROJECT = "Project";

   @BeforeClass
   public static void setup() throws Exception
   {
      assembler = new SingletonAssembler()
      {
         public void assemble( ModuleAssembly module ) throws AssemblyException
         {
            module.layerAssembly().applicationAssembly().setMetaInfo( new ContextInjectionProviderFactory() );

            module.addEntities(
                  ProjectRolemap.class,
                  ActivityEntity.class    // Is playing no (methodful) roles in this version
            );

            module.addServices(
                  MemoryEntityStoreService.class,
                  UuidIdentityGeneratorService.class );
         }
      };

      buildDemoNetwork( assembler );

      System.out.println( "FRONTLOADING TESTS - v6 \n========================================================" );
   }

   @After
   public void printResult() throws Exception
   {
      UnitOfWork uow = assembler.unitOfWorkFactory().newUnitOfWork( newUsecase( "Print Gantt chart" ) );

      try
      {
         List<Activity> activities = uow.get( Project.class, PROJECT ).allActivities();

         StringBuilder sb = new StringBuilder();
         sb.append( "\n\n### Gantt chart showing planned activities:\n" );
         Collections.sort( activities, new Comparator<Activity>()
         {
            public int compare( Activity a1, Activity a2 )
            {
               if (a1.earlyStart().get().equals( a2.earlyStart().get() ))
                  return getEntityReference( a1 ).identity().compareTo( getEntityReference( a2 ).identity() );
               else
                  return a1.earlyStart().get() - a2.earlyStart().get();
            }
         } );

         for (Activity activity : activities)
         {
            String id = EntityReference.getEntityReference( activity ).identity();
            Integer start = activity.earlyStart().get();
            Integer duration = activity.duration().get();
            for (int i = 1; i < start; i++)
            {
               sb.append( "  " );
            }
            sb.append( id );
            for (int i = 0; i < duration - 1; i++)
            {
               sb.append( " =" );
            }
            sb.append( "\n" );
         }

         sb.append( "1 2 3 4 5 6 7 8 9" );
         System.out.println( sb.toString() );
      }
      finally
      {
         uow.discard();
      }
      System.out.println( "----------------------------------------------------------" );
   }

   private static void buildDemoNetwork( SingletonAssembler assembler ) throws Exception
   {
      UnitOfWork uow = assembler.unitOfWorkFactory().newUnitOfWork( newUsecase( "Build demo network of activities" ) );
      try
      {
         // try different combinations here...

         ActivityEntity a = uow.newEntity( ActivityEntity.class, "A" );
         ActivityEntity b = uow.newEntity( ActivityEntity.class, "B" );
         ActivityEntity c = uow.newEntity( ActivityEntity.class, "C" );
         ActivityEntity d = uow.newEntity( ActivityEntity.class, "D" );

         a.duration().set( 2 );
         b.duration().set( 7 );
         c.duration().set( 3 );
         d.duration().set( 2 );

         a.succeededBy( c, d );

         // Try different variations:
         b.succeededBy( d );     // "Original"
//         b.succeededBy( c );     // Variation
//         c.succeededBy( a );     // "C" already planned
//         d.succeededBy( a );     // Cyclic dependency

         Project project = uow.newEntity( Project.class, PROJECT );
//         project.addActivities( a, b, c, d );
         project.addActivities( d, c, b, a ); // More interesting execution flow

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
   public void planActivitiesTest() throws Exception
   {
      UnitOfWork uow = assembler.unitOfWorkFactory().newUnitOfWork( UsecaseBuilder.newUsecase( "Test" ) );

      try
      {
         Project project = uow.get( Project.class, PROJECT );

         new FrontloadContext( project ).frontloadNetworkFrom( 1 ); // try 2

         uow.complete();
      }
      catch (Exception e)
      {
         uow.discard();
         throw e;
      }
   }
}
