package v4.domain.rolemap;

import v4.domain.entity.ProjectEntity;
import v4.domain.context.FrontloadContext;

/**
 * Roles that a Project can play
 */
public interface ProjectRolemap
   extends ProjectEntity,

      // Methodful Roles
      FrontloadContext.FrontloaderRole
{
}
