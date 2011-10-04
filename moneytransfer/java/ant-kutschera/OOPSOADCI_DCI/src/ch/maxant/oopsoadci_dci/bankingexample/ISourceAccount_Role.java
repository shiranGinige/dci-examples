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
package ch.maxant.oopsoadci_dci.bankingexample;

import java.math.BigDecimal;

import ch.maxant.oopsoadci_common.bankingexample.controller.InsufficientFundsException;
import ch.maxant.oopsoadci_common.bankingexample.data.BankAccount;
import ch.maxant.oopsoadci_common.bankingexample.data.Party;

/**
 * this is a DCI methodless role.  it contains methods which are in the domain model, which 
 * are needed in the role impl, as well as methods which dont exist in the domain model.
 */
public interface ISourceAccount_Role {

    /** @see BankAccount#getParty() */
	Party getParty();
    
    /** @see BankAccount#hasAvailableFunds(BigDecimal) */
	boolean hasAvailableFunds(BigDecimal amount);
    
    /** @see BankAccount#decreaseBalance(BigDecimal) */
	void decreaseBalance(BigDecimal amount);
    
    /** @see BankAccount#getBalance() */
	double getBalance();

    /** @see BankAccount#getUid() */
	public int getUid();

	/** @see SourceAccount_Role#transferTo(BigDecimal, DestinationAccount_Role) */
    public void transferTo(BigDecimal amount, IDestinationAccount_Role destination) throws InsufficientFundsException;

    /** @see SourceAccount_Role#withdraw(BigDecimal) */
    public void withdraw(BigDecimal amount) throws InsufficientFundsException;

}
