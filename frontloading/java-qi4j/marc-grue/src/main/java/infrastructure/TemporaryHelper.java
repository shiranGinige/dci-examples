package infrastructure;

import org.qi4j.api.unitofwork.UnitOfWorkFactory;

/**
 * Javadoc
 */
public class TemporaryHelper
{

   private static UnitOfWorkFactory uowf;

   public static void  setUowf(UnitOfWorkFactory unitOfWorkFactory)
   {
      uowf = unitOfWorkFactory;
   }

   public static UnitOfWorkFactory uowf()
   {
      return uowf;
   }

   public static void log( String msg )
   {
      System.out.print( msg );
   }
}
