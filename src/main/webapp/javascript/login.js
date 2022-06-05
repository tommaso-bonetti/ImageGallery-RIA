/**
 * Login and registration handler
 */

(function() { // avoid variables ending up in the global scope
  document.getElementById('loginButton').addEventListener('click', e => {
    let form = document.getElementById('loginForm');
    if (form.checkValidity()) {
      sendAsync('POST', 'AuthenticateUser', form, function(x) {
        if (x.readyState == XMLHttpRequest.DONE) {
          let message = x.responseText;
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
  
  let update = function (errors) {
		let errorContainer = document.getElementById('registerErrorMsg');
		errorContainer.innerHTML = '';
		
		errors.forEach(e => {
			let p = document.createElement('p');
			p.textContent = e;
			errorContainer.appendChild(p);
		});
	};
  
  document.getElementById('registerButton').addEventListener('click', e => {
    let form = document.getElementById('registerForm');
    if (form.checkValidity()) {
			let usernamePattern = /^[a-zA-Z][a-zA-Z0-9_.-]*[a-zA-Z0-9]$/;
			let emailPattern = /^[a-zA-Z][a-zA-Z0-9_.-]+[a-zA-Z0-9]@[a-zA-Z][a-zA-Z0-9.-]+[a-zA-Z]$/;
			
			let username = form.elements['username'].value;
			let email = form.elements['email'].value;
			let password = form.elements['password'].value;
			let repeatPassword = form.elements['repeatPassword'].value;
			
			let errors = [];
			
			if (!usernamePattern.test(username))
				errors.push('Username can only contain letters, numbers, underscores, dots and hyphens, must start with a letter and end with a letter or number');
			
			if (!emailPattern.test(email))
				errors.push('Invalid email format');
				
			if (password.length < 6)
				errors.push('Password needs to be at least 6 characters long');
			
			if (password !== repeatPassword)
				errors.push('Password and repeat password do not match');
			
			if (errors.length > 0) {
				update(errors);
				console.log('Validity check failed - registration');
				return;
			}
	
      sendAsync('POST', 'RegisterUser', form, function(x) {
        if (x.readyState == XMLHttpRequest.DONE) {
          let message = x.responseText;
          switch (x.status) {
            case 200:
          		sessionStorage.setItem('username', message);
              window.location.href = "home.html";
              break;
            case 400: // bad request
              update(JSON.parse(message));
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