(doc) ->
  if(doc.title && doc.ingredients && doc.ingredients.length > 0)
    emit ingredient.name.toLowerCase(), doc.title for ingredient in doc.ingredients