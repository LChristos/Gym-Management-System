
window.onload = fetchdata();
function fetchdata(){// Get all the user that the admin has not accept or reject
    fetch('/request')
    .then((response) => response.json())
    .then(users => display_users(users))
    .catch(error => console.error('Error:' , error))
}

function display_users(users) {
    let request_list = document.getElementById('request_list');
    request_list.innerHTML = ''; // Clear the element

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
        //User's data
        let user_details = `
            <td>${user.name}</td>
            <td>${user.surname}</td>
            <td>${user.country}</td>
            <td>${user.town}</td>
            <td>${user.address}</td>
            <td>${user.email}</td>
            <td>${user.username}</td>
            <td>
                <button onclick="accept_user('${user.id}')">Accept</button>
                <button onclick="reject_user('${user.id}')" style="background-color: red;">Reject</button>
            </td>`;

        user_row.innerHTML = user_details;
        table_body.appendChild(user_row);
    });

    table.appendChild(table_body);
    request_list.appendChild(table);
}

function accept_user(userId) {//To accept the user
    csrfReady.then(() =>
        fetch('/request/accepted/' + userId, {
            method: 'POST' ,
            headers: {'X-CSRF-TOKEN': csrfToken}})
    )
    .then(response => {
        if(!response.ok){
            throw new Error(`HTTP error: ${response.status}`);
        }
        return response.text()})
    .then(message => {
        alert(message);
        fetchdata(); //Refresh list
    })
    .catch(error => console.error('Error:', error));
}

function reject_user(userId) {//To reject and delete the user
    csrfReady.then(() =>
        fetch('/request/denied/' + userId, {
            method: 'DELETE' ,
            headers: {'X-CSRF-TOKEN': csrfToken}})
    )
    .then(response => {
        if(!response.ok){
            throw new Error(`HTTP error: ${response.status}`);
        }
        return response.text()})
    .then(message => {
        alert(message);
        fetchdata(); //Refresh list
    })
    .catch(error => console.error('Error:', error));
}
