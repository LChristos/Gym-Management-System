document.getElementById('add').addEventListener('submit' , function(event) {
    event.preventDefault();
    let file = document.getElementById('image');
    upload_image(file.files[0])
        .then(imageUrl => {
            console.log("Uploaded image URL:", imageUrl);

            let service_data= {
                name : document.getElementById('name').value,
                description: document.getElementById('description').value,
                price: document.getElementById('price').value,
                max_people: document.getElementById('max_people').value,
                url_image: imageUrl
            }

            return fetch('http://localhost:8080/manageservices/services/create' , {
                method: 'POST' ,
                headers: {
                    'Content-Type': 'application/json'
                },
                credentials: 'include',
                body: JSON.stringify(service_data)
            });
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
            console.error("Error:", error);
        });
})

function upload_image(file){
    let form_data = new FormData();
    form_data.append("image", file);

    return fetch('http://localhost:8080/uploadimage', {
        method: 'POST',
        body: form_data,
        credentials: 'include'
    })
    .then(response => response.json())
    .then(data => {
        if (data.imageUrl) {
            return data.imageUrl;
        } else {
            throw new Error(data.error || "Image upload failed");
        }
    });
}