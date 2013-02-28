package controllers

import org.apache.commons.lang3.RandomStringUtils

import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import controllers.AuthenticationActions._

/**
 * In order to increase security against session corruption, we should keep a session in the server with the logged users.
 * This session should expire after one hour of inactivity.
 * If a user use a session key which is not in the cache, ask him to re log
 */

object User extends Controller {
    val userForm = Form(tuple("email" -> text, "password" -> text))

    def login = Action {
        implicit request =>
            userForm.bindFromRequest.fold(
                errors => BadRequest("Unable to login, check that you typed a valid email address"),
                params => params match {
                    case (email, password) =>
                        val loggedUser = models.User.authenticate(email, password)
                        if (loggedUser.isDefined) {
                            val userKey = loggedUser.get.key
                            /*
                            AuthenticationActions.userCache(userKey) = Akka.system.scheduler.scheduleOnce(AuthenticationActions.sessionDuration) {
                              AuthenticationActions.userCache.remove(userKey)
                           }
                           */
                            Ok("You successfully logged in").withSession("userKey" -> loggedUser.get.key)
                        } else {
                            BadRequest( """Unable to login, check that the mail and password are correct. If you lost your password click <a href="/user/lostPasword">here</a>""")
                        }
                }
            )
    }

    def logout = Action {
        implicit request =>
            Ok("You logged out").withNewSession
    }

    def add = Action {
        implicit request =>
            userForm.bindFromRequest.fold(
                errors => BadRequest("Unable to create an account, check that you typed a valid email address"),
                user => {
                    user match {
                        case (email, password) => {
                            // Create a new user if not existing
                            if (!models.User.findByEmail(email).isDefined && !models.User.authenticate(email, password).isDefined) {
                                val pseudo = email.split("@")(0)
                                models.User.create(email, password, pseudo)
                                val loggedUser = models.User.authenticate(email, password)
                                Ok("Account succesfully created").withSession("userKey" -> loggedUser.get.key)
                            } else {
                                BadRequest("Sorry, a user is already registered with this email address")
                            }
                        }
                    }
                })
    }

    def resetPasswordRequest(mailAddress: String) = Action {
        implicit request =>
        // A new password has been generated and send to your mail address. Click on the link in the message to update your password. You will then be able to access your account using this new password. If you want to change it, go to your profile page once connected.
            val newPassword = RandomStringUtils.randomAlphanumeric(16)

            val user = models.User.findByEmail(mailAddress)
            if (user.isDefined) {
                val newKey = models.User.hash(user.get.email.toLowerCase() + newPassword)

                // Add an entry into a resetpassord table
                // get a unique key identifying the password reset query

                // Mail object : [colour-ranking.org] Password reset request
                /* A password reset request has been performed using your email account on http://colour-ranking.org
                 *
                 * The following password has been generated: newPassword
                 * In order to ensure that you are at the origin of the request, to update your password click on the following link:
                 *
                 * http://colour.org/resetpassword?mail=yourmail&key=newKey&request=requestKey
                 *
                 * This link will be valid during one week.
                 *
                 * You will then be able to connect to your account using this email address and the generated password.
                 * We advise you to change it by going to your profile page once connected.
                 *
                 * If you are not at the origin of this request, please accept our apologies and delete this message.
                 *
                 * Regards,
                 */
            }
            Ok
    }

    def profile() = Authentication {
        implicit request =>
            if (request.user.isDefined) {
                Ok(views.html.staticpages.profile(request.user.get))
            } else {
                Ok(views.html.index(models.Conference.count()))
            }
    }

    val pseudoForm = Form("pseudo" -> text)

    def updatePseudo = Authentication {
        implicit request =>
            if (request.user.isDefined) {
                pseudoForm.bindFromRequest.fold(
                    errors => BadRequest("Unable to update the pseudo"),
                    params => params match {
                        case (pseudo) =>
                            request.user.get.updatePseudo(pseudo)
                            Ok("Identification information updated")
                    }
                )
            } else {
                BadRequest("You must be logged to update your pseudo")
            }
    }

    val privacyFormForm = Form("isPublicProfile" -> boolean)

    def updatePrivacy = Authentication {
        implicit request =>
            if (request.user.isDefined) {
                privacyFormForm.bindFromRequest.fold(
                    errors => BadRequest("Unable to update privacy information"),
                    params => params match {
                        case (isPublicProfile) =>
                            request.user.get.updatePrivacy(isPublicProfile)
                            val message = if (isPublicProfile) "Your profile is now public" else "Your profile is now private"
                            Ok(message)
                    }
                )
            } else {
                Ok(views.html.index(models.Conference.count()))
            }
    }


    val passwordForm = Form(tuple("curPassword" -> text, "newPassword" -> text, "rePassword" -> text))

    def updatePassword = Authentication {
        implicit request =>
            if (request.user.isDefined) {
                passwordForm.bindFromRequest.fold(
                    errors => BadRequest("Unable to update the password"),
                    params => params match {
                        case (curPassword, newPassword, rePassword) =>
                            // request.user.get.updatePseudo(pseudo)
                            val user = request.user.get
                            if (models.User.authenticate(user.email, curPassword).isDefined) {
                                val newKey = user.updatePassword(newPassword)
                                Ok("Password updated").withSession("userKey" -> newKey)
                            } else {
                                BadRequest("Sorry, your current password is not matching")
                            }
                    }
                )
            } else {
                Ok(views.html.index(models.Conference.count()))
            }
    }


    def updateResetedPassword(mailAddress: String, key: String, requestKey: String) = Action {
        implicit request =>
            Ok
    }
}