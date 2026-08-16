document.getElementById('add').addEventListener('submit' , function(event) {
    event.preventDefault();
    let trainer_data= {
        name : document.getElementById('name').value,
        surname: document.getElementById('surname').value
    }

    fetch('http://localhost:8080/manageservices/trainers/create' , {
        method: 'POST' ,
        headers: {
            'Content-Type': 'application/json'
        },
        credentials: 'include',
        body: JSON.stringify(trainer_data)
    })
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