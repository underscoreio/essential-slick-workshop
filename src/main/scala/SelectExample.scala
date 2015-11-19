import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

import slick.driver.H2Driver.api._

object SelectExample {



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



  // Queries ------------------------------------



  val base: Query[MessageTable, Message, Seq] =
    MessageTable

  val filter: Query[MessageTable, Message, Seq] =
    MessageTable
      .filter(_.sender === "HAL")

  val sort: Query[MessageTable, Message, Seq] =
    MessageTable
      .sortBy(_.id.desc)

  val offsetAndLimit: Query[MessageTable, Message, Seq] =
    MessageTable
      .drop(1).take(2)

  val project1: Query[Rep[String], String, Seq] =
    MessageTable
      .map(_.sender)

  val project2: Query[Rep[Int], Int, Seq] =
    MessageTable
      .map(_.sender)
      .map(_.length)



  // Database and helpers -----------------------



  val db = Database.forConfig("workshopdatabase")

  def exec[T](action: DBIO[T]): T =
    Await.result(db.run(action), 2 seconds)

  exec(MessageTable.schema.create)

  exec(
    MessageTable ++= Seq(
      Message("Dave", "Hello, HAL. Do you read me, HAL?"),
      Message("HAL",  "Affirmative, Dave. I read you."),
      Message("Dave", "Open the pod bay doors, HAL."),
      Message("HAL",  "I'm sorry, Dave. I'm afraid I can't do that.")
    )
  )



}