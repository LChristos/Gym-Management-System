window.onload = fetcharticles();
function fetcharticles(){
    fetch('http://localhost:8080/managearticles')
    .then(response => response.json())
    .then(data => {
        let articles = document.getElementById('announcements');
        articles.innerHTML = '';
        data.forEach(announcement => {
            let div = document.createElement('div');
            div.className = 'announcement';
            div.innerHTML = `
                <a href="announcement_details.html?id=${announcement.id}">${announcement.title}</a>
                <button onclick="Delete_announcement('${announcement.id}')">Delete</button>`;
            articles.appendChild(div);
        });
    })
    .catch(error => console.error('Error fetching announcements:', error));
}

function Delete_announcement(articleId){
    fetch('http://localhost:8080/managearticles/delete/' + articleId, { method: 'DELETE' })
    .then(response => response.text())
    .then(message => {
        alert(message);
        fetcharticles();  //Refresh list
    })
    .catch(error => console.error('Error:', error));
}

document.getElementById('create_announcement').addEventListener('submit' , function(event) {
    event.preventDefault();
    let article_data= {
        title: document.getElementById('new_title').value,
        body: document.getElementById('new_body').value
    }
    fetch('http://localhost:8080/managearticles/create' , {
        method: 'POST' ,
        headers: {
            'Content-Type': 'application/json'
        },
        credentials: 'include',
        body: JSON.stringify(article_data)
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
        fetcharticles();//Refresh the page
    })
    .catch(error => {
        alert(error.message);
        console.log('Error:' , error);
    })
})