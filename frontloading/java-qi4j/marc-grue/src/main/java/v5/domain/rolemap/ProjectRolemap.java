package v5.domain.rolemap;

import v5.domain.context.FrontloadContext;
import v5.domain.entity.ProjectEntity;

/**
 * Roles that a Project can play
 */
public interface ProjectRolemap
   extends ProjectEntity,

      // Methodful Roles
      FrontloadContext.FrontloaderRole
{
}
