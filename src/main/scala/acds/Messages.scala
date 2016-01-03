package acds

import akka.actor.ActorRef

case object IndexerNodeUp

case object IsMasterElected

case class Election(ts: Long)

case object ElectionOver

case object LeaderElected

case object NewLeader

case class PreElectedMaster(actorRef: ActorRef)

case object DataRequest

case object CheckMasterHB

case object HBMaster

case object MasterHBAck