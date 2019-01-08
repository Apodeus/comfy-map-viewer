<!DOCTYPE html>
<html>
<head>
    <title>BidoofMapExtended</title>
    <meta charset="utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.3.4/dist/leaflet.css"
          integrity="sha512-puBpdR0798OZvTTbP4A8Ix/l+A4dHDD0DGqYW6RQ+9jxkRFclaxxQb/SJAWZfWAkuyeQUytO7+7N4QKrDh+drA=="
          crossorigin=""/>
    <script src="https://unpkg.com/leaflet@1.3.4/dist/leaflet.js"
            integrity="sha512-nMMmRyTVoLYqjP9hrbed9S+FzjZHW5gY1TWCHA5ckwXZBadntCNs8kEqAWdrb9O7rxbCaA4lKTIWjDXZxflOcA=="
            crossorigin=""></script>

    <!-- Custom local libs -->
    <script src="lib.js"></script>
    <script src="rendering.js"></script>
</head>
<body>
<div id="mapid" style="width: 1600px; height: 900px;"></div>
<script>

    function render(canvas, data) {
        let heightMap = readBytes(data);
        let ctx = canvas.getContext('2d');
        let width = 1200;
        let height = 1200;
        let actualWidth = 1201;
        let imgData = ctx.getImageData(0, 0, canvas.width, canvas.height);
        let Id = height2normal(heightMap, width, height);
        for (let y = 0; y < height; ++y)
            for (let x = 0; x < width; ++x) {
                // let r = y * 4 + x * 1201 * 4; // <-- if the canvas is not well rotated
                let r = y * canvas.getAttribute('width') * 4 + x * 4;
                let g = r + 1;
                let b = g + 1;
                let a = b + 1;
                let height = heightMap[y * actualWidth + x];
                let colorAsHSV = genericRender(classicRendering, height);
                let colorAsRGB = HSVtoRGB(colorAsHSV.hue, colorAsHSV.sat, colorAsHSV.val);
                let diffuseIntensity = Id[y * width + x];
                imgData.data[r] = colorAsRGB.r * diffuseIntensity;
                imgData.data[g] = colorAsRGB.g * diffuseIntensity;
                imgData.data[b] = colorAsRGB.b * diffuseIntensity;
                imgData.data[a] = 255;
            }

        ctx.putImageData(imgData, 0, 0);
    }

    //Partial function generator for callback purpose
    function renderPart(tile) {
        return function (data) {
            render(tile, data);
        }
    }

    let map = L.map('mapid', {
        center: [0, 0],

        zoom: 11,
        maxzoom: 11,
    });

    L.GridLayer.HeightMap = L.GridLayer.extend({
        createTile: function (coords) {
            let tile = document.createElement('canvas');
            this.options.tileSize = 1200;
            let tileSize = this.getTileSize();
            tile.setAttribute('width', tileSize.x);
            tile.setAttribute('height', tileSize.y);
            let f = renderPart(tile);
            let r = getRequest(
                './comfy/map/'
                + coords.z + '/'
                + coords.x + '/'
                + coords.y,
                //console.log
                f
            );

            return tile;
        }
    });

    L.gridLayer.heightMap = function (opts) {
        return new L.GridLayer.HeightMap(opts);
    };

    L.GridLayer.GridBorders = L.GridLayer.extend({
        createTile: function (coords) {
            let tile = document.createElement('canvas');
            this.options.tileSize = 1200;
            let tileSize = this.getTileSize();
            tile.setAttribute('width', tileSize.x);
            tile.setAttribute('height', tileSize.y);
            let f = function (canvas) {
                let ctx = canvas.getContext('2d');
                let imgData = ctx.getImageData(0, 0, canvas.width, canvas.height);
                let size = tile.getAttribute('width');
                let cc = function (data, ind) {
                    data[ind] = 255;
                    data[ind + 1] = 0;
                    data[ind + 2] = 0;
                    data[ind + 3] = 255;
                };
                for (let i = 0; i < 10; ++i) {
                    // let r = y * 4 + x * 1201 * 4; // <-- if the canvas is not well rotated
                    let x0y0x0y1 = i * size * 4;
                    let x0y0x1y0 = i * 4;
                    let x1y0x0y0 = (size - i - 1) * 4;
                    let x1y0x1y1 = (size - 1) * 4 + i * size * 4;
                    let x0y1x1y1 = (size - 1) * size * 4 + i * 4;
                    let x0y1x0y0 = ((size - 1) - i) * size * 4;
                    let x1y1x0y1 = (size - 1) * size * 4 + (size - 1 - i) * 4;
                    let x1y1x1y0 = ((size - 1) - i) * size * 4 + (size - 1) * 4;
                    cc(imgData.data, x0y0x0y1);
                    cc(imgData.data, x0y0x1y0);
                    cc(imgData.data, x1y0x0y0);
                    cc(imgData.data, x1y0x1y1);
                    cc(imgData.data, x0y1x1y1);
                    cc(imgData.data, x0y1x0y0);
                    cc(imgData.data, x1y1x0y1);
                    cc(imgData.data, x1y1x1y0);
                }

                ctx.putImageData(imgData, 0, 0);
                return canvas;
            };

            return f(tile);

        }
    });

    L.gridLayer.gridBorders = function (opts) {
        return new L.GridLayer.GridBorders(opts);
    };

    L.GridLayer.GridCoords = L.GridLayer.extend({
        createTile: function (coords) {
            let tile = document.createElement('canvas');
            this.options.tileSize = 1200;
            let tileSize = this.getTileSize();
            tile.setAttribute('width', tileSize.x);
            tile.setAttribute('height', tileSize.y);
            let f = function (canvas) {
                let ctx = canvas.getContext('2d');
                ctx.font = "11px Arial";
                ctx.fillStyle = "red";
                ctx.fillText("(x = " + coords.x + ", y = " + coords.y + ", zoom = " + coords.z + ")", 15, 15);
                return canvas;
            };

            return f(tile);
        }
    });

    L.gridLayer.gridCoords = function (opts) {
        return new L.GridLayer.GridCoords(opts);
    };


    map.addLayer(L.gridLayer.heightMap());
    map.addLayer(L.gridLayer.gridBorders());
    map.addLayer(L.gridLayer.gridCoords());
</script>
</body>
</html>
