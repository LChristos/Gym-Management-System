
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
            <td class="user-name"></td>
            <td class="user-surname"></td>
            <td class="user-country"></td>
            <td class="user-town"></td>
            <td class="user-address"></td>
            <td class="user-email"></td>
            <td class="user-username"></td>
            <td>
                <button type="button" class= "accept-user" >Accept</button>
                <button type="button" class= "reject-user" style="background-color: red;">Reject</button>
            </td>`;

        user_row.innerHTML = user_details;
        user_row.querySelector('.user-name').textContent= user.name;
        user_row.querySelector('.user-surname').textContent= user.surname;
        user_row.querySelector('.user-country').textContent= user.country;
        user_row.querySelector('.user-town').textContent= user.town;
        user_row.querySelector('.user-address').textContent= user.address;
        user_row.querySelector('.user-email').textContent= user.email;
        user_row.querySelector('.user-username').textContent= user.username;
        user_row.querySelector('.accept-user').addEventListener('click', () => {
            accept_user(user.id);
        });
        user_row.querySelector('.reject-user').addEventListener('click', () => {
            reject_user(user.id);
        });
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
