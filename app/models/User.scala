package models

import anorm._
import java.security.MessageDigest
import java.math.BigInteger
import models.renorm.Table

case class User(email: String, key: String, pseudo: Option[String], isAdmin: Boolean, isPublic: Boolean, apiKey: String) {
    var id: Long = 0

    def updatePseudo(pseudo: String) = User.update(id, 'pseudo -> pseudo)

    def updatePrivacy(isPublic: Boolean) = User.update(id, 'is_public -> isPublic)

    def updatePassword(newPassword: String): String = {
        val newKey = User.hash(email + newPassword)
        User.update(id, 'key -> newKey)
        newKey
    }

    lazy val ideaLiked = IdeaVote.byUser(id)
}

object User extends Table[User](Option("IUser")) {

    val deletedUser = create("deletedUser", "", "")

    // -- Queries
    def findByEmail(email: String): Option[User] = findOption('email -> email)

    def hash(password: String): String = {
        val digest = MessageDigest.getInstance("SHA").digest(password.getBytes())
        val number = new BigInteger(1, digest)
        number.toString(16).toUpperCase()
    }

    def getByKey(key: String): Option[User] = {
        val curUser = findOption('key -> key)
        if (curUser.isDefined) {
            if (curUser.get.id != deletedUser) {
                curUser
            } else {
                None
            }
        } else {
            None
        }
    }

    def authenticate(email: String, password: String): Option[User] = findOption('key -> hash(email.toLowerCase() + password))

    def setPublicProfile(id: Long, isPublic: Boolean) = {
        update(id, 'is_public -> isPublic)
    }

    def create(email: String, password: String, pseudo: String, isAdmin: Boolean = false): Long = {
        createEntry('email -> email, 'pseudo -> pseudo, 'is_admin -> isAdmin, 'key -> hash(email + password),
            'is_public -> false, 'api_key -> "")
    }
}
