let listoftrainers = [];//List of trainers for the program

function fetch_trainers() {
    return fetch('/manageservices/trainers', { credentials: 'include' })
        .then(response => response.json())
        .then(trainers => {
            listoftrainers = trainers;
        })
        .catch(error => {console.error('Error fetching trainers:', error); throw error; });
}

let listofservices = [];//List of services for the program

function fetch_services() {
    return fetch('/manageservices/services', { credentials: 'include' })
        .then(response => response.json())
        .then(services => {
            listofservices = services;
        })
        .catch(error => {console.error('Error fetching trainers:', error); throw error; });
}

function loadData(){
    Promise.all([fetch_trainers(), fetch_services()])
        .then(() => reservations())
        .catch(error => console.error('Error loading trainers/services before reservations:', error));
}

function reservations(){
    fetch('/reservations')
    .then(response => response.json())
    .then(programs => {
        let reservation_div = document.getElementById('show_reservations');
        reservation_div.innerHTML = '';//Clear the Element

        // Create the table
        let table = document.createElement('table');
        table.className = 'trainers_table';

        //table's headers
        let tableHeader = `
            <thead>
                <tr>
                    <th>Service</th>
                    <th>Date</th>
                    <th>Time</th>
                    <th>Trainer</th>
                </tr>
            </thead>
        `;
        table.innerHTML = tableHeader;
        let table_body = document.createElement('tbody');
        programs.forEach(program => {
            let program_row = document.createElement('tr');
            program_row.className = 'program_data';
            // Create the program details for each Reservation
            let service_name = '';
            listofservices.forEach(service => {
                if (service.id === program.services_name){
                    service_name = service.name;
                }
            });
            let trainer_name = '';
            listoftrainers.forEach(trainer => {
                if (trainer.id === program.trainer_id){
                    trainer_name = trainer.name + " " + trainer.surname;
                }
            });
            let program_details = `
                    <td><span>${service_name}</span> </td>
                    <td><span>${program.programdate}</span></td>
                    <td><span>${program.hour}</span></td>
                    <td><span>${trainer_name}</span></td>`;

            program_row.innerHTML = program_details;
            table_body.appendChild(program_row);

        });
        table.appendChild(table_body);
        reservation_div.appendChild(table);
    })
    .catch(error => console.log('Error:' , error));
}
if (window.userId !== undefined) {
    loadData();
} else {
    window.addEventListener("userLoaded", loadData, { once: true });
}