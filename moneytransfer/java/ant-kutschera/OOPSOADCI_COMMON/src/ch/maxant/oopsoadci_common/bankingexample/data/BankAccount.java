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
package ch.maxant.oopsoadci_common.bankingexample.data;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.*;

import org.eclipse.persistence.annotations.Cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Comparator;
import java.util.Set;


/**
 * The persistent class for the account database table.
 * domain object, but also contains a little logic for manipulating
 * data.  note this logic is ONLY related to the class state, not to 
 * state from any other class.
 */
@Entity
@Table(name="account")
@Cache(alwaysRefresh=true) //otherwise, LedgerEntries are not loaded correctly
public class BankAccount implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private int uid;

	private double balance;

	//bi-directional many-to-one association to Party
    @ManyToOne
	@JoinColumn(name="customer")
	private Party party;

	//bi-directional many-to-one association to LedgerEntry
	@OneToMany(mappedBy="account")
	private Set<LedgerEntry> ledgerEntries;

	public int getUid() {
		return this.uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public double getBalance() {
		return this.balance;
	}

	public Party getParty() {
		return this.party;
	}

	public void setParty(Party party) {
		this.party = party;
	}
	
	/** @return a NEW list of all ledger entries for this account, sorted by date */
	public List<LedgerEntry> getLedgerEntries() {
		List<LedgerEntry> les = new ArrayList<LedgerEntry>();
		les.addAll(this.ledgerEntries);
		Collections.sort(les, new Comparator<LedgerEntry>() {
			public int compare(LedgerEntry le1, LedgerEntry le2) {
				return le1.getWhenDt().compareTo(le2.getWhenDt());
			}
		});
		return les;
	}

	/** semi intelligent behaviour, but importantly only operates on own data */
	public void decreaseBalance(BigDecimal amount) {
		this.balance -= amount.doubleValue();
	}

	/** semi intelligent behaviour, but importantly only operates on own data */
	public void increaseBalance(BigDecimal amount) {
		this.balance += amount.doubleValue();
	}
	
	/** semi intelligent behaviour, but importantly only operates on own data */
	public boolean hasAvailableFunds(BigDecimal amount) {
		return this.balance > amount.doubleValue();
	}

}