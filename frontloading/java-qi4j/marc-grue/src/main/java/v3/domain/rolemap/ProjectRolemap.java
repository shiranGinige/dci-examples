package v3.domain.rolemap;

import v3.domain.entity.ProjectEntity;
import v3.domain.context.FrontloadContext;

/**
 * Roles that a Project can play
 */
public interface ProjectRolemap
   extends ProjectEntity,

      // Methodful Roles
      FrontloadContext.FrontloaderRole
{
}
