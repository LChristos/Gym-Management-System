
let csrfToken = "";
const csrfReady = fetch('/csrf')
    .then(response => response.json())
    .then(data => {
        csrfToken = data.token;
    });
