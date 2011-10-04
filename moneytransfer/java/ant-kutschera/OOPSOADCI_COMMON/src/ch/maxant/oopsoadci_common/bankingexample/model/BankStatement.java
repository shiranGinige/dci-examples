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
package ch.maxant.oopsoadci_common.bankingexample.model;

import java.util.ArrayList;
import java.util.List;

/**
 * an object used for displaying the balance and transactions related to a
 * customers account.  this is not part of the domain model, but is indeed 
 * part of the users mental model, when he talks about how an account is 
 * viewed in on the screen.
 * it is also a "View Model", part of an extension to MVC, ie its the model of 
 * what is displayed, rather than the domain/business model.
 */
public class BankStatement {

	private double balance;
	
	private Integer account;
	
	private List<MoneyTransaction> transactions = new ArrayList<MoneyTransaction>();

	public BankStatement(double balance, Integer account) {
		super();
		this.balance = balance;
		this.account = account;
	}

	public double getBalance() {
		return balance;
	}

	/** to which account does this sheet belong? */
	public Integer getAccount() {
		return account;
	}

	/** which transactions have occurred on this account */
	public List<MoneyTransaction> getTransactions() {
		return transactions;
	}
	
}
