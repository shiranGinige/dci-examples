package v6.domain.rolemap;

import v6.domain.context.FrontloadContext;
import v6.domain.entity.ProjectEntity;

/**
 * Roles that a Project can play
 */
public interface ProjectRolemap
   extends ProjectEntity,

      // Methodful Roles
      FrontloadContext.FrontloaderRole
{
}
