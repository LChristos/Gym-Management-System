//Get services
let services_list = document.getElementById("name");

fetch('http://localhost:8080/manageservices/services')
.then((response) => response.json())
.then(data => {
    data.forEach(service => {
        let option = document.createElement("option");
        option.textContent = service.name ;
        option.value = JSON.stringify(service);
        services_list.appendChild(option);
    })
})
.catch(error => console.error('Error:' , error))

services_list.addEventListener('change' , () => {
    event.preventDefault();
    let select_service = JSON.parse(services_list.options[services_list.selectedIndex].value);
     document.getElementById('max_people').value = select_service.max_people;

})


//Get trainer
let trainer_list = document.getElementById("trainer");

fetch('http://localhost:8080/manageservices/trainers')
.then((response) => response.json())
.then(data => {
    data.forEach(trainer => {
        let option = document.createElement("option");
        option.textContent = trainer.name + " " + trainer.surname ;
        option.value = trainer.id;
        trainer_list.appendChild(option);
    })
})
.catch(error => console.error('Error:' , error))


document.getElementById('add').addEventListener('submit' , function(event) {
    event.preventDefault();
    let trainer = document.getElementById('trainer');
    let trainer_text = trainer.options[trainer.selectedIndex].value;
    let service = document.getElementById('name');
    let service_text = JSON.parse(service.options[service.selectedIndex].value);
    let program_data= {
        services_name : service_text.id,
        programdate: document.getElementById('programdate').value,
        hour: document.getElementById('hour').value,
        trainer_id: trainer_text,
        max_people: document.getElementById('max_people').value
    }

    csrfReady.then(() =>
        fetch('http://localhost:8080/manageservices/program/create' , {
            method: 'POST' ,
            headers: {
                'Content-Type': 'application/json' ,
                'X-CSRF-TOKEN': csrfToken
            },
            credentials: 'include',
            body: JSON.stringify(program_data)
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
        document.location.href = "/Admin/Manage_System/manage_system.html";
    })
    .catch(error => {
        alert(error.message);
        console.log('Error:' , error);
    })
})