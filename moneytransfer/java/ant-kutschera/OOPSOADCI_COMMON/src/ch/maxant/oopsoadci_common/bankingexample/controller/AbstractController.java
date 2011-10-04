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
package ch.maxant.oopsoadci_common.bankingexample.controller;

import java.math.BigDecimal;

import javax.persistence.EntityTransaction;

import ch.maxant.oopsoadci_common.bankingexample.data.BankAccount;
import ch.maxant.oopsoadci_common.bankingexample.data.LedgerEntry;
import ch.maxant.oopsoadci_common.bankingexample.data.LedgerEntry.LedgerEntrySide;
import ch.maxant.oopsoadci_common.bankingexample.model.BankStatement;
import ch.maxant.oopsoadci_common.bankingexample.model.MoneyTransaction;
import ch.maxant.oopsoadci_common.bankingexample.util.DBHelper;

/**
 * controller in terms of MVC. used in OOP and DCI examples.
 */
public abstract class AbstractController {
	
	protected DBHelper dbHelper;

	public AbstractController(){
		dbHelper = new DBHelper();
	}

	/**
	 * transfer money from the source account to the destination account.  
	 * does things like checking security and creating RELEVANT ledger entries too.
	 */
	public abstract void transfer(Integer sourceAccountId,
			Integer destinationAccountId, BigDecimal amount)
			throws InsufficientFundsException;

	/**
	 * withdraw money from the given account.  does things like checking 
	 * security and creating ledger entries too.
	 */
	public abstract void withdraw(Integer sourceAccountId, BigDecimal amount)
			throws InsufficientFundsException;

	/**
	 * method for loading the newest system state from the DB, and preparing it for
	 * presentation, ie mapping to MVC Model, rather than domain model.
	 */
	public BankStatement getBankStatement(Integer accountId){
	
		EntityTransaction tx = dbHelper.begin();
	
		// now return the balance sheet for the newly updated account
		BankAccount source = dbHelper.getBankAccount(accountId);
		BankStatement bs = new BankStatement(source.getBalance(), accountId);
		for (LedgerEntry le : source.getLedgerEntries()) {
			
			double am = le.getAmount();
			if(le.getSide().equals(LedgerEntrySide.DEBIT)){
				//show transfers out of the account as negative
				am *= -1.0;
			}
			
			MoneyTransaction mt = 
				new MoneyTransaction(
						le.getWhenDt(),
						am, 
						le.getNewBalance(),
						le.getComment());
			bs.getTransactions().add(mt);
			
		}
		
		tx.commit();
		
		return bs;
	}
	
}

