package models.renorm

import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current
import scala.collection.mutable.LinkedList
import scala.Array.canBuildFrom

/*
Quelques idées rapides :
- Il est possible de définir des paramètres where, from (surement découvrable automatiquement), limit, group by qui ont plusieurs types (Pattern Matching), donc on pourrait avoir soit une séquence, soit un type plus complexe (AND OR IN LIKE, ETC)
- Il faut faire le update (Seq pour les valeurs à modifier et Where pour savoir ou), surement faire un updateAtId également
- l'attribut single doit surement être générable de manière automatique, avec un comportement sympa
- réfléchir à comment faire les jointures
- Pour l'insertion, il doit être possible d'utiliser le type T. En fait, T pose surtout des problèmes pou l'identifiant. Avant l'insertion il n'existe pas, par contre, lorsque l'on récupère une instance on en a besoin et d'une manière simple (i.e., T.id et pas T.id.get). L'exemple suivant est une piste de ce que l'on pourraît faire. 
Attention, il faut aussi prendre en compte le cas avec des clés plus complexes (ce qui correspond au generic KeyType)

    class Entity[KeyType] {
        def id : KeyType = {
            0
        }
    }

    case class Foo(name: String) extends Entity

 - Il faut aussi absolument gérer le cas de manière élégante ou le Case Class correspond assez peu à la table (i.e., champs en plus et champs en moins). Comment gérer le cas ou un attibut d'une case classe doit être modifié pour corespondre à des champs dans la base de données ? Annotation MapsTo : Function[Any, (String, Any)*] ?

 - Idée générale : La case class telle qu'il est défini actuellement permet de faire un mapping row => Class mais pas l'inverse. Il est donc utile pour la selection mais pas l'insertion. Le mapping inverse permet de gérer l'insertion de manière élégante.

 */


/*
 * Pour l'instant la sélection des colonnes se fait avec le nom des colonnes dans la base de données, c'est un peu moche
 * 
 * Il est possible d'utiliser directement les champs de la classe mais c'est un peu bourrin :)
 * 
 * Les champs de la case class doivent être de type F[String] ou F[Long] etc.
 * On peut avoir le même comportement que le type du généric avec la conversion implicite :
 * implicit def fieldToValue[T](field : F[T]) : T = field.value
 * Avec la class F[T](value : T)
 * 
 * Ensuite pour accéder aux attributs il faut une fonction avec un prototype du type e : T => Query
 * Dans l'instance 'e' passée en paramètre, les F n'ont pas de valeurs de la table, mais par contre on a associé à chaque instance de F le nom correspondant dans la table
 * Pour pouvoir faire ça, il faut créer une instance de T dans l'objet, et prier pour que l'on puisse construire des case classes sans passer de valeur en parametre
 * 
 * Solution (pour l'instant) : Créer une fausse classe pour chaque type T possible de F[T]
 * Example : 
class StringFieldName(var fieldName : String) extends F[String]("") with FieldName {
    _fieldName = fieldName
}

class IntFieldName(var fieldName : String) extends F[Int](0) with FieldName {
    _fieldName = fieldName
}

trait FieldName {
    var _fieldName : String = ""
}
var x = Entry(F("a"), F(45))
var fakeEntry = x.getClass.getDeclaredConstructors()(0).newInstance(Array[AnyRef](new StringFieldName("c"), new IntFieldName("d")):_*).asInstanceOf[Entry]
println(fakeEntry.x.asInstanceOf[FieldName]._fieldName)
 * 
 * Ca marche :)
 * 
 * Après il faut juste instancier correctement  
 */

case class ForeignKey(fieldName: String)

object ForeignKeyGenerate extends ForeignKey("")

object RowCount extends Enumeration {
    val All, One, Opt = Value
}

object FieldNameConversion {
    def attributeToField(attributeName: String): String = {
        var tmpName = if (attributeName.endsWith("Id")) {
            attributeName.substring(0, attributeName.size - 2) + "_id"
        } else {
            attributeName
        }
        var fieldName = new StringBuffer()
        tmpName.foreach {
            c =>
                if (c.isUpper) {
                    fieldName.append('_')
                    fieldName.append(c.toLower)
                } else {
                    fieldName.append(c)
                }
        }
        fieldName.toString
    }
}

