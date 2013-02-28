$("#publisher_field").eComboBox(
  inputFieldName: "Add new publisher"
  onRemoveChoice: (choice) ->
    $.ajax(
      url: '/publisher/' + choice
      type: 'DELETE'
    )

  onAddChoice: (choice) ->
    $.ajax(
      url: '/publisher'
      type: 'POST'
      data:
        name: choice
    )
)