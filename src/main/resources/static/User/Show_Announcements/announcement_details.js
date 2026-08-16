let parameters = new URLSearchParams(window.location.search);
let id = parameters.get('id');

let announcement_title = document.getElementById('title');
let announcement_body = document.getElementById('body');

fetch('http://localhost:8080/managearticles/' + id, { method: 'GET' })
.then(response => response.json())
.then(data => {
    announcement_title.innerHTML = data.title;
    announcement_body.innerHTML = data.body;
})
.catch(error => console.error('Error fetching announcement:', error));
