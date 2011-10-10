# Money Transfer

12 (!) Java-Qi4j implementations of the money transfer example by Marc Grue (and Rickard Oberg v7-8), October 2010. Comments below are from that time too - much has happened since!

## Comments

After reading Jim and Gertruds excellent Lean Architecture book, I wanted to experiment with and code some of the different approaches described and I ended up with 6 variations of the MoneyTransfer example with java/qi4j. I'm going from simple to more advanced implementations ending with dynamic balance calculation from money transaction entries.

Below you'll find an overview of the different versions and some of the reasoning behind. Might be interesting for others digging into DCI. If you want to skip my introductory steps, you can go straight to version 5 (with static balance) or version 6 (with money transaction entries).

## Discussions

* [[Java/Qi4j Money Transfer example in 6 versions|http://groups.google.com/group/object-composition/browse_thread/thread/643ba009b1648cd0/f8a2acaa703384db?lnk=gst&q=money+transfer#f8a2acaa703384db]]
* [[DCI-Qi4j moneytransfer version 9|http://groups.google.com/group/object-composition/browse_thread/thread/6879bf511184b91b/d330177fa2b55d62?q=]]
* [[Shift of Mental model from Data to Roles - new v10 of Qi4j MoneyTransfer/PayBills|http://groups.google.com/group/object-composition/browse_thread/thread/4f456185c736326b/ca50d714b71fde8a?q=]]


## Versions

### v1

* Simple initial naive implementation with only a Transfer Money use case.
* Entities are retrieved from the UnitOfWork and casted to a Role.
* Names of files/identifiers don't match the use case text (made it before analyzing name discrepencies).

### v2

* Names changed to map directly to the use case (as described in Lean Architecture).
* SavingsAccount and CheckingAccount now extend a common AccountData class.
* Logging moved from Data to Roles - Data shouldn't know about logging needs.

### v3

* API introduced making the Context less cluttered with infrastructure.
* Use case now orchestrated in SourceAccount.
* Explicit post condition check in Context.
* AccountData replaced by BalanceData (simple balance data shouldn't know about accounts).

### v4

* Pay Bills use case introduced. It uses the MoneyTransferContext in a loop _ "nested contexts"?
* Role playing objects are now also passed as method arguments to a Context (and not only data object ids).
* Notice how flexible we can combine data and roles in the three entities and that CheckingAccountEntity can now both play a DestinationAccount and a SourceAccount.
* SourceAccount#payBillsTo(creditors) stays readable as a Use case algorithm by delegating checkAvailableFundsForAllBills(..) into a separate method. TransferMoneyContext initialization inside the loop could also have been splitted out for readability. I think its valuable if a domain expert can immediately understand the meaning of the code in the trigger method that orchestrates the Use case. That should be where we can read and reason about the Use case undisturbed by technical details. 

### v5
* RoleMap introduced. Both Contexts and Roles can access the RoleMap (through ContextMixin and RoleMixin). Is this similar to the 4th type of Context reference described on page 275 in Lean Architecture?
* Role objects are no longer passed as method arguments but looked up within roles.
* No need to save Role objects as properties of the Context anymore.
* Roles now extend RoleMixin to have access to common services/structures. This makes the Roles less cluttered with "infrastructure" elements.

### v6
* BalanceData is now calculated from a more elaborate money transactions model with transactionEntries. I'm using the account domain modelling ideas from Martin Fowlers "Analysis Patterns". This is not a real world example and as such not at all a complete model. Its purpose is to show a dynamic retrieval of the balance for accounts and to demonstrate a more "near-world" example of DCI. "Transactions" here are money transactions - hm, I hope people don't find this confusing.
* SourceAccountRole holds a reference to a Transactions object for balance lookup. The Transactions object holds a private tempTransaction which is a buildup of a money transaction containing TransactionEntries. By the end of the money transfer, all entries of the tempTransaction is check to see if it balances, and if so is saved.

### v7-v8

Coded by Rickard Oberg

### v9-v12

See forum posts...

## Data integrity and access from Role

* v1: MoneySourceRole extends SavingsAccountData directly and therefore exposes Data directly to any client of the role.
* v2: SourceAccount.Mixin has SavingsAccountData injected and saved in the private variable "data".
* v3: We are currently working with a simple balance that shouldn't know anything about accounts, so I call it "BalanceData" instead. SourceAccount.Mixin and SavingsAccountEntity now extends BalanceData. We could imagine extending several atomic dumb Data parts (AccountHolderData, InterestData etc), so it doesn't make sense to point to a single "data" variable as in v2. Role and entity can be composed of smaller atomic Data parts in a flexible and independent way like this. Constraint: Role should only extend Data parts present in the Entity in order. This should probably be enforced somehow (by some annotation possibly). Otherwise the Role could be working on some injected atomic Data part not in the entity and it wouldn't be saved with the entity.
* v4: BalanceData now protects the balance in an private interface, so that clients can't set the balance directly (to an invalid amount). The property allowedMinimum is still public - we can mix "private" and "public" properties in an atomic Data part and use the private option when constraints have to be enforced.
Now that the sensitive data is more protected, we can let the SourceAccount extend BalanceData so that we have direct access in the Role to the public Data methods instead of making proxy methods in the Role to get access to the data (getBalance() for instance).
* v5: No change.
* v6: 
   * Balance is now calculated as the sum of TransactionEntries with the given account id. Transactions is treated as a role, although it contains data. Is this a sin? 
   * Note how Transaction extends TransactionEntries: an example of composed Data objects.
   * TransactionEntries has a reference to the Transaction (with the @This annotation) that they belong to - they are both part of TransactionEntity.
   * In TransferMoneyContext we now have more generic account number arguments and a "unified" accountEntity.
   * In TransferMoneyContext we have skipped the init method. This implementation doesn't seem to need it.

## Directory organization and file naming

* v1: Two main directories: fast changing Behavior part (with context and role subdirectories) and slowly changing Domain part (with data and entity subdirectories). All files have a postfix: SomeContext, SomeRole, SomeData, SomeEntity. We can see immediately what the file is about.
* v2: Accomodation to exact Use case naming by ommitting the Role postfix from the Role files, so that we have SourceAccount instead of SourceAccountRole.
* v3: Api introduced in a separate main directory.
* v4: Okay, now we have an issue: In the old days we had domain objects having both state and behavior, and now we refer to Roles, Contexts and dumb Domain Objects. I would say that Roles/Contexts/Use cases are at least equally as much (if not more!) about the domain as the data objects. So why exclude them from "domain" by assigning that name to only the dumb data objects?! I therefore moved the behavior directory into a main domain directory now containing behavior and structure. Behavior contains context and role sub directories as before. Structure contains data and entity sub directories.
* v5: No change.
* v6: 
   * In some ways we also have some behavior in the Data objects. We increase a balance etc. So I moved data and entity a level up, and skipped the structure/behavior organization. Context and Roles are only about Use cases, right? So why not organize them as such. So now, the domain contains three directories: data, entity and usecase (with context and role sub directories). Those three concepts evolve independently and would therefore have different sub-organizing in real projects. (DDD) Bounded contexts would probably be containers for the three.
   * Decided to skip the Data postfix for Data files and instead add the Role postfix to Roles again. I definitely want to be able to distinguish between Data and Role objects, so that we don't have "SourceAccount" and "SavingsAccount" causing Data/Role confusion.
