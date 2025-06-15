function login() {
    const user = document.getElementById("username").value;
    const pass = document.getElementById("password").value;

    fetch("http://localhost:8000/login", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: `username=${user}&password=${pass}`
    })
    .then(res => res.text())
    .then(data => {
        if (data === "success") {
            document.getElementById("todo-section").style.display = "block";
        } else {
            alert("Login failed");
        }
    });
}

function addTask() {
    const task = document.getElementById("task").value;

    fetch(`http://localhost:8000/add?task=${encodeURIComponent(task)}`)
        .then(res => res.text())
        .then(data => {
            const li = document.createElement("li");
            li.textContent = task;
            document.getElementById("taskList").appendChild(li);
        });
}
