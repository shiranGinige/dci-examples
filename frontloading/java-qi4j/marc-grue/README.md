# Frontloading

5 Java-Qi4j implementations of the frontloading example by Marc Grue, December 2010. Comments below are from that time too - much has happened since!


## Resources

* [Shift of Mental Model from Data to Roles - Qi4j Frontloading in 5 versions](http://groups.google.com/group/object-composition/browse_thread/thread/1c4c8c503f33e98a/f98b6faef02dd9b1?q=frontloading&lnk=ol&)
* [Frontloading Example](http://groups.google.com/group/object-composition/browse_thread/thread/f2345a1f6d46b5c2/f69d10d813b1cf0d)
* [Multiple context execution in controller](http://groups.google.com/group/object-composition/browse_thread/thread/9e2b3e28803fca44/aa8a2d7aa2f4a7cb)
* Execution model imitated from [Trygve Reenskaug's SmallTalk implementation](http://heim.ifi.uio.no/~trygver/2009/bb4plan.pdf)
* Excellent SmallTalk [analysis from Brian (candlerb)](http://groups.google.com/group/object-composition/browse_thread/thread/854df3a328e1c263/5f6985a4cebf86b5) in order to understand Trygves example.
* [Qi4j home](http://www.qi4j.org/)

## Comments

Preconditions / definitions:

* A Project has Activities
* An Activity has a start time, a duration and an end time
* An Activity B can succeed an Activity A. A becomes a "predecessor" to B, and B a "successor" to A.
* An Activity can have one successor
* An Activity can have many predecessors
* An Activity can only start when all its predecessors have finished
* Frontloading is the process of finding the earliest start time for an activity
* When an activity has a start and end time it has been planned
* When all activities have been planned, the frontloading process is completed

Use case algorithm:

1. Find all Activities of a Project
2. Reset start and end times of all Activities
3. Traverse all Activities
4. Find Predecessors of current Activity in the iteration
5. Check if all of those Predecessors are planned:
    1. YES: Plan this activity - set start time to latest Predecessor finish time
    2. NO:  Go to next Activity
6. Exit traversal when all Activities are planned (we might loop existing Activities several times)

This example only has 2 Roles in the FrontLoadContext since the Data model operates with a unified entity composite containing both Activity and Dependency Data (Trygves example has separated those and therefore utilizes sepearate Roles - "Predecessors" (dependencies) and Activity.

## Versions

### v1
I started out along the thinking behind my last PayBills example of seeing the Roles (here FrontloaderRole and TraversedActivityRole) as smart manipulators of Data to implement behaviour. "How can I get the script working?", "How can I manipulate the Data?", "Should I modify the Data in this or that Role?". My focus was on Data. Basically I could have chosen to have all Data manipulation in one big role (a Mediator). And I would completely have lost the idea of Roles. I thought though, that reassigning the TraversedActivityRole in the script loop was fancy. And the TraversedActivityRole would also show having access to both DependencyData and ActivityData. 

Regarding the directory structure: I insist that the Domain contains also Contexts and Roles. You could see a Context layer (including Context/Roles/Rolemaps) having a dependency to a Data layer (including Data and Entity) within the domain.


### v2

I felt I was missing something from Trygves implementation. Saw that he had 5 Roles (Context being a super class)! Why? Okay, then I did 4 Roles (the "Context" super class responsibilities went into my FrontloadContext). Now I could distribute Data handling even more precisely to appropriate Roles. Each Role now had only 1 or 2 methods basically just returning Data. Hmm, quite a lot of code for so little or no benefit…


### v3

Finally noticed that all of Trygves Roles merely returned Data. No behaviour methods defined - AHAA, *that* is what a "Methodless Role" is!! A simple identifier with a meaningful Role name to a Data Object. A Methodless Role simply only uses the methods of the Data Object! That's what has been confusing me for a long time: A Mehtodless Role *has* methods you can call, it's just that these are defined in the Data class and not in a Role class. This is semantically confusing! The identifier itself doesn't have methods, but the Object identified has! 

When we talk about a "Role", it could be any of those:

* Role player
* Methodful Role
* Methodful Object Role
* Methodful Role type
* Methodful Object Role type
* Methodless Role
* Methodless Role type
* Methodless Object Role type
* more?

It's not easy with all these name options, specially not when adding all programming language differences. No wonder there's sometimes confusion on this list :(

Is it possible to distinguish methodless/methodfull in a generic/simplified way (disregarding different language constructs) by saying that

1. A Methodless Role is an identifier to a Data Object.
2. A Methodful Role is an identifier to a Data Object that also implements Use Case specific behavior.

Notice that my rolemaps have references to only methodful Roles.

Back to the example:
Trygve was reselecting all the Roles during the while loop to find unplanned activities. Hmmm, that was also new to me… I went on to imitate as closely I could and did the same "Role binding methods" that are used by the reselectObjectForRoles method in the Context super class to rebind all Roles. 

The logic for finding an unplanned activity now went from being a responsibility of some Methodful Role to some Role Binding Method in the Context. So, logic determining the Role setup happens in the Context, not in Roles! This was quite a shift in thinking for me! We have *two* areas of logic! Role binding and Role behaviour. It feels as though this is a crucial part of DCI.

I see basically *two* types of Role binding:

1. Static Role Binding: happens *once*, either on Context instantiation or with a bind method on the Context before enactment.
2. Dynamic Role Binding: happens *dynamically* anytime suitable before/during enactment.

This was a big eye opener for me. So talking about re-binding/re-selection/re-assignment only makes sense when talking about Dynamic Role Binding. The logic for this should happen _not_ in the Methodful Roles (as I did in v1) but in the Context only - "the one single place, where you can reason about what Objects play what Roles". 

Notice the nice possibility to add dependencies as a comma-separated list of Activity objects in FrontloadTest, line 140. This required some extra logic in DependencyData to ensure data integrity by avoiding cyclic dependencies. I think that any logic that merely enforces valid state is naturally placed in Data classes - WDYT?


### v4

Okay, SmallTalk is different than Java/Qi4j, so I unified all the individual Role Binding Methods in a "canSelectUnplannedActivity" that makes Use Case specific sense (replacing the generic "reselectObjectsForRoles"). It seems unnecessary to reselect the Roles allActivities and frontloader since they don't change during enactment. Only Activity and Predecessors do.

Trygves has a "reselectObjectsForRoles" (as I do in v3) that calls all individual Role binding methods for each Role in order to "activate" the Roles and make them available to the context and the Roles where they will be visible (according to a roleStructure setup). Is there some arguments agains having all those unified in one method as I have in v4?

In v1 I was referencing Data Objects with names like "project", "dependencies" and "activity" inside the Roles, but in v4 I'm referencing only to other Roles with meaningful Use Case names. It's closer to the mental model of the Use Case to think "Predecessor" than "Dependency". It took me quite a while to make this shift from Data-centric to Role-centric thinking, but I think it's crucial to DCI.

Notice also that Methodless Roles can be pointers to *collections* of Data (I understand why Jim wondered about my earlier identity questions of Data objects :). It's a shift in thinking - going from thinking in terms of Data manipulation to weave Role responsibilities! A "network of communicating objects".


### v5

Combining static and dynamic Role binding! Frontloader and AllActivites don't change during enactment and could therefore be set upon Context instantiation, whereas Activity and Predecessors are dynamically reselected during enactment. That also made saving projectData in a Context data variable unnecessary.

Named the Role binding method more intuitively and added a boolean return value to make the re-selection loop more intuitive. As a consequence I could also skip the Context base class.
