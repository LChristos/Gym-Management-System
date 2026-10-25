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
                <a href="announcement_details.html?id=${announcement.id}">${announcement.title}</a>`;
            articles.appendChild(div);
        });
    })
    .catch(error => console.error('Error fetching announcements:', error));
}