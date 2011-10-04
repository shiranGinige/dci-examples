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

package v4.api;

import org.qi4j.api.unitofwork.UnitOfWorkFactory;

import java.util.Stack;

/**
 * Maintains a stack of active contexts. Lookup finds the closest context of the given type on the stack.
 */
public class Contexts
{
   private static UnitOfWorkFactory uowf;

   private static ThreadLocal<Stack<Object>> contexts = new ThreadLocal<Stack<Object>>()
   {
      @Override
      protected Stack<Object> initialValue()
      {
         return new Stack<Object>();
      }
   };

   private static Stack<Object> current()
   {
      return contexts.get();
   }

   // Access a context of a particular type on the stack
   public static <T> T context( Class<T> contextClass )
   {
      for (int i = current().size() - 1; i >= 0; i--)
         if (contextClass.isInstance( current().get( i ) ))
            return (T) current().get( i );

      throw new IllegalArgumentException( "No " + contextClass.getName() + " on the stack" );
   }

   // Perform a query with the given context
   public static <T, CONTEXT extends Context, EX extends Throwable> T withContext( CONTEXT context, Query<T, CONTEXT, EX> enactment ) throws EX
   {
      current().push( context );
      try
      {
         // How to handle exceptions ?!
         context.reselectObjectsForRoles();

         return enactment.query( context );
      }
      catch (Throwable e)
      {
         e.printStackTrace();
         return null; // ?
      }
      finally
      {
         current().pop();
      }
   }

   // Perform a command with the given context
   public static <CONTEXT extends Context, EX extends Throwable> void withContext( CONTEXT context, Command<CONTEXT, EX> enactment ) throws EX
   {
      current().push( context );
      try
      {
         // How to handle exceptions ?!
         context.reselectObjectsForRoles();

         enactment.command( context );
      }
      catch (Throwable e)
      {
         e.printStackTrace();
      }
      finally
      {
         current().pop();
      }
   }

   // Query enactments must implement this interface

   public interface Query<T, CONTEXT extends Context, EX extends Throwable>
   {
      T query( CONTEXT context )
            throws EX;
   }

   // Command enactments must implement this interface

   public interface Command<CONTEXT extends Context, EX extends Throwable>
   {
      void command( CONTEXT context )
            throws EX;
   }
}
