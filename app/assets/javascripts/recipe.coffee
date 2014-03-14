$ ->
  singleTemplate = Handlebars.compile($('#singleRecipe').html())

  $('.ingredients').on 'click', '.addIngredient', (e) ->
    e.preventDefault()
    count = $('.ingredients li').length
    console.log("we have #{count + 1} ingredients")
    li = $(this).closest('li').clone(true).appendTo('.ingredients').find('input').each () ->
      this.name = this.name.replace(/\d+/, count)
      this.id = this.id.replace(/\d+/, count)

  $('.recipelist').on 'click', '.recipetitle', (e) ->
    e.preventDefault()
    title = $(this).data('id')
    showSingle(title)

  showSingle = (id) ->
    jsRoutes.controllers.Recipe.byId(id).ajax({
      success: (recipe) ->
        content = singleTemplate(recipe)
        $('.showrecipe').html(content)

    })


  jsRoutes.controllers.Recipe.allTitle().ajax({
    success: (rows) ->
      $('.recipelist').append("<li class='recipetitle' data-id='#{row.id}'>#{row.key}</li>") for row in rows
  })
