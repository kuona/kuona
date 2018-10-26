function commaListToArray(text, sepearator) {
  let elements = text.split(sepearator);
  let result = [];

  for (let i = 0; i < elements.length; i++) {
    result.push(elements[i].trim())
  }
  return result;
}
