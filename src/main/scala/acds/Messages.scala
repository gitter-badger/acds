package acds

import akka.actor.ActorRef

case object MasterEnd

case object Tick

case object Ack

case object WorkerReady

case object AssignWork

case object IndexerNodeUp

case object IndexerSubcription

case class IndexWork(doc: String)

case class IndexReq(doc: String)

case object IsMasterElected

case class Election(ts:Long)

case object ElectionOver

case object LeaderElected

case object NewLeader

case class PreElectedMaster(actorRef: ActorRef)