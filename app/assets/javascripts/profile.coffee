$("#updatePseudo").click(-> ajaxForm("/user/update/pseudo", ["pseudo"]))

$("#updatePrivacy").click(-> ajaxForm("/user/update/privacy", ["isPublicProfile"]))

$("#updatePassword").click(->
  if(!$("#updatePassword").hasClass("disabled"))
    ajaxForm("/user/update/password", ["curPassword", "newPassword", "rePassword"])
)


checkPassword = () ->
  newPassword = $("#newPassword").val()
  rePassword = $("#rePassword").val()
  passwordOk = true
  if(newPassword == rePassword)
    $("#rePasswordGroup").removeClass("error")
    $("#rePasswordHelp").text("")
  else
    $("#rePasswordGroup").addClass("error")
    $("#rePasswordHelp").text("Passwords do not match")
    passwordOk = false

  if(newPassword.length >= 6)
    $("#newPasswordGroup").removeClass("error")
    $("#newPasswordHelp").text("")
  else
    $("#newPasswordGroup").addClass("error")
    $("#newPasswordHelp").text("The new password must be at least 6 characters long")
    passwordOk = false

  if(passwordOk == true)
    $("#updatePassword").removeClass("disabled")
  else
    $("#updatePassword").addClass("disabled")


$("#newPassword").keyup(checkPassword)

$("#rePassword").keyup(checkPassword)