abstract class Table[T](tableNameOpt: Option[String] = None)(implicit t: reflect.Manifest[T]) {
    val idColumn = "id"
    val tableName = tableNameOpt.getOrElse(t.toString.split("[.]").last)
    val tablePrefix = tableName.toLowerCase + "."
    val compagnionClass = t.erasure
    val constructor = compagnionClass.getDeclaredConstructors()(0)
    val fields = compagnionClass.getDeclaredFields
    val nbFields = constructor.getGenericParameterTypes().size
    val constructorFieldNames: Array[String] = fields.slice(0, nbFields) map {
        field =>
            if (field.isAnnotationPresent(classOf[Attribute])) {
                field.getAnnotation(classOf[Attribute]).value()
            } else {
                FieldNameConversion.attributeToField(field.getName())
            }
    }

    // Row parser
    val single = defaultRowParser

    private def defaultRowParser: RowParser[T] = RowParser.apply[T]({
        r: Row =>
            val rowMap = r.asMap
            var constructorArgs: Array[java.lang.Object] = constructorFieldNames.map {
                fieldName =>
                    rowMap(tablePrefix + fieldName.toLowerCase()).asInstanceOf[java.lang.Object]
            }
            try {
                var entry = constructor.newInstance(constructorArgs: _*).asInstanceOf[T]
                if (idColumn != "") {
                    val field = entry.getClass.getDeclaredField(idColumn)
                    field.setAccessible(true)
                    field.set(entry, rowMap(tablePrefix + idColumn))
                }
                Success(entry)
            } catch {
                case e: IllegalArgumentException => {
                    throw new Exception("Unable to map " + constructorFieldNames.mkString("(", ",", ")") +
                      " to " + constructorArgs.mkString("(", ",", ")" +
                      " for constructor " + constructor), e)
                }
            }
    })

    /**
     * INSERT
     */
    def getOrCreate(params: (Any, ParameterValue[_])*): Long = {
        // val params = getSeqFromTableEntry(entry)
        val id = findIdWhere(params: _*)
        if (id.isDefined) {
            id.get
        } else {
            createEntry(params: _*)
        }
    }

    def createEntry(params: (Any, ParameterValue[_])*): Long = DB.withConnection {
        implicit c =>
            val query = "INSERT INTO " + tableName + "(" + mergeParamsInsert(",", params: _*) + ") VALUES ({" + mergeParamsInsert("},{", params: _*) + "})"
            SQL(query).on(params: _*).executeInsert().get
    }

    /**
     * FIND
     */
    def findIdWhere(params: (Any, ParameterValue[_])*): Option[Long] = DB.withConnection {
        implicit connection =>
            val query = "SELECT " + idColumn + " FROM " + tableName + " WHERE " + andQuery(params: _*)
            SQL(query).on(params: _*).as(scalar[Long].singleOpt)
    }

    def find(params: (Any, ParameterValue[_])*) = DB.withConnection {
        implicit connection =>
            query(Where(params)).as(single *)
    }

    def findOption(params: (Any, ParameterValue[_])*) = DB.withConnection {
        implicit connection =>
            query(Where(params)).as(single.singleOpt)
    }

    def findOne(params: (Any, ParameterValue[_])*) = DB.withConnection {
        implicit connection =>
            query(Where(params)).as(single.single)
    }

