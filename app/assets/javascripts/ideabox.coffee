$("#toggleShowNewIdea").click(->
  $("#newIdea").slideToggle()
)

$("#page_container").show()
$('#page_container').pajinate()

$("button.voteUp").click((e) ->
  ideaId = $(this).attr("for")
  curElement = $(this)
  console.log(ideaId)
  $.ajax(
    url: '/ideabox/vote/' + ideaId
    type: 'POST'
    success: (data) ->
      newNbVotes = parseInt($("#nbVotes_" + ideaId).text()) + 1
      $("#nbVotes_" + ideaId).text(newNbVotes)
      curElement.addClass("disabled")
      curElement.children().removeClass("icon-thumbs-up").addClass("icon-heart")
      $('#page_container').pajinate()
    error: (data) ->
      message.addError(data.responseText)
    complete: (data, x, e) ->
  )
)

