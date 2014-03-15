window.recipe = {}

window.recipe.showInfo = (message) ->
  $('.message').removeClass('bg-danger').addClass('bg-success').text(message).slideDown('slow')
window.recipe.showError = (message) ->
  $('.message').removeClass('bg-success').addClass('bg-danger').text(message).slideDown('slow')

$ ->
  singleTemplate = Handlebars.compile($('#singleRecipe').html())
  recipeTemplate = Handlebars.compile($('#recipeTemplate').html())

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
        content = recipeTemplate(recipe)
        $('#formHolder').html(content)
    })

  $('form.recipeForm').on 'submit', (e) ->
    e.preventDefault()
    data = $('form.recipeForm').serialize()
    jsRoutes.controllers.Recipe.submitForm().ajax({
      data: data,
      success: () ->
        recipe.showInfo('success')
      error: () ->
        recipe.showError('error')
    })


  jsRoutes.controllers.Recipe.allTitle().ajax({
    success: (rows) ->
      $('.recipelist').append("<li class='recipetitle' data-id='#{row.id}'>#{row.key}</li>") for row in rows
  })
