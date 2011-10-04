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

import java.util.Date;

import ch.maxant.oopsoadci_common.bankingexample.data.LedgerEntry;

/**
 * part of the views model. ie the Model in MVC. similar to a {@link LedgerEntry}.
 * 
 * @see BankStatement
 */
public class MoneyTransaction {

	private Date transferDate;
	private double amount;
	private double newBalance;
	private String comment;

	public MoneyTransaction(Date transferDate, double amount, double newBalance, String comment) {
		this.transferDate = transferDate;
		this.amount = amount;
		this.newBalance = newBalance;
		this.comment = comment;
	}

	public Date getTransferDate() {
		return transferDate;
	}

	public double getAmount() {
		return amount;
	}
	
	public double getNewBalance() {
		return newBalance;
	}
	
	public String getComment() {
		return comment;
	}

}
