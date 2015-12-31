package acds

import akka.actor._
import akka.cluster.Cluster
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Publish
import com.typesafe.config.ConfigFactory
import akka.actor.Props
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit

/**
  * */
/*object IndexerFrontend extends App {

  val port = if (args.isEmpty) "0" else args(0)
  val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port").
    withFallback(ConfigFactory.parseString("akka.cluster.roles = [frontend]")).
    withFallback(ConfigFactory.load())

  val system = ActorSystem("ClusterSystem", config)

  val frontend = system.actorOf(Props(classOf[IndexerFrontend]), name = "frontend")

}*/


/**
 *
 *
 */
class IndexerFrontend extends Actor {

  // override def preStart = workProducer ! Tick
  val cluster = Cluster(context.system)

  // activate the extension
  val mediator = DistributedPubSub(context.system).mediator

  import scala.concurrent.ExecutionContext.Implicits._

  context.system.scheduler.schedule(Duration.Zero, Duration.create(10, TimeUnit.SECONDS), self, IndexReq(""))

  def receive = {

    case IndexerSubcription =>
      sender() ! Ack

    case ir: IndexReq =>
      println("FrontEnd obtained IndexRequest ")
      mediator ! Publish("doc", ir.doc)

    case WorkerReady =>
      context watch sender()
      println(s"Backend Registration message received from ${sender()}")

  }
}
