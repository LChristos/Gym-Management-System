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
                <td><input type="text" name="name" class="trainer-name" /></td>
                <td><input type="text" name="surname" class="trainer-surname" /></td>
                <td>
                    <button type="submit" class="change-trainer">Change</button>
                    <button type="button" class="delete-trainer" >Delete</button>
                </td>`;

        trainer_row.innerHTML = trainer_details;
        const trainernameinput = trainer_row.querySelector('.trainer-name');
        trainernameinput.id = `name_${trainer.id}`;
        trainernameinput.value = trainer.name ?? '';
        trainernameinput.setAttribute('form', String(formId));
        const trainersurnameinput = trainer_row.querySelector('.trainer-surname');
        trainersurnameinput.id = `surname_${trainer.id}`;
        trainersurnameinput.value = trainer.surname ?? '';
        trainersurnameinput.setAttribute('form', String(formId));
        trainer_row.querySelector('.change-trainer').setAttribute('form', formId);
        trainer_row.querySelector('.delete-trainer').addEventListener('click', () => {
            Delete_trainer(trainer.id);
        });
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
            <td><input type="text" name="service_name" class="service-name"></td>
            <td><input type="text" name="description" class="service-description"></td>
            <td><input type="number" name="price" class="service-price"></td>
            <td><input type="number" name="max_people" class="service-people"></td>
            <td>
                <button type="submit" class="change-service">Change</button>
                <button type="button" class="delete-service">Delete</button>
            </td>
        `;

        service_row.innerHTML = service_details;

        const servicenameinput = service_row.querySelector('.service-name');
        servicenameinput.id = `service_name_${service.id}`;
        servicenameinput.value = service.name ?? '';
        servicenameinput.setAttribute('form', formId);

        const servicedescriptioninput = service_row.querySelector('.service-description');
        servicedescriptioninput.id = `description_${service.id}`;
        servicedescriptioninput.value = service.description ?? '';
        servicedescriptioninput.setAttribute('form', formId);

        const servicepriceinput = service_row.querySelector('.service-price');
        servicepriceinput.id = `price_${service.id}`;
        servicepriceinput.value = service.price ?? '';
        servicepriceinput.setAttribute('form', formId);

        const servicepeopleinput = service_row.querySelector('.service-people');
        servicepeopleinput.id = `max_people_${service.id}`;
        servicepeopleinput.value = service.max_people ?? '';
        servicepeopleinput.setAttribute('form', formId);

        service_row.querySelector('.change-service').setAttribute('form', formId);

        service_row.querySelector('.delete-service').addEventListener('click', () => {
            Delete_service(service.id);
        });

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

        let program_details = `
            <td>
                <select name="services_name" class="program-service"></select>
            </td>
            <td>
                <input type="date" name="programdate" class="program-date">
            </td>
            <td>
                <input type="time" name="hour" class="program-hour">
            </td>
            <td>
                <select name="trainer_id" class="program-trainer"></select>
            </td>
            <td>
                <input type="number" name="services_max_people" class="program-max-people">
            </td>
            <td>
                <input type="number" name="peoplereserve" class="program-reservations">
            </td>
            <td>
                <button type="submit" class="change-program">Change</button>
                <button type="button" class="delete-program">Delete</button>
            </td>
        `;

        program_row.innerHTML = program_details;

        // Service dropdown
        const serviceSelect = program_row.querySelector('.program-service');
        serviceSelect.id = `services_name_${program.id}`;
        serviceSelect.setAttribute('form', formId);

        listofservices.forEach(service => {
            const option = document.createElement('option');
            option.value = service.id;
            option.textContent = service.name ?? '';
            option.selected = service.id === program.services_name;
            serviceSelect.appendChild(option);
        });

        // Trainer dropdown
        const trainerSelect = program_row.querySelector('.program-trainer');
        trainerSelect.id = `trainer_id_${program.id}`;
        trainerSelect.setAttribute('form', formId);

        listoftrainers.forEach(trainer => {
            const option = document.createElement('option');
            option.value = trainer.id;
            option.textContent = `${trainer.name ?? ''} ${trainer.surname ?? ''}`.trim();
            option.selected = trainer.id === program.trainer_id;
            trainerSelect.appendChild(option);
        });

        // Date
        const programdateinput = program_row.querySelector('.program-date');
        programdateinput.id = `programdate_${program.id}`;
        programdateinput.value = program.programdate ?? '';
        programdateinput.setAttribute('form', formId);

        // Time
        const programhourinput = program_row.querySelector('.program-hour');
        programhourinput.id = `hour_${program.id}`;
        programhourinput.value = program.hour ?? '';
        programhourinput.setAttribute('form', formId);

        // Maximum people
        const programmaxpeopleinput = program_row.querySelector('.program-max-people');
        programmaxpeopleinput.id = `services_max_people_${program.id}`;
        programmaxpeopleinput.value = program.max_people ?? '';
        programmaxpeopleinput.setAttribute('form', formId);

        // Reserved places
        const programreservationsinput = program_row.querySelector('.program-reservations');
        programreservationsinput.id = `peoplereserve_${program.id}`;
        programreservationsinput.value = program.peoplereserve ?? '';
        programreservationsinput.setAttribute('form', formId);

        // Buttons
        program_row.querySelector('.change-program').setAttribute('form', formId);

        program_row.querySelector('.delete-program').addEventListener('click', () => {
            Delete_program(program.id);
        });

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