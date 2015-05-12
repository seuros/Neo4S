package com.kreattiewe.neo4s.orm

import org.anormcypher.{Cypher, Neo4jREST}

import scala.concurrent.{ExecutionContext, Future}

/**
 * Created by michelperez on 4/29/15.
 *
 * creates a relationship dao with the following feature
 * A = Node Source
 * B = Node Destitny
 * C = Rel
 **/
abstract class RelDAO[A <: NeoNode[_] : Mapper, B <: NeoNode[_] : Mapper, C <: NeoRel[A, B] : Mapper]
(implicit val MapperA: Mapper[A], implicit val MapperB: Mapper[B], implicit val MapperC: Mapper[C]) {

  val unique: Boolean = true

  def validateUniqueness(c: C)(implicit connection: Neo4jREST, ec: ExecutionContext): Future[Boolean] = Future {
    val query =
      s"""
         match (a:${c.from.label}{ id: "${c.from.getId()}"})-[c:${c.label}]->(b:${c.to.label}{ id: "${c.to.getId()}"})
         return c
       """.stripMargin
    Cypher(query)().size == 0
  }

  /** saves a relationship of type C between A and B */
  def save(c: C)(implicit connection: Neo4jREST, ec: ExecutionContext): Future[Boolean] =
    for {
      validate <- if (unique) validateUniqueness(c) else Future(true)
    } yield {
      if (validate) {
        val query =
          s"""
         match (a:${c.from.label}{ id: "${c.from.getId()}"}),
         (b:${c.to.label}{ id: "${c.to.getId()}"})
         create (a)-[c:${c.label} {props}]->(b)
      """.stripMargin
        Cypher(query).on("props" -> MapperC.caseToMap(c)).execute()
      }
      else false
    }


  /** updates the relationship of type C */
  def update(c: C)(implicit connection: Neo4jREST, ec: ExecutionContext): Future[Boolean] = Future {
    val query =
      s"""
         match (a:${c.from.label}{ id: "${c.from.getId()}"}),
         (b:${c.to.label}{ id: "${c.to.getId()}"}),
         (a)-[c:${c.label}]->(b)
         set c += {props}
      """.stripMargin
    Cypher(query).on("props" -> MapperC.caseToMap(c)).execute()
  }

  /** deletes the relationship of type C */
  def delete(c: C)(implicit connection: Neo4jREST, ec: ExecutionContext): Future[Boolean] = Future {
    val query =
      s"""
         match (a:${c.from.label}{ id: "${c.from.getId()}"}),
         (b:${c.to.label}{ id: "${c.to.getId()}"}),
         (a)-[c:${c.label}]->(b)
         delete c
      """.stripMargin
    Cypher(query).execute()
  }

}
