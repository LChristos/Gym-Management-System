let navbar = document.getElementById('navigation_bar');

fetch('/user')
.then(response => {
    if (!response.ok){
        throw new Error(`HTTP error: ${response.status}`);
    }
    return response.json()
})
.then(data => {
    const role = data.role;
    const username = data.username;
    const id = data.id;
    window.userId = data.id;
    window.userRole = data.role;
    window.dispatchEvent(new Event("userLoaded"));

    if ( role == "ADMIN"){
        //Διαχειριστής
        navbar.innerHTML = `
        <nav>
            <ul>
                <a href="/Admin/Registration_Requests/users_request.html">Αιτήματα εγγραφών</a>
                <a href="/Admin/Manage_Users/manage_users.html">Διαχείριση Χρηστών</a>
                <a href="/Admin/Manage_System/manage_system.html">Διαχείριση Συστήματος</a>
                <a href="/Admin/Manage_Announcements/announcement.html">Νέα και Ανακοινώσεις</a>
                <form id="logout" method="post" action="/logout"
                     style="display: inline-block; margin: 0;">
                    <button type="submit">Logout</button>
                    <input type="hidden" id="csrfToken" name="_csrf" value=""/>
                </form>
            </ul>
        </nav>`;
    }

    if (role == "USER"){
    //Χρήστες
        navbar.innerHTML = `
        <nav>
            <ul>
                <a href="/User/Show_Services/show_services.html">Υπηρεσίες</a>
                <a href="/User/Product_Reservations/make_reservation.html">Κρατήση προϊόντος/Υπηρεσίας</a>
                <a href="/User/Show_History/history_userreservation.html">Ιστορικό Κρατήσεων</a>
                <a href="/User/Show_Announcements/announcement.html">Νέα και Ανακοινώσεις</a>
                <form id="logout" method="post" action="/logout"
                    style="display: inline-block; margin: 0;">
                    <button type="submit">Logout</button>
                    <input type="hidden" id="csrfToken" name="_csrf" value=""/>
                </form>
            </ul>
        </nav>`;
    }


    if (role == "VISITOR"){
        if (username == null){
            navbar.innerHTML = `
            <nav>
                <ul>
                    <a href="/User/Show_Services/show_services.html">Υπηρεσίες</a>
                    <a href="/Login_Register/newuser.html">Login/Register</a>
                </ul>
            </nav>`;
        }
        else{//Not accepted user/admin
            navbar.innerHTML = `
            <nav>
                <ul>
                    <a href="/User/Show_Services/show_services.html">Υπηρεσίες</a>
                    <form id="logout" method="post" action="/logout"
                        style="display: inline-block; margin: 0;">
                        <button type="submit">Logout</button>
                        <input type="hidden" id="csrfToken" name="_csrf" value=""/>
                    </form>
                </ul>
            </nav>`;
        }
    }


    //Get the CSRF token
    if (document.getElementById("csrfToken") != null){
        fetch('http://localhost:8080/csrf' ,{
            method: 'GET' ,
        })
            .then(r => r.json())
            .then(data => {
                document.getElementById("csrfToken").value = data.token;
            });
    }
})
.catch(error => {
              alert(error.message);
              console.log('Error:' , error);
})
