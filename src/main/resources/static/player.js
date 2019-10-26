// player.js
//
// This file is designed to make the media file player interface a little easier to use.
//
// Author: Kneelawk

// There is not really a good way to determine a video's fps so we just assume every video has a *constant* fps of 30.
const fps = 30;

$(document).ready(function () {
    let player = $("#player")[0];

    $(document).on("keypress", function (e) {
        let code = String.fromCharCode(e.which);
        if (code === 'k' || code === 'K') {
            if (player.paused || player.ended) {
                player.play();
            } else {
                player.pause();
            }
        }
        if (code === 'j' || code === 'J') {
            player.currentTime -= 10;
        }
        if (code === 'l' || code === 'L') {
            player.currentTime += 10;
        }
        if (code === 'h' || code === 'H') {
            player.currentTime -= 5;
        }
        if (code === ';' || code === ':') {
            player.currentTime += 5;
        }
        if (code === 'm' || code === 'M') {
            player.muted = !player.muted;
        }
        if (code === '-' || code === '_') {
            player.volume -= 0.05;
        }
        if (code === '=' || code === '+') {
            player.volume += 0.05;
        }
        if (code === ',' || code === '<') {
            let frame = player.currentTime * fps;
            frame -= 1;
            player.currentTime = frame / fps + 0.00001;
        }
        if (code === '.' || code === '>') {
            let frame = player.currentTime * fps;
            frame += 1;
            player.currentTime = frame / fps + 0.00001;
        }
    })
});