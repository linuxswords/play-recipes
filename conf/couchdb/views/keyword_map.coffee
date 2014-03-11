(doc) ->
  if(doc.title && doc.keywords && doc.keywords.length > 0)
    emit keyword.toLowerCase(), doc.title for keyword in doc.keywords