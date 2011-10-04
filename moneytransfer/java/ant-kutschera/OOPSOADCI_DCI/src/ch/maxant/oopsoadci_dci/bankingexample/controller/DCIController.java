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
package ch.maxant.oopsoadci_dci.bankingexample.controller;

import java.math.BigDecimal;

import javax.persistence.EntityTransaction;

import ch.maxant.oopsoadci_common.bankingexample.controller.AbstractController;
import ch.maxant.oopsoadci_common.bankingexample.controller.InsufficientFundsException;
import ch.maxant.oopsoadci_dci.bankingexample.Transfer_Context;
import ch.maxant.oopsoadci_dci.bankingexample.Withdraw_Context;

/**
 * controller in terms of MVC.
 */
public class DCIController extends AbstractController {

	/** {@inheritDoc} */
	public void transfer(Integer sourceAccountId,
			Integer destinationAccountId, BigDecimal amount)
			throws InsufficientFundsException {

		//the transaction is started before the context is run 
		//see http://groups.google.com/group/object-composition/browse_thread/thread/f6e84cb9e327f162
		EntityTransaction tx = dbHelper.begin();
		
		// in DCI, the system behaviour has been extracted from the domain
		// model (bank account, etc) and is captured in roles. Create a context
		// which maps the roles to the data objects and executes the
		// interaction.
		new Transfer_Context(1, 2, amount, dbHelper).doIt();

		tx.commit();
	}

	/** {@inheritDoc} */
	@Override
	public void withdraw(Integer sourceAccountId, BigDecimal amount)
	throws InsufficientFundsException {
		
		//the transaction is started before the context is run 
		//see http://groups.google.com/group/object-composition/browse_thread/thread/f6e84cb9e327f162
		EntityTransaction tx = dbHelper.begin();
		
		// in DCI, the system behaviour has been extracted from the domain
		// model (bank account, etc) and is captured in roles. Create a context
		// which maps the roles to the data objects and executes the
		// interaction.
		new Withdraw_Context(1, amount, dbHelper).doIt();
		
		tx.commit();
	}
	
}
