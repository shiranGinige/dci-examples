/**
 *
 * Copyright 2010 Marc Grue
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package v06.api;

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Role-to-EntityObject mapping
 */
@Mixins( RoleMap.Mixin.class )
public interface RoleMap
      extends ServiceComposite
{
   public void set( Object object, Class... roleClasses );
   public <T> T get( Class<T> RoleClass ) throws IllegalArgumentException;

   abstract class Mixin
         implements RoleMap
   {
      private HashMap<Class, Object> roles = new HashMap<Class, Object>();

      public void set( Object object, Class... roleClasses )
      {
         if (object instanceof Class)
            throw new IllegalArgumentException( "Object '" + object + "' can't be of type Class" );

         if (object == null)
         {
            for (Class roleClass : roleClasses)
            {
               roles.remove( roleClass );
            }
         }
         else
         {
            if (roleClasses.length == 0)
            {
               put( object.getClass(), object );
            }
            else
            {
               for (Class roleClass : roleClasses)
               {
                  put( roleClass, object );
               }
            }
         }
      }

      private void put( Class roleClass, Object object )
      {
         if (roleClass.isAssignableFrom( object.getClass() ))
            roles.put( roleClass, object );
         else
            throw new IllegalArgumentException( "Object '" + object.getClass().getSimpleName() +
                  "' can't play Role of '" + roleClass.getSimpleName() + "'" );
      }

      public <T> T get( Class<T> roleClass ) throws IllegalArgumentException
      {
         Object object = roles.get( roleClass );

         if (object != null)
            return roleClass.cast( object );

         T rolePlayer = null;

         // If no explicit mapping has been made, see if
         // a unique object maps to the role
         List<Class> roleCandidates = new ArrayList<Class>();
         for (Map.Entry<Class, Object> entry : roles.entrySet())
         {
            if (roleClass.isAssignableFrom( entry.getKey() ))
            {
               roleCandidates.add( entry.getKey() );
               rolePlayer = roleClass.cast( entry.getValue() );
            }
            if (roleCandidates.size() > 1)
               throw new IllegalArgumentException( "Ambiguous role binding - Role of '"
                     + roleClass.getSimpleName() + "' is played by at least: "
                     + roleCandidates.toString()
               );
         }

         if (rolePlayer == null)
            throw new IllegalArgumentException( "No object in roleMap for role: " + roleClass.getSimpleName() );

         return rolePlayer;
      }
   }
}