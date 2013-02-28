$('.typeAhead').typeahead(
  source: (query, process) ->
    $.get("/search/json", query: query, (data) ->
      process(data.options))
)

$("#displayAdvancedSearch").click(->
  $("#advancedSearch").slideToggle()
)

window.toggleLongText = (key) ->
  $("#longTextDots_" + key).toggle()
  $("#longText_" + key).toggle()
  moreText = if $("#longTextShow_" + key).text() == " Less" then " More" else " Less"
  $("#longTextShow_" + key).text(moreText)

window.message = {
addMessage: (message, panel = "#messageAreaTop", timed = true, type, strong) ->
  $(panel).append("""<div class="alert #{type}">
                  <button class="close" data-dismiss="alert">×</button>
                  <strong>#{strong}</strong> #{message}
                  </div>""")
  # $("div.alert").delay(1000).slideToggle("slow", -> )
  $("div.alert").delay(5000).hide("clip")
addInfo: (message, panel = "#messageAreaTop", timed = true) ->
  this.addMessage(message, panel, timed, "alert-info", "Info:")
addSuccess: (message, panel = "#messageAreaTop", timed = true) ->
  this.addMessage(message, panel, timed, "alert-success", "Success:")
addWarning: (message, panel = "#messageAreaTop", timed = true) ->
  this.addMessage(message, panel, timed, "", "Warning:")
addError: (message, panel = "#messageAreaTop", timed = true) ->
  this.addMessage(message, panel, timed, "alert-error", "Error:")
}

window.ajaxForm = (url, fields) ->
  console.log("test")
  params = {}
  for field in fields
    f = $("#" + field)
    console.log(f)
    if (f.is(':checkbox'))
      console.log(f.is(':checked'))
      params[field] = f.is(':checked')
    else
      params[field] = f.val()
  $.ajax(
    url: url
    type: 'POST'
    data: params
    success: (data) ->
      message.addSuccess(data + "")
    error: (data) ->
      message.addError(data.responseText)
    complete: () ->
  )

jQuery(document).ready(->
  jQuery("time.timeago").timeago()
  prettyPrint()
)

$("#btn_show_new_conf").click(-> $("#form_new_conf").toggle("fast"))

$('div').tooltip();
$('#test').tooltip({'trigger': 'focus', 'title': 'Password tooltip'});
$("#test").tooltip(
  animation: false
  placement: top
)

