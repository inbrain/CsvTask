package csvtask

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.specs2.mutable.Specification

class BaseSpec extends Specification{
  implicit val ac = TestInit.ac
  implicit val mat = TestInit.mat
  implicit val ec = TestInit.ec
}

object TestInit {
  val ac = ActorSystem("testSystem")
  val mat = ActorMaterializer.create(ac)
  val ec = ac.dispatcher

}