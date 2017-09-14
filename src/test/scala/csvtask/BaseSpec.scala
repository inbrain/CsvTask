package csvtask

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, scaladsl}
import akka.stream.scaladsl.Source
import org.specs2.mutable.Specification

class BaseSpec extends Specification{
  implicit val ac = TestInit.ac
  implicit val mat = TestInit.mat
  implicit val ec = TestInit.ec

  def testSource(data: String): Source[String, NotUsed] =
    scaladsl.Source.fromIterator(() ⇒ scala.io.Source.fromString(data).getLines())
}

object TestInit {
  val ac = ActorSystem("testSystem")
  val mat = ActorMaterializer.create(ac)
  val ec = ac.dispatcher


}