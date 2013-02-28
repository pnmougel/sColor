$("#run").click(->
  actionName = $("#run").attr("action")

  $.ajax(
    url: "/admin/action/run/" + actionName
    type: 'GET'
    success: (data) ->
    error: (data) ->
    complete: () ->
  )

  setInterval(->
    updateProgressBar(actionName)
    1000)
  setInterval(->
    updateMessage(actionName)
    1000)
)

updateProgressBar = (actionName) ->
  $.ajax(
    url: "/admin/action/percentage/" + actionName
    type: 'GET'
    success: () ->
    error: (data) ->
    complete: (data) ->
      $("#actionProgress").attr("style", "width:" + data.responseText + "%;")
  )

updateMessage = (actionName) ->
  $.ajax(
    url: "/admin/action/message/" + actionName
    type: 'GET'
    success: (data) ->
      if(data == "clear")
        $("#message_area").empty()
      else
        content = $(data)
        $("#message_area").prepend(content.filter(".logMessage"))
        $("#mainContent").append(content.not(".logMessage"))
        initBindings()
    error: (data) ->
    complete: () ->

  )


initBindings = () ->
  $("button.sameAsPublication").unbind('click')
  $("button.createNewPublication").unbind('click')

  $("button.sameAsPublication").click(->
    publicationId = $(this).attr("for")
    mergeId = $(this).attr("mergeId")
    console.log("Clicked !!")

    $.ajax(
      url: "/admin/sameAsPublication"
      type: 'POST'
      data:
        publicationId: publicationId
        mergeId: mergeId
      success: (data) ->
        $("#merge_" + mergeId).remove()
      error: (data) ->
      complete: () ->
        $("#merge_" + mergeId).remove()
    )
  )

  $("button.createNewPublication").click(->
    mergeId = $(this).attr("mergeId")
    $.ajax(
      url: "/admin/createPublication"
      type: 'POST'
      data:
        mergeId: mergeId
      success: (data) ->
        $("#merge_" + mergeId).remove()
      error: (data) ->
      complete: () ->
        $("#merge_" + mergeId).remove()
    )
  )

initBindings()

