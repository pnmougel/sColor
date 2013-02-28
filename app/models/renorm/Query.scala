package models.renorm

import anorm.ParameterValue
import scala.collection.mutable.LinkedList

abstract class Query {
    var next: Option[Query] = None
    var first: Option[Query] = Option(this)

    def ::(q: Query): Query = {
        q.next = Option(this)
        this.first = Option(q)
        q
    }

    def toSql(): String

    var params = new LinkedList[(Any, ParameterValue[_])]()

    def toSqlStatement(): String = {
        var curQuery = first
        val sqlStatement = new StringBuffer()

        while (curQuery.isDefined) {
            sqlStatement.append(curQuery.get.toSql())
            if (curQuery.get.isInstanceOf[Where]) {
                params = params.++:((curQuery.get.asInstanceOf[Where]).where)
            }
            curQuery = curQuery.get.next
        }
        sqlStatement.toString()
    }
}

object EmptyQuery extends Query {
    override def toSql() = ""
}

case class Where(var where: Seq[(Any, anorm.ParameterValue[_])]) extends Query {
    override def toSql() = {
        if (where.isEmpty) {
            ""
        } else {
            " WHERE " + where.map {
                case (s: Symbol, v) => s.name + " = {" + s.name + "}"
                case (k, v) => k.toString + " = {" + k.toString + "}"
            }.mkString(" AND ")
        }
    }
}

case class OrderBy(fieldName: String, order: String = "ASC") extends Query {
    override def toSql() = " ORDER BY \"" + fieldName + "\" " + order
}

case class Limit(limit: Int, offset: Int = 0) extends Query {
    override def toSql() = " LIMIT " + limit + (if (offset != 0) " OFFSET " + offset else "")
}

case class GroupBy(fieldName: String) extends Query {
    override def toSql() = " GROUP BY \"" + fieldName + "\""
}

