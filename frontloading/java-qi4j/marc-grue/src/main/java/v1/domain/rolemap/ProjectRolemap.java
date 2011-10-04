package v1.domain.rolemap;

import v1.domain.entity.ProjectEntity;
import v1.domain.context.FrontLoadContext;

/**
 * Roles that a Project can play
 */
public interface ProjectRolemap
   extends ProjectEntity,

      // Roles
      FrontLoadContext.FrontLoaderRole
{
}
