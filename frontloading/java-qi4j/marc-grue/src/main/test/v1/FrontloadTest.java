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

package v1;

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
import v1.api.ContextInjectionProviderFactory;
import v1.domain.data.ActivityData;
import v1.domain.data.ProjectData;
import v1.domain.entity.ActivityEntity;
import v1.domain.rolemap.ActivityRolemap;
import v1.domain.rolemap.ProjectRolemap;
import v1.domain.context.FrontLoadContext;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.qi4j.api.entity.EntityReference.getEntityReference;
import static org.qi4j.api.usecase.UsecaseBuilder.newUsecase;

/**
 * Test of frontloader
 *
 * Execution model immitated from:
 *
 * @See http://heim.ifi.uio.no/~trygver/2009/bb4plan.pdf
 *
 * Discussions of the frontloader example:
 * @See http://groups.google.com/group/object-composition/browse_thread/thread/9e2b3e28803fca44/aa8a2d7aa2f4a7cb
 * @See http://groups.google.com/group/object-composition/browse_thread/thread/f2345a1f6d46b5c2/f69d10d813b1cf0d
 *
 * Excellent SmallTalk analysis from Brian (candlerb) in order to understand Trygves example:
 * @See http://groups.google.com/group/object-composition/browse_thread/thread/854df3a328e1c263/5f6985a4cebf86b5
 */
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
                  ActivityRolemap.class );

            module.addServices(
                  MemoryEntityStoreService.class,
                  UuidIdentityGeneratorService.class );
         }
      };

      buildDemoNetwork( assembler );

      System.out.println( "FRONTLOADING TESTS - v1 \n========================================================" );
   }

   @After
   public void printResult() throws Exception
   {
      UnitOfWork uow = assembler.unitOfWorkFactory().newUnitOfWork( newUsecase( "Print Gantt chart" ) );

      try
      {
         List<ActivityData> activities = uow.get( ProjectData.class, PROJECT ).allActivities();

         StringBuilder sb = new StringBuilder();
         sb.append( "\n\n### Gantt chart showing planned activities:\n" );
         Collections.sort( activities, new Comparator<ActivityData>()
         {
            public int compare( ActivityData a1, ActivityData a2 )
            {
               if (a1.earlyStart().get().equals( a2.earlyStart().get() ))
                  return getEntityReference( a1 ).identity().compareTo( getEntityReference( a2 ).identity() );
               else
                  return a1.earlyStart().get() - a2.earlyStart().get();
            }
         } );

         for (ActivityData activity : activities)
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

         a.succeededBy( c );
         c.succeededBy( d );

         // Try different variations:
         b.succeededBy( d );     // "Original"
//         b.succeededBy( c );     // Variation
//         c.succeededBy( a );     // "C" already planned
//         d.succeededBy( a );     // Cyclic dependency

         ProjectData project = uow.newEntity( ProjectData.class, PROJECT );
//         project.addActivities( a, b, c, d );
         project.addActivities( d, c, b, a ); // interesting execution flow!

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
         ProjectData project = uow.get( ProjectData.class, PROJECT );

         new FrontLoadContext( project ).frontloadNetworkFrom( 1 ); // try 2

         uow.complete();
      }
      catch (Exception e)
      {
         uow.discard();
         throw e;
      }
   }
}
