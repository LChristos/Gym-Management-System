document.getElementById('show_register').addEventListener('click' , function(event) {// Change scene to Register Form
    event.preventDefault();
    document.getElementById('form_login').style.display = 'none';
    document.getElementById('form_register').style.display = 'block';
});

document.getElementById('show_login').addEventListener('click', function(event) {// Change scene to Login Form
    event.preventDefault();
    document.getElementById('form_register').style.display = 'none';
    document.getElementById('form_login').style.display = 'block';
});

//API for Country/Town
let url_api = 'https://countriesnow.space/api/v0.1/countries';

let country_list = document.getElementById("country");
let town_list = document.getElementById("town");

fetch(url_api)
.then((response) => response.json())
.then(data => {
    data.data.forEach(num => {
        let option = document.createElement("option");
        option.textContent = num.country;
        option.value = JSON.stringify(num.cities);
        country_list.appendChild(option);
    })
})
.catch(error => console.error('Error:' , error))

country_list.addEventListener('change' , () => {
    event.preventDefault();
    town_list.innerHTML = '<option value="">Select Town</option>';
    let cities_list = JSON.parse(country_list.value);
    cities_list.forEach(town => {
        let options = document.createElement("option");
        options.textContent = town;
        town_list.appendChild(options);
    })
})
//When a new person register
document.getElementById('register').addEventListener('submit' , function(event) {
    event.preventDefault();
    let country = document.getElementById('country');
    let town = document.getElementById('town');
    let town_text = town.options[town.selectedIndex].text;
    let country_text = country.options[country.selectedIndex].text;
    let data = {
        name: document.getElementById('name').value,
        surname: document.getElementById('surname').value,
        country: country_text,
        town: town_text,
        address: document.getElementById('address').value,
        email: document.getElementById('email').value,
        username: document.getElementById('register_username').value,
        password: document.getElementById('register_password').value
    };

    csrfReady.then(() =>
        fetch('http://localhost:8080/signup' , {
            method: 'POST' ,
            headers: {
                'Content-Type': 'application/json' ,
                'X-CSRF-TOKEN': csrfToken
            },
            body: JSON.stringify(data)
        })
    )
    .then(async response => {
        console.log("Status:", response.status);
        console.log("URL:", response.url);
        console.log("Redirected:", response.redirected);
        console.log("Content-Type:", response.headers.get("content-type"));

        const message = await response.text();
        console.log("Response:", message);

        if (!response.ok || response.redirected) {
            throw new Error(`Registration failed: ${response.status}`);
        }

        return message;
    })
    .then(message => {
        alert(message);
        //TRANSFER TO HOME
        document.getElementById('form_register').style.display = 'none';
        document.getElementById('form_login').style.display = 'block';})
    .catch(error => {
        alert(error.message);
        console.log('Error:' , error);
    });
});

if (new URLSearchParams(location.search).has('error')) {
    alert('Wrong Credentials');
}

//Get the CSRF token
fetch('http://localhost:8080/csrf' ,{
    method: 'GET' ,
})
.then(r => r.json())
.then(data => {
    document.getElementById("csrfToken").value = data.token;
});


//When someone login
/*document.getElementById('login').addEventListener('submit' , function(event) {
    event.preventDefault();
    let login_data= {
        username : document.getElementById('login_username').value,
        password: document.getElementById('login_password').value
    }

    fetch('http://localhost:8080/login' , {
        method: 'POST' ,
        headers: {
            'Content-Type': 'application/json'
        },
        credentials: 'include',
        body: login_data
    })
    .then(response => {
        return response.text().then(message => {
            if(!response.ok){
                throw new Error(message);
            }
            return message;
        });
    })
    .then(message => {
        alert(message);
        window.location.reload();
    })
    .catch(error => {
        alert(error.message);
        console.log('Error:' , error);
    })
})*/
//After the login what to do depends on role
if (window.userRole === "user") {
    window.location.href = "/User/Show_Announcements/announcement.html";
} else if (window.userRole === "admin") {
    window.location.href = "/Admin/Manage_Announcements/announcement.html";
} else if (window.userAccept === "false"){
    window.addEventListener("load", function() {
        alert("Ο Διαχειρηστής δεν σε έχει αποδεκτή ακόμα");
    });
}