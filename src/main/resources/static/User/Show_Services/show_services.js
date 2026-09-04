let listoftrainers = [];//list of trainers for the program

function fetch_trainers() {//Fetch all the trainers and save them in the variable listoftrainers
    fetch('http://localhost:8080/manageservices/trainers', { credentials: 'include' })
        .then(response => response.json())
        .then(trainers => {
            listoftrainers = trainers;
        })
        .catch(error => console.error('Error fetching trainers:', error));
}

document.addEventListener('DOMContentLoaded', function() {
    fetch_trainers();
    fetch('http://localhost:8080/manageservices/services')//Take the services
        .then(response => response.json())
        .then(services => {
            let service_div = document.getElementById('services');
            service_div.innerHTML = ''; // Clear the div element
            services.forEach(service => {//For each Service
                let service_section = document.createElement('div');
                service_section.className = 'service_section';
                //Show Services
                if(window.userRole === "user"){
                    service_section.innerHTML = `
                        <img src="${service.url_image}" alt="${service.name} image" class="service_image" />
                        <div class="card_content">
                            <h2>${service.name}</h2>
                            <p>${service.description}</p>
                            <div id="program_${service.id}"></div>
                        </div>`;
                    //Take this week's program for each service
                    fetch(`http://localhost:8080/service/show/${service.id}`)
                    .then(response => response.json())
                    .then(programs => {
                        let program_div = '';
                        if(programs.length > 0) {
                            programs.forEach(program => {//For each Program
                                let trainer_name = '';
                                listoftrainers.forEach(trainer => {//Find the specific trainer
                                    if (trainer.id === program.trainer_id){
                                        trainer_name = trainer.name + " " + trainer.surname;
                                    }
                                });
                                let date = new Date(program.programdate + 'T00:00:00').toLocaleDateString();
                                program_div += `
                                    <div class="program_item">
                                        <strong>${date}</strong> at <strong>${program.hour}</strong>
                                        <span> Trainer: ${trainer_name}</span>
                                    </div>`;
                            });
                        }
                        else{
                            program_div = `<div class="program_item">Δεν υπάρχει διαθέσιμο πρόγραμμα για αυτή την εβδομάδα.</div>`;
                        }
                        document.getElementById(`program_${service.id}`).innerHTML = program_div;
                    })
                    .catch(error => {
                        document.getElementById(`program_${service.id}`).innerHTML = 'Πρόβλημα στην φόρτωση προγράμματος';
                        console.error('Error fetching program for services', error);
                    });
                }
                else{
                    service_section.innerHTML = `
                        <img src="${service.url_image}" alt="${service.name} image" class="service_image" />
                        <div class="card_content">
                            <h2>${service.name}</h2>
                            <p>${service.description}</p>
                        </div>
                    `;
                }
                service_div.appendChild(service_section);
            });
        })
        .catch(error => console.error('Error fetching services:', error));
});

