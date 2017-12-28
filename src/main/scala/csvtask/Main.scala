package csvtask

import akka.actor.ActorSystem
import akka.actor.FSM.Failure
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import csvtask.connector.Connector
import csvtask.preprocess.Preprocess
import csvtask.process.Process

import scala.util
import scala.util.{Success, Try}

//noinspection TypeAnnotation
object Main {

  def main(args: Array[String]): Unit = {
    implicit val ac = ActorSystem("testSystem")
    implicit val mat = ActorMaterializer.create(ac)
    implicit val ec = ac.dispatcher  // import it

    val connector = new Connector()

    // Consider using existing solutions instead of manual parameters parsing.
    // For instance: https://github.com/scopt/scopt
    def validateArgLength: Either[String, Unit] =
      if (args.length != 1) Left("Wrong number of arguments") else Right(())

    def readChoice: Either[String, Int] =
      Try(args(0).toInt).fold(_ ⇒ Left(s"${args(0)} is not a number"), x ⇒ Right(x))

    def validateNumber(opt: Int): Either[String, Unit] =
      if(opt < 1 || opt > 3) Left("Argument should be 1, 2 or 3") else Right(())

    // Consider sealed trait/objects hierarchy instead of Int constants.
    val opChoice = Map(1 → Process.dailyPrice, 2 → Process.dailyReturn, 3 → Process.periodMean)

    val res = for {
      _ ← validateArgLength
      opt ← readChoice
      _ ← validateNumber(opt)
    } yield  {
      val preprocessedSource = connector.priceStream("GOOG").via(Preprocess.preprocess)
      // Consider Flow instead of Source here:
      // preprocessedSource.via(Process.flow(opChoice(opt))).runWith(Sink.foreach(x ⇒ println(x)))
      Process.applyFlow(preprocessedSource, opChoice(opt)).runWith(Sink.foreach(x ⇒ println(x)))
    }



    res match {
      case Right(x) ⇒ x.onComplete(_ ⇒ ac.terminate())
      case Left(problem) ⇒
        println(problem)
        ac.terminate()
    }
  }
}
