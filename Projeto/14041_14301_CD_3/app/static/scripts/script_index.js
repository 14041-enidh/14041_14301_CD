var mailRegEx = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
var nameRegEx = /^[\d]{9}$/;
var passwordRegEx = /^[\w]{3,7}$/;

var xmlHttp;

function GetXmlHttpObject() {
    try {
      return new ActiveXObject("Msxml2.XMLHTTP");
    } catch(e) {} // Internet Explorer
    try {
      return new ActiveXObject("Microsoft.XMLHTTP");
    } catch(e) {} // Internet Explorer
    try {
      return new XMLHttpRequest();
    } catch(e) {} // Firefox, Opera 8.0+, Safari
    alert("XMLHttpRequest not supported");
    return null;
  }

function toggleForm() {
    const loginContainer = document.getElementById('login-container');
    const signupContainer = document.getElementById('signup-container');
    if (loginContainer.style.display === 'none') {
        loginContainer.style.display = 'block';
        signupContainer.style.display = 'none';
    } else {
        loginContainer.style.display = 'none';
        signupContainer.style.display = 'block';
    }
}

function validaMail(value) {
  
    return mailRegEx.test(value);
}
  
function validaPass(value) {
    
    return passwordRegEx.test(value);
}

  
function FormLoginValidator() {
	mailValue = document.getElementById( "emailID" ).value;
	passValue = document.getElementById( "passwordID" ).value;

  if ( validaPass( passValue )==false ) {
		alert( "Um dos campos é inválido" );
		return false;
	}

	if ( validaMail( mailValue )==false ) {
		alert( "Um dos campos é inválido" );
		return false;
	}
	
	return true;
}

function FormAccValidator() {
	SmailValue = document.getElementById( "signup-emailID" ).value;
	SpassValue = document.getElementById( "signup-passwordID" ).value;
    SpassValueConfirm = document.getElementById( "signup-confirm-passwordID" ).value;

    if ( validaPass( SpassValue )==false ) {
		alert( "Um dos campos é inválido" );
		return false;
	}

	if ( validaMail( SmailValue )==false ) {
		alert( "Um dos campos é inválido" );
		return false;
	}

    if (SpassValue != SpassValueConfirm ){
        alert( "As passwords nao sao iguais!" );
		return false;

    }
	
	return true;
}




