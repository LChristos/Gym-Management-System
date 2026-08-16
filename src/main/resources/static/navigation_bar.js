let navbar = document.getElementById('navigation_bar');

let cookie_info = null;
let cookie_array = document.cookie.split(";");
for (let cookie of cookie_array) {
    cookie = cookie.trim();
    if (cookie.indexOf("token" + "=") === 0) {
        cookie_info = cookie.substring(6);
    }
}

if (cookie_info) {
    // Split the cookie value by the delimiter '|'
    let parts = cookie_info.split("|");
    let userId = parts[0];
    let role = parts[1];
    let accepted = parts[2];
    window.userId= userId;
    window.userRole = role;
    window.userAccept = accepted;
    if(role === "user"){
    //Χρήστες
        navbar.innerHTML = `
        <nav>
            <ul>
                <a href="/User/Show_Services/show_services.html">Υπηρεσίες</a>
                <a href="/User/Product_Reservations/make_reservation.html">Κρατήση προϊόντος/Υπηρεσίας</a>
                <a href="/User/Show_History/history_userreservation.html">Ιστορικό Κρατήσεων</a>
                <a href="/User/Show_Announcements/announcement.html">Νέα και Ανακοινώσεις</a>
                <button onclick="Logout()">Logout</button>
            </ul>
        </nav>`;
    }
    else if(role === "admin"){
    //Διαχειριστής
        navbar.innerHTML = `
        <nav>
            <ul>
                <a href="/Admin/Registration_Requests/users_request.html">Αιτήματα εγγραφών</a>
                <a href="/Admin/Manage_Users/manage_users.html">Διαχείριση Χρηστών</a>
                <a href="/Admin/Manage_System/manage_system.html">Διαχείριση Συστήματος</a>
                <a href="/Admin/Manage_Announcements/announcement.html">Νέα και Ανακοινώσεις</a>
                <button onclick="Logout()">Logout</button>
            </ul>
        </nav>`;
    }
    else{
        navbar.innerHTML = `
        <nav>
            <ul>
                <a href="/User/Show_Services/show_services.html">Υπηρεσίες</a>
                <button onclick="Logout()">Logout</button>
            </ul>
        </nav>`;
    }
}
else {
    navbar.innerHTML = `
    <nav>
        <ul>
            <a href="/User/Show_Services/show_services.html">Υπηρεσίες</a>
            <a href="/Login_Register/newuser.html">Login/Register</a>
        </ul>
    </nav>`;
    console.log("Cookie not found");
}

function Logout() {
    fetch('http://localhost:8080/logout' , {
        method: 'POST',
        credentials: 'include'
    })
    .then(response => response.text())
    .then(message => {
        alert(message);
        window.location.href = '/Login_Register/newuser.html'
    })
    .catch(error => console.error('Error:', error));
}


