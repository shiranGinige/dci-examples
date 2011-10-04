/*
 * Copyright (c) 2010 Ant Kutschera, maxant
 * 
 * This file is part of Ant Kutschera's blog.
 * 
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the Lesser GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Lesser GNU General Public License for more details.
 * You should have received a copy of the Lesser GNU General Public License
 * along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package ch.maxant.oopsoadci_common.bankingexample.ccc;

import java.security.Principal;

import ch.maxant.oopsoadci_common.bankingexample.model.Roles;


/**
 * cross cutting concern. a mock implementation for use in the OOP and DCI examples.
 */
public class SecurityDomainMock implements SecurityDomain {

    public boolean isUserInRole(String role) {

        // TODO actually do some real checks against the directory server
        return role.equals(Roles.CUSTOMER);
    }

    /**
     * @return  the logged in user, or null, if they are not logged in
     */
    public Principal getUser() {

        // TODO actually return the authenticated user!
        return new Principal() {

                public String getName() {
                    return "maxant@maxant.co.uk";
                }
            };
    }
}
