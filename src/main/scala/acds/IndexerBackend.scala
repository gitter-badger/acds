package acds

import akka.actor._
import akka.cluster.ClusterEvent.{CurrentClusterState, MemberUp}
import akka.cluster.pubsub.DistributedPubSubMediator.Subscribe
import akka.cluster.{Cluster, Member, MemberStatus}
import com.typesafe.config.ConfigFactory
import scala.collection.mutable.ListBuffer

/**
 */
/*object IndexerBackend extends App {
  // Override the configuration of the port when specified as program argument
  val port = if (args.isEmpty) "0" else args(0)
  val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port").
    withFallback(ConfigFactory.parseString("akka.cluster.roles = [indexer]")).
    withFallback(ConfigFactory.load())

  val system = ActorSystem("ClusterSystem", config)
  system.actorOf(Props[IndexerBackend], name = "indexBackend")
}*/

class IndexerBackend extends Actor {

  val mediator = akka.cluster.pubsub.DistributedPubSub(context.system).mediator

  val cluster = Cluster(context.system)

  val peersBuffer = ListBuffer[ActorRef]()

  // subscribe to cluster changes, MemberUp
  // re-subscribe when restart
  override def preStart(): Unit = {
    cluster.subscribe(self, classOf[MemberUp])
    // Sub to IndexFrontEnd's index-req message
    mediator ! Subscribe("doc", self)
  }

  override def postStop(): Unit = cluster.unsubscribe(self)

  var currHash: String = ""

  def receive = {
    case Ack => cluster.subscribe(sender, classOf[IndexWork])

    case state: CurrentClusterState =>
      state.members.filter(_.status == MemberStatus.Up) foreach register

    // case s: String => sender() ! ElectMe(s)
 
    case iw: IndexWork => println(s"IndexWork received with ${iw.doc}")

    case IndexerNodeUp =>
      peersBuffer.+=(sender())
      println(s"A Indexer node is brought up now the peers are $peersBuffer")

    case MemberUp(m) => register(m)



    case akka.cluster.pubsub.DistributedPubSubMediator.SubscribeAck(Subscribe("doc", None, `self`)) =>
      println("subscribing")
  }

  def register(member: Member): Unit =
    if (member.hasRole("indexer"))
      context.actorSelection(RootActorPath(member.address) / "user" / "indexBackend") ! IndexerNodeUp

}
