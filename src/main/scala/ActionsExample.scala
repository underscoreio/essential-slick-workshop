import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

import slick.driver.H2Driver.api._

object ActionsExample {



  // Tables -------------------------------------



  case class Account(
    number  : String,
    balance : Int,
    id      : Long = 0L
  )

  class AccountTable(tag: Tag) extends Table[Account](tag, "accounts") {
    def number  = column[String]("number")
    def balance = column[Int]("balance")
    def id      = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def * = (number, balance, id) <> (Account.tupled, Account.unapply)
  }

  lazy val AccountTable = TableQuery[AccountTable]



  // Handy helpers ------------------------------



  val db = Database.forConfig("workshopdatabase")

  def exec[T](action: DBIO[T]): T =
    Await.result(db.run(action), 2 seconds)



  // Sequencing actions -------------------------



  val createTables: DBIO[Unit] =
    AccountTable.schema.create

  val insertAccounts: DBIO[Option[Int]] =
    AccountTable ++= Seq(
      Account("Alice",   1000),
      Account("Bob",      500),
      Account("Charlie", 1200),
      Account("Dave",     300)
    )

  def createAndInsert: DBIO[Option[Int]] =
    createTables >> insertAccounts



  // Transforming the results of actions --------



  def accountExists(acct: String): DBIO[Boolean] =
    ???



  // Sequencing interdependent actions ----------



  def openAccount(acct: String, balance: Int): DBIO[Int] =
    AccountTable += Account(acct, balance)

  def getBalance(acct: String): DBIO[Option[Int]] =
    AccountTable
      .filter(_.number === acct)
      .map(_.balance)
      .result
      .headOption

  def setBalance(acct: String, balance: Int): DBIO[Int] =
    AccountTable
      .filter(_.number === acct)
      .map(_.balance)
      .update(balance)

  def setup: DBIO[Option[Int]] =
    createTables >>
    insertAccounts

  def deposit(acct: String, amt: Int) =
    ???

  def withdraw(acct: String, amt: Int) =
    ???

  def transfer(acct: String, amt: Int) =
    ???


}