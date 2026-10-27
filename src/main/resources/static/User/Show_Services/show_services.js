let listoftrainers = [];//list of trainers for the program

function fetch_trainers() {//Fetch all the trainers and save them in the variable listoftrainers
    return fetch('/manageservices/trainers', { credentials: 'include' })
        .then(response => response.json())
        .then(trainers => {
            listoftrainers = trainers;
        })
        .catch(error => console.error('Error fetching trainers:', error));
}

function loadData(){
    const trainersReady = fetch_trainers();
    fetch('/manageservices/services')//Take the services
        .then(response => response.json())
        .then(services => {
            let service_div = document.getElementById('services');
            service_div.innerHTML = ''; // Clear the div element
            services.forEach(service => {//For each Service
                let service_section = document.createElement('div');
                service_section.className = 'service_section';
                //Show Services
                if(window.userRole === "USER"){
                    service_section.innerHTML = `
                        <img class="service_image" />
                        <div class="card_content">
                            <h2 class="service-name"></h2>
                            <p class="service-description"></p>
                            <div class="service-program"></div>
                        </div>`;
                    const programContainer = service_section.querySelector('.service-program');
                    programContainer.id = `program_${service.id}`;
                    //Take this week's program for each service
                    fetch(`/service/show/${service.id}`)
                    .then(response => response.json())
                    .then(programs => trainersReady.then(() => programs))
                    .then(programs => {
                        programContainer.innerHTML = '';
                        if(programs.length > 0) {
                            programs.forEach(program => {//For each Program
                                let trainer_name = '';
                                listoftrainers.forEach(trainer => {//Find the specific trainer
                                    if (trainer.id === program.trainer_id){
                                        trainer_name = trainer.name + " " + trainer.surname;
                                    }
                                });
                                let date = new Date(program.programdate + 'T00:00:00').toLocaleDateString();
                                const program_item = document.createElement('div');
                                program_item.className = 'program_item';
                                program_item.innerHTML = `
                                    <strong class="program-date"></strong> at <strong class="program-hour"></strong>
                                    <span class="program-trainer"></span>`;
                                program_item.querySelector('.program-date').textContent = date;
                                program_item.querySelector('.program-hour').textContent = String(program.hour);
                                program_item.querySelector('.program-trainer').textContent = ` Trainer: ${trainer_name}`;
                                programContainer.appendChild(program_item);
                            });
                        }
                        else{
                            programContainer.innerHTML = `<div class="program_item">Δεν υπάρχει διαθέσιμο πρόγραμμα για αυτή την εβδομάδα.</div>`;
                        }
                    })
                    .catch(error => {
                        programContainer.textContent = 'Πρόβλημα στην φόρτωση προγράμματος';
                        console.error('Error fetching program for services', error);
                    });
                }
                else{
                    service_section.innerHTML = `
                        <img class="service_image" />
                        <div class="card_content">
                            <h2 class="service-name"></h2>
                            <p class="service-description"></p>
                        </div>
                    `;
                }
                const serviceImage = service_section.querySelector('img.service_image');
                serviceImage.src = String(service.url_image);
                serviceImage.alt = `${service.name} image`;
                service_section.querySelector('h2.service-name').textContent = String(service.name);
                service_section.querySelector('p.service-description').textContent = String(service.description);
                service_div.appendChild(service_section);
            });
        })
        .catch(error => console.error('Error fetching services:', error));
}

if (window.userId !== undefined) {
    loadData();
} else {
    window.addEventListener("userLoaded", loadData, { once: true });
}
