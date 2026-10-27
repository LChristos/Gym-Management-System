
window.onload = fetchdata();
function fetchdata(){
    fetch('/manageusers')
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
                <td><input type="text" name="name" class="user-name"/></td>
                <td><input type="text" name="surname" class="user-surname"/></td>
                <td><input type="text" name="country" class="user-country"/></td>
                <td><input type="text" name="town" class="user-town"/></td>
                <td><input type="text" name="address" class="user-address"/></td>
                <td><input type="email" name="email" class="user-email"/></td>
                <td><input type="text" name="username" class="user-username"/></td>
                <td>
                    <select name="role" class="user-role">
                        <option value="user">user</option>
                        <option value="admin">admin</option>
                    </select>
                </td>
                <td>
                    <button type="submit" class="change-user">Change</button>
                    <button type="button" class="delete-user">Delete</button>
                </td>`;

        user_row.innerHTML = user_details;

        ['name', 'surname', 'country', 'town', 'address', 'email', 'username'].forEach(field => {
            const input = user_row.querySelector(`input.user-${field}`);
            input.id = `${field}_${user.id}`;
            input.value = String(user[field]);
            input.setAttribute('form', String(formId));
        });

        const roleSelect = user_row.querySelector('select.user-role');
        roleSelect.id = `role_${user.id}`;
        roleSelect.value = user.role === 'admin' ? 'admin' : 'user';
        roleSelect.setAttribute('form', String(formId));

        user_row.querySelector('button.change-user').setAttribute('form', String(formId));
        user_row.querySelector('button.delete-user').addEventListener('click', () => {
            Delete_user(user.id);
        });

        table_body.appendChild(user_row);

        //Creation of the form for users changes
        let form_data = document.createElement('form');
        form_data.id = formId;
        form_data.action = "/manageusers/change";
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
                    fetch('/manageusers/change', {
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
        fetch('/manageusers/delete/' + userId,
            { method: 'DELETE' ,
                headers: {'X-CSRF-TOKEN': csrfToken}})
    )
    .then(response => {
        if(!response.ok){
            throw new Error(`HTTP error: ${response.status}`);
        }
        return response.text()})
    .then(message => {
        alert(message);
        fetchdata();  //Refresh list
    })
    .catch(error => console.error('Error:', error));
}
