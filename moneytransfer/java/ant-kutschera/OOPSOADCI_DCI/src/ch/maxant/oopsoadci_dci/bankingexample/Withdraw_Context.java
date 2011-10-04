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
import ch.maxant.oopsoadci_common.bankingexample.util.DBHelper;
import ch.maxant.oopsoadci_dci.bankingexample.ccc.SecurityContextMock;
import ch.maxant.oopsoadci_dci.util.BehaviourInjector;

/**
 * this is the context part of DCI and maps the methodless roles to the domain objects and 
 * injects methodful role methods into the role objects.
 * <br><br>
 * this particular instance is used for withdrawing money from a source account.
 */
public class Withdraw_Context {

	private final BigDecimal amount;
	private DBHelper dbHelper;
	private BankAccount sourceAccount;

	/**
	 * why doesnt this object get passed a {@link BankAccount} object? why are
	 * we working with integers / ids here? many reasons! mainly because we dont
	 * pass persistence objects up to the client - its bad policy.  and as such, we cant pass them
	 * back down again. so we need to look them up.  thats done in the {@link #doIt()} method.
	 */
	public Withdraw_Context(Integer sourceAccountId, BigDecimal amount, DBHelper dbHelper) {

		this.amount = amount;
		this.dbHelper = dbHelper;
		
		//get hold of the domain model, by loading it from the DB.
		sourceAccount = dbHelper.getBankAccount(sourceAccountId);
	}

	public void doIt() throws InsufficientFundsException {

		// prepare the injector and add some resources
		BehaviourInjector behaviourInjector = new BehaviourInjector();
		behaviourInjector.addResource("em", dbHelper.getEntityManager());
		behaviourInjector.addResource("sc", new SecurityContextMock());
		
		// convert the domain object into a role, and inject the relevant role methods into it
		ISourceAccount_Role source = behaviourInjector.inject(
				sourceAccount, //domain object
				SourceAccount_Role.class, //class providing all the impl
				ISourceAccount_Role.class); //the entire role impl

		//now do the withdraw interaction, get some cash and go shopping!
		source.withdraw(amount);
	}

}
