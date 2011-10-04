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
import javax.persistence.*;
import java.util.Date;
import java.util.EnumSet;


/**
 * The persistent class for the ledger_entry database table.
 * domain object, but also contains a little logic for manipulating
 * data.  note this logic is ONLY related to the class state, not to 
 * state from any other class.
 */
@Entity
@Table(name="ledger_entry")
public class LedgerEntry implements Serializable {
	private static final long serialVersionUID = 1L;

	/** enumeration to capture possible values of the "side" of a ledger entry. */
	public enum LedgerEntrySide {
		CREDIT('c'),
		DEBIT('d');
		
		private char value;
		
		private LedgerEntrySide(char value){
			this.value = value;
		}
		
		public char getValue() {
			return value;
		}
		
		public static LedgerEntrySide find(char c){
			for(LedgerEntrySide les : EnumSet.allOf(LedgerEntrySide.class)){
				if(les.value == c){
					return les;
				}
			}
			return null;
		}
	}
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private int uid;

	private double amount;

	@Column(name="new_balance")
	private double newBalance;

	private String side;

	private String comment;

    @Temporal( TemporalType.TIMESTAMP)
	@Column(name="when_dt")
	private Date whenDt;

	//bi-directional many-to-one association to Account
    @ManyToOne
	@JoinColumn(name="account")
	private BankAccount account;

    public LedgerEntry() {
    }

	public int getUid() {
		return this.uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public double getAmount() {
		return this.amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public double getNewBalance() {
		return this.newBalance;
	}

	public void setNewBalance(double newBalance) {
		this.newBalance = newBalance;
	}

	public LedgerEntrySide getSide() {
		return LedgerEntrySide.find(this.side.charAt(0));
	}

	public void setSide(LedgerEntrySide side) {
		this.side = String.valueOf(side.getValue());
	}

	public String getComment() {
		return comment;
	}
	
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	public Date getWhenDt() {
		return this.whenDt;
	}

	public void setWhenDt(Date whenDt) {
		this.whenDt = whenDt;
	}

	public BankAccount getAccount() {
		return this.account;
	}

	public void setAccount(BankAccount account) {
		this.account = account;
	}
	
}