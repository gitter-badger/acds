package acds

import java.util.concurrent.TimeUnit

import akka.actor._
import akka.cluster.ClusterEvent.{MemberUp, UnreachableMember}
import akka.cluster.{Cluster, Member}
import com.typesafe.config.ConfigFactory

import scala.collection.mutable.ListBuffer
import scala.concurrent.duration.Duration


object Node extends App {
  // Override the configuration of the port when specified as program argument
  val port = if (args.isEmpty) "0" else args(0)
  val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port").
    withFallback(ConfigFactory.parseString("akka.cluster.roles = [indexer]")).
    withFallback(ConfigFactory.load())

  val system = ActorSystem("ClusterSystem", config)
  system.actorOf(Props[Node], name = "indexBackend")
}

class Node extends Actor {

  val cluster = Cluster(context.system)

  val peersBuffer = ListBuffer[ActorRef]()

  val nodeData = new NodeData()

  // subscribe to cluster changes, MemberUp
  // re-subscribe when restart
  override def preStart(): Unit = {
    cluster.subscribe(self, classOf[MemberUp])
    cluster.subscribe(self, classOf[UnreachableMember])
  }

  override def postStop(): Unit = cluster.unsubscribe(self)

  def receive = idle

  var masterElected = false

  var electedMaster: ActorRef = null

  var lastTimeStamp = 0l

  import scala.concurrent.ExecutionContext.Implicits._

  context.system.scheduler.schedule(Duration.create(2, TimeUnit.SECONDS), Duration.create(2, TimeUnit.SECONDS), self, IsMasterElected)

  context.system.scheduler.schedule(Duration.create(4, TimeUnit.SECONDS), Duration.create(10, TimeUnit.SECONDS), self, ElectionOver)

  /**
    * this stage node collects its peers before it can conduct election
    * when the node
    */
  def idle: Receive = {
    case MemberUp(m) => register(m)

    case IndexerNodeUp =>
      if (masterElected) {
        sender() ! PreElectedMaster(electedMaster)
      }
      peersBuffer.+=(sender())
      println(s"A Indexer node is brought up now the peers are $peersBuffer")

    case a: PreElectedMaster =>
      masterElected = true
      electedMaster = a.actorRef

    case IsMasterElected =>
      println("schduler kicked in to elect master")
      if (!masterElected) {
        println("Cluster's master does not exist , we may need election")
        lastTimeStamp = System.currentTimeMillis()
        println(s"Sending Election message to peers $lastTimeStamp -> $peersBuffer")
        println("Iam a candidate now contesting on election")
        context.become(candidate)
        peersBuffer
          // .filter(ar => ar.path != self.path)
          .foreach(ar => ar ! Election(lastTimeStamp))
      } else context.become(candidate)
    //
  }

  def candidate: Receive = {
    case IndexerNodeUp =>
      if (masterElected) {
        sender() ! PreElectedMaster(electedMaster)
      }
      peersBuffer.+=(sender())
      println(s"A Indexer node is brought up now the peers are $peersBuffer")

    case election: Election =>
      println("Received vote from peer")
      nodeData.addVote(sender(), election.ts)
    case ElectionOver =>
      println(s"schduler kicked in to announce election over master ${masterElected}")
      if (!masterElected) nodeData.findOldest ! LeaderElected
    case LeaderElected =>
      println("Oh my god ... Iam elected as leader ")
      masterElected = true
      println("Sending the new leader")
      peersBuffer.foreach(ar => ar ! NewLeader)
    case NewLeader =>
      println("Welcome new leader")
      masterElected = true
      electedMaster = sender()
      if (sender().path == self.path) {
        println("Iam elected Let me sworn in as leader ")
        context.become(leader)
        self ! "First Msg"
      }
      nodeData.invalidateVotes()
  }

  def leader: Receive = {
    case s: String => println(s"After elected as a leader $s")
    case IndexerNodeUp =>
      if (masterElected) {
        sender() ! PreElectedMaster(electedMaster)
      }
      peersBuffer.+=(sender())
      println(s"A Indexer node is brought up now the peers are $peersBuffer")
  }

  def register(member: Member): Unit =
    if (member.hasRole("indexer"))
      context.actorSelection(RootActorPath(member.address) / "user" / "indexBackend") ! IndexerNodeUp


}

class NodeData {

  private val votes = scala.collection.mutable.Map[Long, ActorRef]()

  def addVote(actorRef: ActorRef, ts: Long) = votes.+=(ts -> actorRef)

  def findOldest = {
    val oldest = votes.keySet.toSeq.sortWith((a, b) => a < b).head
    votes(oldest)
  }

  def invalidateVotes() = votes.clear()
}