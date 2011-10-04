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
import java.util.Date;

import javax.annotation.Resource;
import javax.persistence.EntityManager;

import ch.maxant.oopsoadci_common.bankingexample.data.BankAccount;
import ch.maxant.oopsoadci_common.bankingexample.data.LedgerEntry;
import ch.maxant.oopsoadci_common.bankingexample.data.LedgerEntry.LedgerEntrySide;
import ch.maxant.oopsoadci_dci.util.BehaviourInjector;

/**
 * a simple superclass able to create ledger entries.  the idea is that roles needing to be able to
 * do that, would extend this class.
 */
public abstract class AbstractLedgerEntryCreator {

	/** injected by the {@link BehaviourInjector} */
	@Resource(name="em")
	protected EntityManager em;
	
	/** 
	 * creates and persists a ledger entry
	 */
	protected LedgerEntry createLedgerEntry(int accountId, BigDecimal amount, double newBalance, LedgerEntrySide side, String comment) {
		//need to create an object, so that JPA can set the foreign key
		BankAccount account = new BankAccount();
		account.setUid(accountId);

		LedgerEntry le = new LedgerEntry();
		le.setAccount(account);
		le.setAmount(amount.doubleValue());
		le.setNewBalance(newBalance);
		le.setSide(side);
		le.setWhenDt(new Date());
		le.setComment(comment);

		em.persist(le);
		
		return le;
	}
}
