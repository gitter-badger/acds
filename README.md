**Why Leader Election**

Lets say that we have a cluster of nodes with out SPOF. There should be a node in the cluster ready to receive the value from client and save it (disk or memory). How do we find that node,hence we need to select a node from the cluster as Master and that in turn replicates to other nodes. Instead of us making the decision of hand-pick the node, this alogrithm is going to pick node as master, this process is called **Leader Election** and is a common use case in Distributed Systems.

I had implemented a simple **Leader Election** algorithm that does this using Akka. This post discusses the implementation.

**Algorithm**

Each node is considered as a State machine in this algorithm where in it could be in any of the following state

* Idle - node start with this state.
* Candidate - when node becomes ready for election.
* Leader - the master of the cluster.

**Idle to Candidate**

* on start of each node it should be aware ofthe other nodes

* a timer executes and it checks if master is elected

* if elected it updates its reg to the master

* Else it starts the election processs where in it initaites "Election"
	msg which of type

		// Election(ActorRef , Long)

	is sent to other nodes here by it (and other nodes) un-become "Idle" i.e it goes back to the Idle state.

**Candidate to Leader**

* While its candidate it also receives election message from other nodes and it 	stores all such messages in a cache.

		// List(Election)

* At this state another scheduler kicks in to elect the leader such that all 	nodes finds the actor ref for corresponding oldest timestamp basically a 	simple search in the cache and is sent a Leader Elected message.

* The node which gets the Leader elected message is the new Leader thus it 	becomes "Leader" now the leader send other nodes Leader message.

* Other nodes remains as candidate and update their master reference to the 	sender 	of the "Leader" message.


**When Master node goes down**

Now that master is elected and oher nodes acting in Idle state. Its also possible hat Master node might go down in such situations and other nodes should be aware of the state master in equal intervals of time.

* Each node sends a Heart-Beat message to the master for every 2 secs and 	receive a Ack msg from the master within 2 secs.

* If the Ack is received the node considers that master is alive if not the node 	gets a time out or something it starts a election thus becomes candidate.


**More steps**

So far I have implmented only the alorithm and its not complete with a implementation. 