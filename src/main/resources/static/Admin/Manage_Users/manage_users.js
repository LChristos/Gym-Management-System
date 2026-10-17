
window.onload = fetchdata();
function fetchdata(){
    fetch('http://localhost:8080/manageusers')
    .then((response) => response.json())
    .then(users => display_users(users))
    .catch(error => console.error('Error:' , error))
}

function display_users(users) {
    let list_people = document.getElementById('list_people');
    list_people.innerHTML = ''; // Clear the content

    // Create the table
    let table = document.createElement('table');
    table.className = 'users_table';

    //table's headers
    let tableHeader = `
        <thead>
            <tr>
                <th>Name</th>
                <th>Surname</th>
                <th>Country</th>
                <th>Town</th>
                <th>Address</th>
                <th>Email</th>
                <th>Username</th>
                <th>Role</th>
                <th>Actions</th>
            </tr>
        </thead>
    `;
    table.innerHTML = tableHeader;

    // table's body
    let table_body = document.createElement('tbody');
    Object.values(users).forEach(user => {
        let user_row = document.createElement('tr');
        user_row.className = 'user_data';
        let formId = user.id;
        // Create the user details HTML
        let user_details = `
                <td><input type="text" name="name" id="name_${user.id}" value="${user.name}" form="${formId}"/></td>
                <td><input type="text" name="surname" id="surname_${user.id}" value="${user.surname}" form="${formId}"/></td>
                <td><input type="text" name="country" id="country_${user.id}" value="${user.country}" form="${formId}"/></td>
                <td><input type="text" name="town" id="town_${user.id}" value="${user.town}" form="${formId}"/></td>
                <td><input type="text" name="address" id="address_${user.id}" value="${user.address}" form="${formId}"/></td>
                <td><input type="email" name="email" id="email_${user.id}" value="${user.email}" form="${formId}"/></td>
                <td><input type="text" name="username" id="username_${user.id}" value="${user.username}" form="${formId}"/></td>
                <td>
                    <select name="role" id="role_${user.id}" form="${formId}">
                        <option value="user" ${user.role === 'user' ? 'selected' : ''}>user</option>
                        <option value="admin" ${user.role === 'admin' ? 'selected' : ''}>admin</option>
                    </select>
                </td>
                <td>
                    <button type="submit" form="${formId}">Change</button>
                    <button onclick="Delete_user('${user.id}')">Delete</button>
                </td>`;

        user_row.innerHTML = user_details;
        table_body.appendChild(user_row);

        //Creation of the form for users changes
        let form_data = document.createElement('form');
        form_data.id = formId;
        form_data.action = "http://localhost:8080/manageusers/change";
        form_data.method = "post";
        form_data.style.display = "none";
        table_body.appendChild(form_data);

    });

    table.appendChild(table_body);
    list_people.appendChild(table);

    Object.values(users).forEach(user => {
        let change_user = document.getElementById(user.id);
        if (change_user){
            document.getElementById(user.id).addEventListener('submit' , function(event) {
                event.preventDefault();
                let data = {
                    id: user.id,
                    name: document.getElementById(`name_${user.id}`).value,
                    surname: document.getElementById(`surname_${user.id}`).value,
                    country: document.getElementById(`country_${user.id}`).value,
                    town: document.getElementById(`town_${user.id}`).value,
                    address: document.getElementById(`address_${user.id}`).value,
                    email: document.getElementById(`email_${user.id}`).value,
                    username: document.getElementById(`username_${user.id}`).value,
                    role: document.getElementById(`role_${user.id}`).value
                };

                console.log(data);
                // Sending data via fetch API
                csrfReady.then(() =>
                    fetch('http://localhost:8080/manageusers/change', {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json' ,// Required for POST
                            'X-CSRF-TOKEN': csrfToken
                        },
                        body: JSON.stringify(data)
                    })
                )
                .then(response => {
                    if(!response.ok){
                        throw new Error(`HTTP error: ${response.status}`);
                    }
                    return response.text()})
                .then(message => {
                    alert(message);
                    fetchdata();
                })
                .catch((error) => {
                    console.error('Error:', error);
                });
            });
        }
    });

}


function Delete_user(userId){
    csrfReady.then(() =>
        fetch('http://localhost:8080/manageusers/delete/' + userId,
            { method: 'DELETE' ,
                headers: {'X-CSRF-TOKEN': csrfToken}})
    )
    .then(response => response.text())
    .then(message => {
        alert(message);
        fetchdata();  //Refresh list
    })
    .catch(error => console.error('Error:', error));
}