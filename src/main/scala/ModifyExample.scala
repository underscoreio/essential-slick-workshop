import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

import slick.driver.H2Driver.api._

object ModifyExample {



  // Tables -------------------------------------



  case class Message(
    sender  : String,
    content : String,
    id      : Long = 0L
  )

  class MessageTable(tag: Tag) extends Table[Message](tag, "messages") {
    def sender  = column[String]("sender")
    def content = column[String]("content")
    def id      = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def * = (sender, content, id) <> (Message.tupled, Message.unapply)
  }

  lazy val MessageTable = TableQuery[MessageTable]



  // Actions ------------------------------------



  val insert: DBIOAction[Option[Int], NoStream, Effect.Write] =
    MessageTable ++= Seq(
      Message("Dave", "Hello, HAL. Do you read me, HAL?"),
      Message("HAL",  "Affirmative, Dave. I read you."),
      Message("Dave", "Open the pod bay doors, HAL."),
      Message("HAL",  "I'm sorry, Dave. I'm afraid I can't do that.")
    )

  val updateHal: DBIOAction[Int, NoStream, Effect.Write] =
    MessageTable
      .filter(_.sender === "HAL")
      .map(_.sender)
      .update("HAL 9000")

  val deleteDave: DBIOAction[Int, NoStream, Effect.Write] =
    MessageTable
      .filter(_.sender === "Dave")
      .delete



  // Database and helpers -----------------------



  val db = Database.forConfig("workshopdatabase")

  def exec[T](action: DBIO[T]): T =
    Await.result(db.run(action), 2 seconds)

  exec(MessageTable.schema.create)



}