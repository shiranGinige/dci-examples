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
package ch.maxant.oopsoadci_common.bankingexample.view;

import java.math.BigDecimal;

import ch.maxant.oopsoadci_common.bankingexample.controller.AbstractController;
import ch.maxant.oopsoadci_common.bankingexample.controller.InsufficientFundsException;
import ch.maxant.oopsoadci_common.bankingexample.model.BankStatement;
import ch.maxant.oopsoadci_common.bankingexample.model.MoneyTransaction;

/**
 * abstract class for running examples.  this is effectively the "View" in MVC.
 */
public abstract class AbstractMain {

	/** runs the test case */
	protected void run(){
		try {
			AbstractController controller = getController();
			
			controller.transfer(1, 2, new BigDecimal(1000.0));
			System.out.println("Transfer complete.");

			controller.withdraw(1, new BigDecimal(500.0));
			System.out.println("Withdrawal complete.");
			
			BankStatement bs = controller.getBankStatement(1);
			
			System.out.println("Account " + bs.getAccount() + ", Balance: " + bs.getBalance() + " CHF");
			for(MoneyTransaction mt : bs.getTransactions()){
				System.out.println("\tDate: " + mt.getTransferDate() + "\tAmount: " + 
						mt.getAmount() + "\tBalance: " + mt.getNewBalance() + 
						" CHF\tComment: " + mt.getComment());
			}
		
		} catch (InsufficientFundsException e) {
			System.err.println("Error: " + e);
		} catch (SecurityException e) {
			System.err.println("Error: " + e);
		}
	}
	
	/**
	 * @return the controller to use.
	 */
	protected abstract AbstractController getController();
}
