(doc) ->
  emit doc.title.toLowerCase() if doc.title
  if(doc.title && doc.ingredients && doc.ingredients.length > 0)
    emit ingredient.name.toLowerCase(), doc.title for ingredient in doc.ingredients
  if(doc.title && doc.keywords && doc.keywords.length > 0)
    emit keyword.toLowerCase() doc.title for keyword in doc.keywords