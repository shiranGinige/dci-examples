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

import javax.annotation.Resource;

import ch.maxant.oopsoadci_common.bankingexample.controller.InsufficientFundsException;
import ch.maxant.oopsoadci_common.bankingexample.data.LedgerEntry;
import ch.maxant.oopsoadci_common.bankingexample.data.LedgerEntry.LedgerEntrySide;
import ch.maxant.oopsoadci_dci.bankingexample.ccc.SecurityContext;
import ch.maxant.oopsoadci_dci.util.BehaviourInjector;
import ch.maxant.oopsoadci_dci.util.Self;


/**
 * This is the implementation of the role SourceAccount, ie an account
 * which knows how to withdraw money and transferTo.
 */
public class SourceAccount_Role 
		extends AbstractLedgerEntryCreator {

	/** injected by the {@link BehaviourInjector}. */
    @Self
    protected ISourceAccount_Role self;
        	
    /** injected by the {@link BehaviourInjector}. */
	@Resource(name="sc")
	protected SecurityContext sc;

	private LedgerEntry ledgerEntry;

    /** 
	 * first withdraw money, then update ledger entry and then deposit the money in the destination account.
	 * assume, that there is no clearing time - the maxant bank is realtime (ie no long lived transaction)!
     */
    public void transferTo(BigDecimal amount, IDestinationAccount_Role destination) throws InsufficientFundsException {

        //withdraw and log
        withdraw(amount);

        //update ledger because this is a transfer, not a simple withdrawl
        ledgerEntry.setComment("Transfer to " + destination.getUid());
        
        //deposit and log
        destination.deposit(amount);
    }

	/** 
	 * check its the logged in users account, check the account has available funds,
	 * withdraw money from the account and write ledger entry. 
	 */
    public void withdraw(BigDecimal amount) throws InsufficientFundsException {
		
    	//check security
        sc.checkSecurityForTransfer(self);
    	
		//check funds
		if(!self.hasAvailableFunds(amount)){
			throw new InsufficientFundsException();
		}

		//decrease balance
		self.decreaseBalance(amount);

		//create ledger entry
		String comment = "Withdrawal";
		this.ledgerEntry = createLedgerEntry(self.getUid(), amount, self.getBalance(), LedgerEntrySide.DEBIT, comment);
    }
    
}
