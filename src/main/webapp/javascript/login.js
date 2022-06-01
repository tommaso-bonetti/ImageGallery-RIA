/**
 * Login and registration handler
 */

(function() { // avoid variables ending up in the global scope
  document.getElementById('loginButton').addEventListener('click', e => {
    var form = document.getElementById('loginForm');
    if (form.checkValidity()) {
      sendAsync('POST', 'AuthenticateUser', form, function(x) {
        if (x.readyState == XMLHttpRequest.DONE) {
          var message = x.responseText;
          switch (x.status) {
            case 200:
          		sessionStorage.setItem('username', message);
              window.location.href = 'home.html';
              break;
            case 400:	// bad request
            case 401:	// unauthorized
            case 500: // internal server error
              document.getElementById('loginErrorMsg').textContent = message;
              break;
            default:
            	document.getElementById('loginErrorMsg').textContent = 'Unexpected error';
          }
        }
      });
    } else {
    	form.reportValidity();
    }
  });
  
  var update = function (errors) {
		var errorContainer = document.getElementById('registerErrorMsg');
		errorContainer.innerHTML = '';
		
		errors.forEach(e => {
			var p = document.createElement('p');
			p.textContent = e;
			errorContainer.appendChild(p);
		});
	};
  
  document.getElementById('registerButton').addEventListener('click', e => {
    var form = document.getElementById('registerForm');
    if (form.checkValidity()) {
      sendAsync('POST', 'RegisterUser', form, function(x) {
        if (x.readyState == XMLHttpRequest.DONE) {
          var message = x.responseText;
          switch (x.status) {
            case 200:
          		sessionStorage.setItem('username', message);
              window.location.href = "home.html";
              break;
            case 400: // bad request
            	var errors = JSON.parse(message);
              update(errors);
              break;
            case 409, 500: // conflict, internal server error
              update([message]);
              break;
            default:
            	update(['Unexpected error']);
          }
        }
      });
    } else {
    	 form.reportValidity();
    }
  });
})();