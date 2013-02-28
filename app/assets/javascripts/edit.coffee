# Display or not the changed label if the field has been updated
switchUpdated = (field, fieldLabel) ->
  if $(field).val() != $(field).attr("prev")
    $(fieldLabel).show()
  else
    $(fieldLabel).hide()

defaultCheckChanges = (field) ->
  $(field).val() != $(field).attr("prev")

switchUpdated = (field, test = defaultCheckChanges) ->
  if test(field)
    $(field + "_label_updated").show()
  else
    $(field + "_label_updated").hide()

recordChanges = (field, test = defaultCheckChanges) ->
  $(field).keyup(->
    switchUpdated(field, test)
  )
  $(field).change(->
    switchUpdated(field, test)
  )

recordChanges("#description")
recordChanges("#publisher")
recordChanges("#startedOn")
recordChanges("#type")
recordChanges("#region")


# Deal with the changes on the short name or the long name
nameChanged = () ->
  if $("#name").val() != $("#name").attr("prev") || $("#shortName").val() != $("#shortName").attr("prev")
    $("#label_name_updated").show()
  else
    $("#label_name_updated").hide()

$("#name").keyup(nameChanged)
$("#name").change(nameChanged)
$("#shortName").keyup(nameChanged)
$("#shortName").change(nameChanged)

checkChanges = () ->
  prevValue = $("#id").attr("prev")
  value = $("#id").val()


checkSubfieldsUpdated = () ->
  prevIds = $("#subFields").attr("prev").split(",").sort().join(",")
  curIds = $("#subFields").val()
  if(prevIds == curIds)
    $("#subFields_label_updated").hide()
  else
    $("#subFields_label_updated").show()

$("#resetYear").click(->
  $("#startedOn").val(0)
)


# Remove subfield action
# Attached to window to get global access
window.removeSubfield = (id) ->
  subFieldIds = $("#subFields").val().split(",")
  idx = subFieldIds.indexOf("#{id}")
  if(idx != -1)
    subFieldIds.splice(idx, 1)
  $("#subFields").val(subFieldIds.join(","))
  checkSubfieldsUpdated()


# Add subfield action
# Update the subfields field
$("#subfield_field").change(->
  label = $("#subfield_field :selected").text()
  id = $("#subfield_field").val()

  subFieldIds = $("#subFields").val().split(",")
  if(id not in subFieldIds && id != "-1")
    subFieldIds.push(id)
    subFieldIds.sort()

    if($("#subFields").val() == "")
      $("#subFields").val(id)
    else
      $("#subFields").val(subFieldIds.join(","))

    $("#subFieldList").append("<div class='subfield' id='subfield_#{id}'>#{label}<button type='button' class='close' data-dismiss='alert'  onclick='removeSubfield(#{id});'>×</button></div>")

    $("#subfield_field").val("-1")

    checkSubfieldsUpdated()
)

# show change history
$("#showHistory").click(->
  action = if $("#showHistory").text() == "View all" then "all" else "limited"

  $("#showHistory").text(if(action == "all") then "Reduce" else "View all")
  $.ajax(
    url: '/history/' + action + '/' + $("#id").val()
    type: 'GET'
    success: (data) ->
      $('#history').html(data)
    error: (data) ->
      message.addError(data.responseText)
    complete: (data, x, e) ->
  )
)