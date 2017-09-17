package csvtask

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.GraphDSL
import csvtask.connector.Connector
import csvtask.preprocess.Preprocess

import scala.io.Source

//noinspection TypeAnnotation
class Main {
  implicit val ac = ActorSystem("testSystem")
  implicit val mat = ActorMaterializer.create(ac)
  implicit val ec = ac.dispatcher

  val connector = new Connector()


  def sampleLogic(source: Source[]) = GraphDSL.create() { implicit b: GraphDSL.Builder[NotUsed] ⇒
    import GraphDSL.Implicits._


  }

  connector.priceStream("GOOG").via(Preprocess.preprocess).
}
