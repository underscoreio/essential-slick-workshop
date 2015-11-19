import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

import slick.driver.H2Driver.api._

object MusicExample {



  // Tables -------------------------------------



  case class Album(
    title    : String,
    artistId : Long,
    id       : Long = 0L
  )

  class AlbumTable(tag: Tag) extends Table[Album](tag, "albums") {
    val title    = column[String]("title")
    val artistId = column[Long]("artistid")
    val id       = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def * = (title, artistId, id) <> (Album.tupled, Album.unapply)
  }

  lazy val AlbumTable = TableQuery[AlbumTable]

  case class Artist(
    name : String,
    id   : Long = 0L
  )

  class ArtistTable(tag: Tag) extends Table[Artist](tag, "artists") {
    val name = column[String]("name")
    val id   = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def * = (name, id) <> (Artist.tupled, Artist.unapply)
  }

  lazy val ArtistTable = TableQuery[ArtistTable]



  // Handy helpers ------------------------------



  val db = Database.forConfig("workshopdatabase")

  def exec[T](action: DBIO[T]): T =
    Await.result(db.run(action), 2 seconds)



  // Sequencing actions -------------------------



  val createDatabase: DBIO[Unit] = for {
    _        <- ArtistTable.schema.create
    _        <- AlbumTable.schema.create
    bieberId <- ArtistTable returning ArtistTable.map(_.id) += Artist("Justin 'The Man' Bieber")
    beardyId <- ArtistTable returning ArtistTable.map(_.id) += Artist("Beardyman")
    _        <- AlbumTable += Album("Believe", bieberId)
    _        <- AlbumTable += Album("My World", bieberId)
    _        <- AlbumTable += Album("Distractions", beardyId)
  } yield ()

  exec(createDatabase)



}
