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
package ch.maxant.oopsoadci_dci.bankingexample.ccc;

import ch.maxant.oopsoadci_common.bankingexample.ccc.SecurityDomain;
import ch.maxant.oopsoadci_common.bankingexample.ccc.SecurityDomainMock;
import ch.maxant.oopsoadci_common.bankingexample.model.Roles;
import ch.maxant.oopsoadci_dci.bankingexample.ISourceAccount_Role;

/**
 * mock impl of a security context, as used in OOP and DCI examples.
 * 
 * @see SecurityContext
 */
public class SecurityContextMock implements SecurityContext {

    public void checkSecurityForTransfer(ISourceAccount_Role account) {
        SecurityDomain sec = new SecurityDomainMock();

        if (!sec.isUserInRole(Roles.CUSTOMER)) {
            throw new SecurityException("Wrong role!");
        }

        if (!sec.getUser().getName().equals(account.getParty().getLogin())) {
            throw new SecurityException("Not account holder");
        }
    }

}
