function makeCall(method, url, formElement, callback, reset = true) {
  var req = new XMLHttpRequest();
  req.onreadystatechange = function() {
    callback(req)
  };
  req.open(method, url);
  if (formElement == null) {
    req.send();
  } else {
    req.send(new FormData(formElement));
  }
  if (formElement !== null && reset === true) {
    formElement.reset();
  }
};