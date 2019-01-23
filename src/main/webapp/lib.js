function HSVtoRGB(h, s, v) {
    var r, g, b, i, f, p, q, t;
    if (arguments.length === 1) {
        s = h.s, v = h.v, h = h.h;
    }
    i = Math.floor(h * 6);
    f = h * 6 - i;
    p = v * (1 - s);
    q = v * (1 - f * s);
    t = v * (1 - (1 - f) * s);
    switch (i % 6) {
        case 0: r = v, g = t, b = p; break;
        case 1: r = q, g = v, b = p; break;
        case 2: r = p, g = v, b = t; break;
        case 3: r = p, g = q, b = v; break;
        case 4: r = t, g = p, b = v; break;
        case 5: r = v, g = p, b = q; break;
    }
    return {
        r: Math.round(r * 255),
        g: Math.round(g * 255),
        b: Math.round(b * 255)
    };
};


// STASHED UNTIL ZOOM-7 FIX ON LANDSCAPE NAVIGATION
function readBytes(cbin) {
    //var bin = pako.inflate(cbin);
    //cbin = cbin.split('').map(function (e) { return e.charCodeAt(0);});
    //var inflate = new Zlib.Inflate(cbin);
    //var bin = inflate.decompress();
    let bin = cbin;
    //return new Uint8Array(cbin);
    let byteArray = [];
    for (let i = 0; i < bin.byteLength; i += 1) {
        //console.log(bin[i])
        //let v = bin.charCodeAt(i + 1) + bin.charCodeAt(i);
        //let v =  (bin.charCodeAt(i) & 0xff);
        byteArray.push(bin[i]);
    }

    return byteArray;
}

function getRequest(url, callback)
{
    var xmlHttp = new XMLHttpRequest();
    xmlHttp.responseType = "arraybuffer";
    xmlHttp.onreadystatechange = function() { 
        if (xmlHttp.readyState === 4 && xmlHttp.status === 200){
            //var s = new Uint8Array(xmlHttp.response)
            if (s){
                //console.log(s);
                callback(s);
            }else
                console.log("error while arraybuffer read")

        }
    }
    xmlHttp.open("GET", url, true); // true for asynchronous 
    xmlHttp.send(null);
}

function cross(v1, v2) {
    return {
        x: v1.y * v2.z - v1.z * v2.y,
        y: v1.z * v2.x - v1.x * v2.z,
        z: v1.x * v2.y - v1.y * v2.x
    }
}
