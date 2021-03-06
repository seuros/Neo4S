package graph.model.orm

import com.kreattiewe.neo4s.orm._
import com.kreattiewe.mapper.macros.Mappable

/**
 * Created by michelperez on 4/26/15.
 */
object UserMappers {
  implicit val myUserMapper = Mapper.build[MyUser]
  implicit val myUserOptMapper = Mapper.build[MyUserOpt]
  implicit val myUserExpMapper = Mapper.build[MyUserExp]
  implicit val myRelMapper = Mapper.build[MyRel]
  implicit val myRelSeqMapper = Mapper.build[MyRelSeq]
}

object UserNodes {

  import UserMappers._

  implicit val userNode = NeoNode("user", (user: MyUser) => user.id)
  implicit def parseUserNode(user: MyUser) = userNode.operations(user)

  implicit val userOptNode = NeoNode("user", (user: MyUserOpt) => user.id.getOrElse(""))
  implicit def parseUserOptNode(user: MyUserOpt) = userOptNode.operations(user)

  implicit val userExpNode = NeoNode("user", (user: MyUserExp) => user.id)
  implicit def parseUserExpNode(user: MyUserExp) = userExpNode.operations(user)

}

object UserRels {

  import UserMappers._
  import UserNodes._

  val userRel = NeoRel[MyRel, MyUser, MyUser]("friendship", true)
  implicit def parseUserRel(rel: MyRel) = userRel.operations(rel)

  val userRelSeq = NeoRel[MyRelSeq, MyUser, MyUser]("friendship", true)
  implicit def parseUserRelSeq(rel: MyRelSeq) = userRelSeq.operations(rel)
}

case class MyUser(id: String, name: String, age: Int)

case class MyUserOpt(id: Option[String], name: String, age: Option[Int])

case class MyUserExp(id: String, name: String, email: String)

case class MyRel(from: MyUser, to: MyUser, enabled: Boolean) extends Rel[MyUser, MyUser]

case class MyRelSeq(from: MyUser, to: MyUser, enabled: Boolean, loc: Seq[Double]) extends Rel[MyUser, MyUser]