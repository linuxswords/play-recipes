$ ->
  $('.ingredients').on 'click', '.addIngredient', (e) ->
    e.preventDefault
    count = $('.ingredients li').length
    console.log("we have #{count + 1} ingredients")
    li = $(this).closest('li').clone(true).appendTo('.ingredients').find('input').each () ->
      this.name = this.name.replace(/\d+/, count)
      this.id = this.id.replace(/\d+/, count)


  jsRoutes.controllers.Recipe.allTitle().ajax({
    success: (rows) ->
      $('.recipelist').append("<li data-id='#{row.id}'>#{row.key}</li>") for row in rows
  })
