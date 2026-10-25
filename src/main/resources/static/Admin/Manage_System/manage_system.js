window.addEventListener('DOMContentLoaded', function() {
    fetchdatatrainer();
    fetchdataservice();
    Promise.all([fetch_trainers(), fetch_services()])
        .then(() => fetchdataprogram())
        .catch(error => console.error('Error loading trainers/services before program:', error));
});

let listoftrainers = [];//list of trainers for the program

function fetch_trainers() {
    return fetch('/manageservices/trainers', { credentials: 'include' })
        .then(response => response.json())
        .then(trainers => {
            listoftrainers = trainers;
        })
        .catch(error => {console.error('Error fetching trainers:', error); throw error; });
}

let listofservices = [];//List of the services for the program

function fetch_services() {
    return fetch('/manageservices/services', { credentials: 'include' })
        .then(response => response.json())
        .then(services => {
            listofservices = services;
        })
        .catch(error => {console.error('Error fetching trainers:', error); throw error; });
}


//CRUD FOR TRAINERS
function fetchdatatrainer(){//fetch trainers
    fetch('/manageservices/trainers')
    .then((response) => response.json())
    .then(trainers => display_trainers(trainers))
    .catch(error => console.error('Error:' , error))
}

function display_trainers(trainers) {
    let list_system = document.getElementById('list_system');
    list_system.innerHTML = ''; // Clear the content

    // Create the table
    let table = document.createElement('table');
    table.className = 'trainers_table';

    //table's headers
    let tableHeader = `
        <thead>
            <tr>
                <th>Name</th>
                <th>Surname</th>
                <th>Actions</th>
            </tr>
        </thead>
    `;
    table.innerHTML = tableHeader;

    // table's body
    let table_body = document.createElement('tbody');
    Object.values(trainers).forEach(trainer => {
        let trainer_row = document.createElement('tr');
        trainer_row.className = 'trainer_data';
        let formId = 'trainer_' + trainer.id;
        // Create the trainer data
        let trainer_details = `
                <td><input type="text" name="name" id="name_${trainer.id}" value="${trainer.name}" form="${formId}"/></td>
                <td><input type="text" name="surname" id="surname_${trainer.id}" value="${trainer.surname}" form="${formId}"/></td>
                <td>
                    <button type="submit" form="${formId}">Change</button>
                    <button onclick="Delete_trainer('${trainer.id}')">Delete</button>
                </td>`;

        trainer_row.innerHTML = trainer_details;
        table_body.appendChild(trainer_row);

        //Creation of the form for trainer changes
        let form_data = document.createElement('form');
        form_data.id = formId;
        form_data.action = "/manageservices/trainers/change";
        form_data.method = "post";
        form_data.style.display = "none";
        table_body.appendChild(form_data);

    });

    table.appendChild(table_body);
    list_system.appendChild(table);

    Object.values(trainers).forEach(trainer => {//For each trainer what the Change button do
        let change_trainer = document.getElementById('trainer_' + trainer.id);
        if (change_trainer){
            document.getElementById('trainer_' + trainer.id).addEventListener('submit' , function(event) {
                event.preventDefault();
                let data = {
                    id: trainer.id,
                    name: document.getElementById(`name_${trainer.id}`).value,
                    surname: document.getElementById(`surname_${trainer.id}`).value,
                };

                csrfReady.then(() =>
                    fetch('/manageservices/trainers/change', {
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
                    fetchdatatrainer();
                })
                .catch((error) => {
                    console.error('Error:', error);
                });
            });
        }
    });

}
//Delete Trainer
function Delete_trainer(trainerId){
    csrfReady.then(() =>
        fetch('/manageservices/trainers/delete/' + trainerId,
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
        fetchdatatrainer();  //Refresh list
    })
    .catch(error => console.error('Error:', error));
}

//CRUD FOR THE SERVICES

function fetchdataservice(){//Fetch services
    fetch('/manageservices/services', { credentials: 'include' })
    .then((response) => response.json())
    .then(services => display_services(services))
    .catch(error => console.error('Error:' , error))
}

function display_services(services) {
    let list_services = document.getElementById('list_services');
    list_services.innerHTML = ''; // Clear the content

    // Create the table
    let table = document.createElement('table');
    table.className = 'services_table';

    //table's headers
    let tableHeader = `
        <thead>
            <tr>
                <th>Name</th>
                <th>Description</th>
                <th>Price</th>
                <th>Max People</th>
                <th>Actions</th>
            </tr>
        </thead>
    `;
    table.innerHTML = tableHeader;

    // table's body
    let table_body = document.createElement('tbody');
    Object.values(services).forEach(service => {
        let service_row = document.createElement('tr');
        service_row.className = 'service_data';
        let formId = 'service_' + service.id;
        let service_details = `
                <td><input type="text" name="service_name" id="service_name_${service.id}" value="${service.name}" form="${formId}"/></td>
                <td><input type="text" name="description" id="description_${service.id}" value="${service.description}" form="${formId}"/></td>
                <td><input type="number" name="price" id="price_${service.id}" value="${service.price}" form="${formId}"/></td>
                <td><input type="number" name="max_people" id="max_people_${service.id}" value="${service.max_people}" form="${formId}"/></td>
                <td>
                    <button type="submit" form="${formId}">Change</button>
                    <button onclick="Delete_service('${service.id}')">Delete</button>
                </td>`;

        service_row.innerHTML = service_details;
        table_body.appendChild(service_row);

        //Creation of the form for users changes
        let form_data = document.createElement('form');
        form_data.id = formId;
        form_data.action = "/manageservices/services/change";
        form_data.method = "post";
        form_data.style.display = "none";
        table_body.appendChild(form_data);

    });

    table.appendChild(table_body);
    list_services.appendChild(table);

    Object.values(services).forEach(service => {//For each service the change button
        let change_service = document.getElementById('service_' + service.id);
        if (change_service){
            document.getElementById('service_' + service.id).addEventListener('submit' , function(event) {
                event.preventDefault();
                let data = {
                    id: service.id,
                    name: document.getElementById(`service_name_${service.id}`).value,
                    description: document.getElementById(`description_${service.id}`).value,
                    price: document.getElementById(`price_${service.id}`).value,
                    max_people: document.getElementById(`max_people_${service.id}`).value
                };

                csrfReady.then(() =>
                    fetch('/manageservices/services/change', {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json' ,// Required for POST
                            'X-CSRF-TOKEN': csrfToken
                        },
                        credentials: 'include',
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
                    fetchdataservice();
                })
                .catch((error) => {
                    console.error('Error:', error);
                });
            });
        }
    });

}
//Delete Service
function Delete_service(serviceId){
    csrfReady.then(() =>
        fetch('/manageservices/services/delete/' + serviceId,
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
        fetchdataservice();  //Refresh list
    })
    .catch(error => console.error('Error:', error));
}

//CRUD FOR PROGRAM

function fetchdataprogram(){//Fetch only schedule from today and after
    fetch('/manageservices/program/futures')
    .then((response) => response.json())
    .then(programs => display_programs(programs))
    .catch(error => console.error('Error:' , error))
}

function display_programs(programs) {
    let list_program = document.getElementById('list_program');
    list_program.innerHTML = ''; // Clear the content

    // Create the table
    let table = document.createElement('table');
    table.className = 'program_table';

    //table's headers
    let tableHeader = `
        <thead>
            <tr>
                <th>Service</th>
                <th>Day</th>
                <th>hour</th>
                <th>Trainer</th>
                <th>Max People</th>
                <th>People Reserve</th>
                <th>Actions</th>
            </tr>
        </thead>
    `;
    table.innerHTML = tableHeader;
    // table's body
    let table_body = document.createElement('tbody');
    Object.values(programs).forEach(program => {
        let program_row = document.createElement('tr');
        program_row.className = 'program_data';
        let formId = 'program_' + program.id;
        //List of trainers
        let trainer_option = `<select id="trainer_id_${program.id}" name="trainer_id" form="${formId}">`;
        listoftrainers.forEach(trainer => {
           // If the trainer's id matches the program's trainer_id, mark it as selected.
           trainer_option += `<option value="${trainer.id}" ${trainer.id === program.trainer_id ? 'selected' : ''}>${trainer.name} ${trainer.surname}</option>`;
        });
        trainer_option += `</select>`;

        //List of services
        let service_option = `<select id="services_name_${program.id}" name="services_name" form="${formId}">`;
        listofservices.forEach(service => {
           // If the service's id matches the program's service_name, mark it as selected.
           service_option += `<option value="${service.id}" ${service.id === program.services_name ? 'selected' : ''}>${service.name}</option>`;
        });
        service_option += `</select>`;

        // Create the program details
        let program_details = `
                <td>${service_option}</td>
                <td><input type="date" name="programdate" id="programdate_${program.id}" value="${program.programdate}" form="${formId}"/></td>
                <td><input type="time" name="hour" id="hour_${program.id}" value="${program.hour}" form="${formId}"/></td>
                <td>${trainer_option}</td>
                <td><input type="number" name="services_max_people" id="services_max_people_${program.id}" value="${program.max_people}" form="${formId}"/></td>
                <td><input type="number" name="peoplereserve" id="peoplereserve_${program.id}" value="${program.peoplereserve}" form="${formId}"/></td>
                <td>
                    <button type="submit" form="${formId}">Change</button>
                    <button onclick="Delete_program('${program.id}')">Delete</button>
                </td>`;

        program_row.innerHTML = program_details;
        table_body.appendChild(program_row);

        //Creation of the form for program changes
        let form_data = document.createElement('form');
        form_data.id = formId;
        form_data.action = "/manageservices/program/change";
        form_data.method = "post";
        form_data.style.display = "none";
        table_body.appendChild(form_data);

    });

    table.appendChild(table_body);
    list_program.appendChild(table);

    Object.values(programs).forEach(program => {//Change button for the program
        let change_program = document.getElementById('program_' + program.id);
        if (change_program){
            document.getElementById('program_' + program.id).addEventListener('submit' , function(event) {
                event.preventDefault();
                let trainerId = document.getElementById(`trainer_id_${program.id}`);
                let serviceId = document.getElementById(`services_name_${program.id}`);
                let programs_data = {
                    id: program.id,
                    services_name: serviceId.options[serviceId.selectedIndex].value,
                    programdate: document.getElementById(`programdate_${program.id}`).value,
                    hour: document.getElementById(`hour_${program.id}`).value,
                    trainer_id: trainerId.options[trainerId.selectedIndex].value,
                    max_people: document.getElementById(`services_max_people_${program.id}`).value,
                    peoplereserve: document.getElementById(`peoplereserve_${program.id}`).value
                };

                // Example of sending data via fetch API
                csrfReady.then(() =>
                    fetch('/manageservices/program/change', {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json' ,// Required for POST
                            'X-CSRF-TOKEN': csrfToken
                        },
                        body: JSON.stringify(programs_data)
                    })
                )
                .then(response => {
                    if(!response.ok){
                        throw new Error(`HTTP error: ${response.status}`);
                    }
                    return response.text()})
                .then(message => {
                    alert(message);
                    fetchdataprogram();
                })
                .catch((error) => {
                    console.error('Error:', error);
                });
            });
        }
    });

}
//Delete specific schedule from the program
function Delete_program(programId){
    csrfReady.then(() =>
        fetch('/manageservices/program/delete/' + programId, {
            method: 'DELETE' ,
            headers: {'X-CSRF-TOKEN': csrfToken}})
    )
    .then(response => {
        return response.text().then(message => {
          if(!response.ok){
              throw new Error(message);
          }
          return message;})
    })
    .then(message => {
        alert(message);
        fetchdataprogram();  //Refresh list
    })
    .catch(error => console.error('Error:', error));
}