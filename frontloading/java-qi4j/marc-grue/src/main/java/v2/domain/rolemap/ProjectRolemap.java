package v2.domain.rolemap;

import v2.domain.entity.ProjectEntity;
import v2.domain.context.FrontLoadContext;

/**
 * Roles that a Project can play
 */
public interface ProjectRolemap
   extends ProjectEntity,

      // Roles
      FrontLoadContext.FrontLoaderRole,
      FrontLoadContext.AllActivitiesRole
{
}
