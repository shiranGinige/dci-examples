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

package v07.api;

import java.util.Stack;

/**
 * Maintains a stack of active contexts. Lookup finds the closest context of the given type on the stack.
 */
public class Contexts
{
   public static Stack<Object> contexts = new Stack<Object>();

   // Access a context of a particular type on the stack
   public static <T> T context( Class<T> contextClass )
   {
      for (int i = contexts.size() - 1; i >= 0; i--)
         if (contextClass.isInstance( contexts.get( i ) ))
            return (T) contexts.get( i );

      throw new IllegalArgumentException( "No " + contextClass.getName() + " on the stack" );
   }

   // Perform a query with the given context
   public static <T, CONTEXT, EX extends Throwable> T withContext( CONTEXT context, Query<T, CONTEXT, EX> enactment ) throws EX
   {
      contexts.push( context );
      try
      {
         return enactment.query( context );
      } finally
      {
         contexts.pop();
      }
   }

   // Perform a command with the given context
   public static <CONTEXT, EX extends Throwable> void withContext( CONTEXT context, Command<CONTEXT, EX> enactment ) throws EX
   {
      contexts.push( context );
      try
      {
         enactment.command( context );
      } finally
      {
         contexts.pop();
      }
   }

   // Query enactments must implement this interface
   public interface Query<T, CONTEXT, EX extends Throwable>
   {
      T query( CONTEXT context )
            throws EX;
   }

   // Command enactments must implement this interface
   public interface Command<CONTEXT, EX extends Throwable>
   {
      void command( CONTEXT context )
            throws EX;
   }

}
