$("#btn_show_new_url").click(->
  $(".add_url_form").slideToggle("fast")
)


$("#submit_add_url").click(->
  $.ajax(
    url: '/link'
    type: 'POST'
    data:
      url: $("#input_form_add_url").val()
      conference_id: $("#id").val()
      label: $("#formAddUrlLabel").val()
    success: (data) ->
      $('#confLinks').append(data)
      # $('#links_container').html(data)
      $(".add_url_form").slideToggle("fast")
    error: (data) ->
      message.addError(data.responseText)
    complete: (data, x, e) ->
  )
)

window.deleteUrl = (id) ->
  $.ajax({
  type: 'DELETE',
  url: '/link/' + id
  success: (data) ->
    $('#confLinks').append(data)
  });
  $('#link_' + id).hide("slow")

