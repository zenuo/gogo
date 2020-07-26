var lints = null;
var input = null;
var lastRequestLints = new Date();
var lastInputValue = null;
function requestLints() {
    if (lints == null) {
        lints = document.getElementById("lints");
    }
    if (input == null) {
        input = document.getElementById("input");
    }
    let value = input.value;
    if (lastInputValue != value) {
        lastInputValue = value;
    } else {
        return;
    }
    if (value == "") {
        return;
    }
    var now = Date.now();
    if (now - lastRequestLints < 2000) {
        return;
    }
    lastRequestLints = now;
    while(lints.childElementCount > 0) {
        lints.children[0].remove();
    }
    var xhttp = new XMLHttpRequest();
    xhttp.timeout = 5000;
    xhttp.onreadystatechange = function () {
        if (this.readyState != 4 || this.status != 200) {
            return;
        }
        let responseText = this.responseText;
        let data = JSON.parse(responseText);
        data.lints.forEach(l => lints.appendChild(new Option(l.replace("<b>", "").replace("</b>", ""))));
    };
    xhttp.open('GET', "/api/lint?q=" + value);
    xhttp.send();
};