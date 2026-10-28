function openUrlWeb(url) {
    window.open(url, '_blank'); // Open URL in a new tab
}

function demoParam(name) {
    return new URLSearchParams(window.location.search).get(name) || "";
}

function onLoadFinished() {
    document.dispatchEvent(new Event("app-loaded"));
}
