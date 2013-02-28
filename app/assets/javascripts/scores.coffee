$("#show_vote_form").click(->
  if $("#hasVoted").val() == "true"
    $("#vote_remove").slideToggle("slow")
  else
    $("#vote_form").slideToggle("slow")
)

# Handle vote
# Update color background change on mouseover
# Send the vote request
handleVote = (voteId, color, score) ->
  $(voteId).mouseenter(-> $(voteId).css("background-color", color).css('cursor', 'pointer'))
  $(voteId).mouseleave(-> $(voteId).css("background-color", '#F5F5F5'))
  $(voteId).click(->
    $.ajax(
      url: '/vote'
      type: 'POST'
      data:
        conference_id: $("#conference_id").val()
        score: score
      success: (data) ->
        $("#hasVoted").val('true')
        $("#user_scores").html(data)
      error: (data) ->
        message.addError(data.responseText)
    )
    $("#vote_form").slideToggle("slow")
  )

# Handle events for all ranks
handleVote("#vote_rankA", '#E6FFE6', 5)
handleVote("#vote_rankB", '#FFF4E6', 4)
handleVote("#vote_rankC", '#E6F7FF', 3)
handleVote("#vote_rankD", '#FFE6E6', 1)

$("#remove_vote").click(->
  $.ajax(
    url: '/vote'
    type: 'DELETE'
    data:
      conference_id: $("#conference_id").val()
    success: (data) ->
      $("#hasVoted").val('false')
      $("#user_scores").html(data)
      $("#vote_remove").slideToggle("slow")
  )
)