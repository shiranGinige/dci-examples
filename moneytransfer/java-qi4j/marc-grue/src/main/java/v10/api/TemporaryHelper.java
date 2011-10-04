package v10.api;

import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;

/**
 * Javadoc
 */
public class TemporaryHelper
{

   private static UnitOfWorkFactory uowf;
   private static QueryBuilderFactory qbf;


   public static void  setUowf(UnitOfWorkFactory unitOfWorkFactory)
   {
      uowf = unitOfWorkFactory;
   }

   public static UnitOfWorkFactory uowf()
   {
      return uowf;
   }

   public static void  setQbf(QueryBuilderFactory queryBuilderFactory)
   {
      qbf = queryBuilderFactory;
   }

   public static QueryBuilderFactory qbf()
   {
      return qbf;
   }




   // Temporary helper method to make the use case algorithm clear in the roles
   // (wouldn't be here in a real project)

   public static void log( String msg )
   {
      System.out.println( msg );
   }
}
