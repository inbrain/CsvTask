package csvtask

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, scaladsl}
import akka.stream.scaladsl.Source
import org.specs2.mutable.Specification

//noinspection TypeAnnotation
class BaseSpec extends Specification{
  implicit val ac = TestInit.ac
  implicit val mat = TestInit.mat
  implicit val ec = TestInit.ec

  def testSource(data: String): Source[String, NotUsed] =
    scaladsl.Source.fromIterator(() ⇒ scala.io.Source.fromString(data).getLines())
  def testSourceFromPath(path: String): Source[String, NotUsed] =
    scaladsl.Source.fromIterator {() ⇒
      scala.io.Source.fromInputStream(getClass.getClassLoader.getResourceAsStream(path), "UTF-8").getLines()
    }
}

//noinspection TypeAnnotation
object TestInit {
  val ac = ActorSystem("testSystem")
  val mat = ActorMaterializer.create(ac)
  val ec = ac.dispatcher
}