function openUrlWeb(url) {
    window.open(url, '_blank'); // Open URL in a new tab
}

function onLoadFinished() {
    document.dispatchEvent(new Event("app-loaded"));
}
