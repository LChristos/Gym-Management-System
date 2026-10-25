let listoftrainers = [];//list of trainers for the program

function fetch_trainers() {
    fetch('http://localhost:8080/manageservices/trainers', { credentials: 'include' })
        .then(response => response.json())
        .then(trainers => {
            listoftrainers = trainers;
        })
        .catch(error => console.error('Error fetching trainers:', error));
}

let listofservices = [];

function fetch_services() {
    fetch('http://localhost:8080/manageservices/services', { credentials: 'include' })
        .then(response => response.json())
        .then(services => {
            listofservices = services;
        })
        .catch(error => console.error('Error fetching trainers:', error));
}

function loadData(){
    fetch_trainers();
    fetch_services();
    fetch('http://localhost:8080/reservationsavailable')
    .then(response => response.json())
    .then(programs => {
        let reservation_div = document.getElementById('reservations');
        reservation_div.innerHTML = '';

        // Create the table
        let table = document.createElement('table');
        table.className = 'trainers_table';

        //table's headers
        let tableHeader = `
            <thead>
                <tr>
                    <th>Υπηρεσία</th>
                    <th>Μέρα</th>
                    <th>Ώρα</th>
                    <th>Γυμναστής</th>
                    <th>Μέγιστος αριθμός ατόμων</th>
                    <th>Άτομα που έκαναν κράτηση</th>
                </tr>
            </thead>
        `;
        table.innerHTML = tableHeader;
        let table_body = document.createElement('tbody');
        programs.forEach(program => {
            let program_row = document.createElement('tr');
            program_row.className = 'program_data';
            let formId = program.id;
            // Create the user details
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
                    <td><span>${trainer_name}</span></td>
                    <td><span>${program.max_people}</span></td>
                    <td><span>${program.peoplereserve}</span></td>
                    <td>
                        <button onclick="Make_Reservation('${program.id}')">Make a Reservation</button>
                    </td>`;

            program_row.innerHTML = program_details;
            table_body.appendChild(program_row);

        });
        table.appendChild(table_body);
        reservation_div.appendChild(table);
    })
    .catch(error => console.log('Error:' , error));
    reservations_todelete();
}

if (window.userId !== undefined) {
    loadData();
} else {
    window.addEventListener("userLoaded", loadData, { once: true });
}

function reservations_todelete(){
    fetch('http://localhost:8080/reservationuser')
    .then(response => response.json())
    .then(programs => {
        let reservation_div = document.getElementById('reservations_made');
        reservation_div.innerHTML = '';
        if(programs.length > 0) {
            // Create the table
            let table = document.createElement('table');
            table.className = 'trainers_table';

            //table's headers
            let tableHeader = `
                <thead>
                    <tr>
                        <th>Υπηρεσία</th>
                        <th>Μέρα</th>
                        <th>Ώρα</th>
                        <th>Γυμναστής</th>
                    </tr>
                </thead>
            `;
            table.innerHTML = tableHeader;
            let table_body = document.createElement('tbody');
            programs.forEach(program => {
                let program_row = document.createElement('tr');
                program_row.className = 'program_data';
                let formId = program.id;
                // Create the user details
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
                        <td><span>${trainer_name}</span></td>
                        <td>
                            <button onclick="Delete_Reservation('${program.id}')">Διαγραφή Κράτησης</button>
                        </td>`;

                program_row.innerHTML = program_details;
                table_body.appendChild(program_row);

            });
            table.appendChild(table_body);
            reservation_div.appendChild(table);
        }
    })
    .catch(error => console.log('Error:' , error));
}

function Delete_Reservation(programid){
    let res_data= {
        programid: programid,
    }
    csrfReady.then(() =>
        fetch('http://localhost:8080/reservations/delete/'+ programid , {
            method: 'DELETE' ,
            headers: {
                'Content-Type': 'application/json' ,
                'X-CSRF-TOKEN': csrfToken
            },
            credentials: 'include',
            body: JSON.stringify(res_data)
        })
    )
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
        location.reload();
    })
    .catch(error => {
        alert(error.message);
        console.log('Error:' , error);
    })

}



function Make_Reservation(programId){
    let res_data= {
        programid: programId,
    }
    csrfReady.then(() =>
        fetch('http://localhost:8080/reservations/create' , {
            method: 'POST' ,
            headers: {
                'Content-Type': 'application/json' ,
                'X-CSRF-TOKEN': csrfToken
            },
            credentials: 'include',
            body: JSON.stringify(res_data)
        })
    )
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
        location.reload();
    })
    .catch(error => {
        alert(error.message);
        console.log('Error:' , error);
    })

}