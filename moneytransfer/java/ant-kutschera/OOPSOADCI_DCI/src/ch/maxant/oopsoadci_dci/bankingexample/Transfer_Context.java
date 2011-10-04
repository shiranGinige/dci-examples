/*
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
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
 * this particular instance is used for transferring money from a source account to a destination account.
 */
public class Transfer_Context {
	
	private final BigDecimal amount;
	private DBHelper dbHelper;
	private BankAccount destinationAccount;
	private BankAccount sourceAccount;

	/**
	 * why doesnt this object get passed a {@link BankAccount} object? why are
	 * we working with integers / ids here? many reasons! mainly because we dont
	 * pass persistence objects up to the client - its bad policy.  and as such, we cant pass them
	 * back down again. so we need to look them up.  thats done in the {@link #doIt()} method.
	 */
	public Transfer_Context(Integer sourceAccountId, Integer destinationAccountId,
			BigDecimal amount, DBHelper dbHelper) {

		this.amount = amount;
		this.dbHelper = dbHelper;
		
		//get hold of the domain model, by loading it from the DB.
		sourceAccount = dbHelper.getBankAccount(sourceAccountId);
		destinationAccount = dbHelper.getBankAccount(destinationAccountId);
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

		// convert the domain object into a role, and inject the relevant role methods into it
		IDestinationAccount_Role destination = behaviourInjector.inject(
				destinationAccount, //domain object
				DestinationAccount_Role.class, //class providing all the impl
				IDestinationAccount_Role.class); //the entire role impl

		//do the transfer interaction!
		source.transferTo(amount, destination);
	}

}
