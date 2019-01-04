
<!DOCTYPE html>
<html>
<head>
    <title>BidoofMapExtended</title>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.3.4/dist/leaflet.css"
          integrity="sha512-puBpdR0798OZvTTbP4A8Ix/l+A4dHDD0DGqYW6RQ+9jxkRFclaxxQb/SJAWZfWAkuyeQUytO7+7N4QKrDh+drA==" crossorigin=""/>
    <script src="https://unpkg.com/leaflet@1.3.4/dist/leaflet.js" integrity="sha512-nMMmRyTVoLYqjP9hrbed9S+FzjZHW5gY1TWCHA5ckwXZBadntCNs8kEqAWdrb9O7rxbCaA4lKTIWjDXZxflOcA==" crossorigin=""></script>

    <!-- Custom local libs -->
    <script src="lib.js"></script>
    <script src="rendering.js"></script>
</head>
<body>
<div id="mapid" style="width: 1600px; height: 900px;"></div>
<script>

    function render(canvas, data)
    {
        let uu = readBytes(data);
        let ctx = canvas.getContext('2d');
        let imgData = ctx.getImageData(0, 0, canvas.width, canvas.height);
        let img = imgData.data;
        let size = Math.sqrt(data.length/2);
        for (let y = 0; y < size; ++y)
            for (let x = 0; x < size; ++x){
                // let r = y * 4 + x * 1201 * 4; // <-- if the canvas is not well rotated
                let r = y * 1201 * 4 + x * 4;
                let g = r + 1;
                let b = g + 1;
                let a = b + 1 ;
                let v = uu[y * size + x];
                let rrr = genericRender(renderMegadrive, v);
                let c = HSVtoRGB(rrr.hue, rrr.sat, rrr.val);
                imgData.data[r] = c.r;
                imgData.data[g] = c.g;
                imgData.data[b] = c.b;
                imgData.data[a] = 255;
            }

        ctx.putImageData(imgData, 0, 0);
    }

    //Partial function generator for callback purpose
    function renderPart(tile) {
        return function(data) {
            render(tile, data);
        }
    }

    let map = L.map('mapid', {
        center: [0, 0],
        zoom: 0
    });

    L.GridLayer.HeightMap = L.GridLayer.extend({
        createTile: function (coords) {
            console.log(coords);
            let tile = document.createElement('canvas');
            this.options.tileSize = 1201;
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

    L.gridLayer.heightMap = function(opts) {
        return new L.GridLayer.HeightMap(opts);
    };

    map.addLayer( L.gridLayer.heightMap() );
</script>
</body>
</html>
