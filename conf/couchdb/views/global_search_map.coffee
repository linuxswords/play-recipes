(doc) ->

  Array::unique = ->
    output = {}
    output[@[key]] = @[key] for key in [0...@length]
    value for key, value of output


  words = [doc.title] if(doc.title)
  words.push(ing.name.toLowerCase()) for ing in  doc.ingredients if(doc.ingredients)
  words.push(word.toLowerCase()) for word in doc.keywords  if(doc.keywords)

  emit word.toLowerCase(), doc.title for word in words.unique()