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
import ch.maxant.oopsoadci_common.bankingexample.data.LedgerEntry;
import ch.maxant.oopsoadci_common.bankingexample.data.LedgerEntry.LedgerEntrySide;
import ch.maxant.oopsoadci_dci.util.BehaviourInjector;
import ch.maxant.oopsoadci_dci.util.Self;


/**
 * This is the implementation of the role DestinationAccount, ie an account
 * which knows how to deposit and transferFrom.
 */
public class DestinationAccount_Role 
		extends AbstractLedgerEntryCreator {
    
	/** injected by the {@link BehaviourInjector}. */
    @Self
    protected IDestinationAccount_Role self;
        	
	/** temp ref to object, in case a sub class needs it */
	protected LedgerEntry ledgerEntry;
	

	/** 
	 * deposit money into account and write ledger entry.
	 */
    public void deposit(BigDecimal amount) {
		
		self.increaseBalance(amount);

		String comment = "Deposit";
		this.ledgerEntry = createLedgerEntry(self.getUid(), amount, self.getBalance(), LedgerEntrySide.CREDIT, comment);
    }
    

	/** 
	 * first deposit money, then update ledger entry.  assume, that there is no clearing time - the maxant
	 * bank is realtime (ie no long lived transaction)!
	 */
    public void transferFrom(BigDecimal amount, ISourceAccount_Role source) throws InsufficientFundsException {

        //withdraw and log
        deposit(amount);

        //update ledger because this is a transfer, not a simple deposit
        ledgerEntry.setComment("Transfer from " + source.getUid());
    }

}
