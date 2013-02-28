###
Scripts for the conference page
###

window.selectText = (element) ->
  doc = document
  text = doc.getElementById(element)

  if (doc.body.createTextRange)
    range = doc.body.createTextRange()
    range.moveToElementText(text)
    range.select()
  else if (window.getSelection)
    selection = window.getSelection()
    range = doc.createRange()
    range.selectNodeContents(text);
    selection.removeAllRanges();
    selection.addRange(range);


$("#getTheBadge").click(->
  $('#myModal').modal('show')
)

$("#selectUrl").click(->
  selectText("widgetUrl")
)
