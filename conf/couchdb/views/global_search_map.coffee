(doc) ->
  words = [doc.title] if(doc.title)
  words = words.concat doc.ingredients  if(doc.ingredients)
  words = words.concat doc.keywords  if(doc.keywords)

  emit(word.toLowerCase()) for word in words.unique()