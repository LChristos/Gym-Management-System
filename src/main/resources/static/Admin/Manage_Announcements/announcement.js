window.onload = fetcharticles();
function fetcharticles(){
    fetch('/managearticles')
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
    csrfReady.then(() =>
        fetch('/managearticles/delete/' + articleId, {
            method: 'DELETE' ,
            headers: {
                'X-CSRF-TOKEN': csrfToken
            }})
    )
    .then(response => {
        if(!response.ok){
            throw new Error(`HTTP error: ${response.status}`);
        }
        return response.text()})
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
    csrfReady.then(() =>
        fetch('/managearticles/create' , {
            method: 'POST' ,
            headers: {
                'Content-Type': 'application/json' ,
                'X-CSRF-TOKEN': csrfToken
            },
            credentials: 'include',
            body: JSON.stringify(article_data)
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
        fetcharticles();//Refresh the page
    })
    .catch(error => {
        alert(error.message);
        console.log('Error:' , error);
    })
})