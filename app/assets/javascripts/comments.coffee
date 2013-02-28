###
Scripts for the comment actions
###


$("#comment_field").blur(->
  setTimeout(( ->
    $("#comment_field").attr("rows", 1)
    $("#submit_add_comment").hide()
  )
  , 500);
)


# Add a comment
$("#submit_add_comment").click(->
  $.ajax(
    url: '/comment'
    type: 'POST'
    data:
      conference_id: $("#conference_id").val()
      content: $("#comment_field").val()
    success: (data) ->
      $('#confComments').prepend(data)
      $("#comment_field").val("")
      $("#comment_field").attr("rows", 1)
      $("#nb_comments").text(parseInt($("#nb_comments").text()) + 1)
      $("#submit_add_comment").hide()
      jQuery("time.timeago").timeago()
      if($("#nb_comments").text() != "0")
        $("#noComments").hide()
    error: (data) ->
      message.addError(data.responseText)
    complete: (data, x, e) ->
      $('#confComments').prepend(data)
      $("#comment_field").val("")
      $("#comment_field").attr("rows", 1)
      # $("#nb_comments").text(parseInt($("#nb_comments").text()) + 1)
      $("#submit_add_comment").hide()
      jQuery("time.timeago").timeago()
      if($("#nb_comments").text() != "0")
        $("#noComments").hide()
  )
  false
)

# Display the comment form 
$("#comment_field").click(->
  if $("#comment_field").attr("rows") != "5"
    $("#submit_add_comment").show()
  $("#comment_field").attr("rows", 5)
)

window.deleteComment = (id) ->
  $.ajax({
  type: 'DELETE',
  url: '/comment/' + id
  success: (data) ->
    $("#nb_comments").text(parseInt($("#nb_comments").text()) - 1)
    $('#comment_' + id).hide("slow")
    if($("#nb_comments").text() == "0")
      $("#noComments").show()
  });
    