    def byId(id: Long) = DB.withConnection {
        implicit connection =>
            query(Where(Seq('id -> id))).as(single.singleOpt)
    }

    /**
     * SELECT QUERY
     */
    def select(q: Query = EmptyQuery) = DB.withConnection {
        implicit connection => query(q).as(single *)
    }

    def selectOne(q: Query = EmptyQuery) = DB.withConnection {
        implicit connection => query(q).as(single.single)
    }

    def selectOption(q: Query = EmptyQuery) = DB.withConnection {
        implicit connection => query(q).as(single.singleOpt)
    }

    def all(query: Query = EmptyQuery) = select(query)

    /**
     * DELETES
     */
    def deleteAll() = DB.withConnection {
        implicit c =>
            SQL("DELETE FROM " + tableName).executeUpdate()
    }

    def deleteWhere(params: (Any, ParameterValue[_])*): Long = DB.withConnection {
        implicit c =>
            SQL("DELETE FROM " + tableName + " WHERE " + andQuery(params: _*)).on(params: _*).executeUpdate()
    }

    def delete(id: Long) = deleteWhere('id -> id)

    /**
     * UPDATES
     */
    def update(id: Long, params: (Any, ParameterValue[_])*) = DB.withConnection {
        implicit c =>
            SQL("UPDATE " + tableName + " SET " + mergeParams(", ", params: _*) + " WHERE " + idColumn + " = " + id).on(params: _*).executeUpdate()
    }

    def updateWhere(where: Where, params: (Any, ParameterValue[_])*) = DB.withConnection {
        implicit c =>
            var allParams = new LinkedList[(Any, ParameterValue[_])]()
            allParams = allParams.++:(params)
            allParams = allParams.++:(where.where)
            SQL("UPDATE " + tableName + " SET " + mergeParams(", ", params: _*) + " WHERE " + andQuery(where.where: _*)).on(allParams: _*).executeUpdate()
    }

    /**
     * COUNT
     */
    def countWhere(params: (Any, ParameterValue[_])*): Long = DB.withConnection {
        implicit c =>
            SQL("SELECT COUNT(*) FROM " + tableName + " WHERE " + andQuery(params: _*)).on(params: _*).as(scalar[Long].single)
    }

    def count(): Long = DB.withConnection {
        implicit c =>
            SQL("SELECT COUNT(*) FROM " + tableName).as(scalar[Long].single)
    }

    /**
     * Methods for OneToMany and ManyToOne relations  
     */
    def oneToMany[FT](fTable: Table[FT], foreignKey: ForeignKey = ForeignKeyGenerate)(implicit instance: T): List[FT] = DB.withConnection {
        implicit c =>
            val foreignKeyField = foreignKey match {
                case ForeignKeyGenerate => tableName + "_id"
                case _ => foreignKey.fieldName
            }
            val primaryKeyField = compagnionClass.getDeclaredField(idColumn)
            primaryKeyField.setAccessible(true)
            fTable.find(foreignKeyField -> primaryKeyField.get(instance))
    }

    def manyToOneOpt[FT](fTable: Table[FT], foreignKey: ForeignKey = ForeignKeyGenerate)(implicit instance: T): Option[FT] = DB.withConnection {
        implicit c =>
            val foreignKeyField = foreignKey match {
                case ForeignKeyGenerate => fTable.tableName + "_id"
                case _ => foreignKey.fieldName
            }
            val field = compagnionClass.getDeclaredField(foreignKeyField)
            field.setAccessible(true)
            val foreignKeyValue = field.get(instance)
            fTable.byId(field.get(instance).asInstanceOf[Long])
    }

    def manyToOne[FT](fTable: Table[FT], foreignKey: ForeignKey = ForeignKeyGenerate)(implicit instance: T): FT = manyToOneOpt(fTable, foreignKey)(instance).get

    /**
     * PRIVATE
     */
    private def query(query: Query = EmptyQuery): SimpleSql[_] = DB.withConnection {
        implicit connection =>
            SQL("SELECT * FROM " + tableName + query.toSqlStatement).on(query.params: _*)
    }


    private def mergeParamsInsert(separator: String, params: (Any, ParameterValue[_])*): String = {
        if (params.isEmpty) {
            ""
        } else {
            params.map {
                case (s: Symbol, v) => s.name
                case (k, v) => k.toString
            }.mkString(separator)
        }
    }

    private def mergeParams(separator: String, params: (Any, ParameterValue[_])*): String = {
        if (params.isEmpty) {
            ""
        } else {
            params.map {
                case (s: Symbol, v) => s.name + " = {" + s.name + "}"
                case (k, v) => k.toString + " = {" + k.toString + "}"
            }.mkString(separator)
        }
    }

    private def andQuery(params: (Any, ParameterValue[_])*): String = mergeParams(" AND ", params: _*)

}