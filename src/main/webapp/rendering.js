function genericRender(style, height) {
    const waterlevelCap = 0;
    const beachlevelCap = 45;
    const plainlevelCap = 450;
    const mountainlevelCap = 1500;
    const highmountainlevelCap = 2000;
    const snowmountainlevelCap = 8900;
    let lowCap = waterlevelCap;
    let topCap = beachlevelCap;

    let currentStyle = style.water;

    if (height > waterlevelCap && height <= beachlevelCap) {
        lowCap = waterlevelCap;
        topCap = beachlevelCap;
        currentStyle = style.beach;
    } else if (height > beachlevelCap && height <= plainlevelCap) {
        lowCap = beachlevelCap;
        topCap = plainlevelCap;
        currentStyle = style.plain;
    } else if (height > plainlevelCap && height <= mountainlevelCap) {
        lowCap = plainlevelCap;
        topCap = mountainlevelCap;
        currentStyle = style.mountain;
    } else if (height > mountainlevelCap && height <= highmountainlevelCap) {
        lowCap = mountainlevelCap;
        topCap = highmountainlevelCap;
        currentStyle = style.highMountain;
    } else if (height > highmountainlevelCap && height <= snowmountainlevelCap) {
        lowCap = highmountainlevelCap;
        topCap = snowmountainlevelCap;
        currentStyle = style.snow;
    }


    const hueRatio = 1 / (topCap - lowCap) * currentStyle.hueVariation;
    const satRatio = 1 / (topCap - lowCap) * currentStyle.satVariation;
    const valRatio = 1 / (topCap - lowCap) * currentStyle.valVariation;


    return {
        hue: currentStyle.hueBase + (height - lowCap) * hueRatio,
        sat: currentStyle.satBase + (height - lowCap) * satRatio,
        val: currentStyle.valBase + (height - lowCap) * valRatio,
    };
}


function height2normal(heightmap, width, height) {

    //let src = context.getImageData( 0, 0, width, height );
    let actualSize = Math.sqrt(heightmap.length);
    let result = [];

    for (let y = 0; y < height; ++y) {
        for (let x = 0; x < width; ++x) {
            let index = y * actualSize + x;
            let s11, s01, s21, s10, s12;
            if (x === 0) {

                // left edge

                s21 = heightmap[index];
                s01 = heightmap[index + 1];

            } else if (x === (width - 1)) {

                // right edge

                s21 = heightmap[index - 1];
                s01 = heightmap[index];

            } else {
                s01 = heightmap[index + 1];
                s21 = heightmap[index - 1];

            }

            if (y === 0) {

                // top edge

                s12 = heightmap[index];
                s10 = heightmap[index + actualSize];

            } else if (y === height - 1) {

                // bottom edge

                s12 = heightmap[index - actualSize];
                s10 = heightmap[index];

            } else{
                s10 = heightmap[index + actualSize];
                s12 = heightmap[index - actualSize];

            }

            let nnva = {x: 2, y: 0, z: s21 - s01};
            let lva = Math.sqrt(nnva.x * nnva.x + nnva.y * nnva.y + nnva.z * nnva.z);
            let va = {x: nnva.x / lva, y: nnva.y / lva, z: nnva.z / lva };
            let nnvb = {x: 0, y: 2, z: s12 - s10};
            let lvb = Math.sqrt(nnvb.x * nnvb.x + nnvb.y * nnvb.y + nnvb.z * nnvb.z);
            let vb = {x: nnvb.x / lvb, y: nnvb.y / lvb, z: nnvb.z / lvb };
            let normal = cross(va, vb);
            let L = {x: -1, y: -1, z: 1};

            let Ia = 0.9;
            let id = 1;
            let kd = 1;
            let Id = Ia + id * kd * math.dot([normal.x, normal.y, normal.z], [L.x, L.y, L.z]);
            result[y * width + x] = Id;
            //result[y * width + x] = normal;
        }
    }


    return result;

}
/*
let genericTruc = [
    {
        nom: "water/beach",
        minLevel: 0,
        maxLevel: 15,
        colorMin: renderMegadrive.water,
        colorMax: renderMegadrive.beach
    },
    {
        nom: "plaine",
        minLevel: 16,

    }
]*/

let renderMegadrive = {
    water: {
        hueBase: 240 / 360,
        hueVariation: 0,
        satBase: 1,
        satVariation: 0,
        valBase: 0.2,
        valVariation: 0
    },
    beach: {
        hueBase: 40 / 360,
        hueVariation: 80 / 360,
        satBase: 1,
        satVariation: 0,
        valBase: 0.7,
        valVariation: -0.5
    },
    plain: {
        hueBase: 120 / 360,
        hueVariation: -90 / 360,
        satBase: 1,
        satVariation: 0,
        valBase: 0.2,
        valVariation: 0
    },
    mountain: {
        hueBase: 120 / 360,
        hueVariation: -90 / 360,
        satBase: 1,
        satVariation: 0,
        valBase: 0.2,
        valVariation: 0
    },
    highMountain: {
        hueBase: 30 / 360,
        hueVariation: 0,
        satBase: 1,
        satVariation: 0,
        valBase: 0.2,
        valVariation: 0.7
    },
    snow: {
        hueBase: 30 / 360,
        hueVariation: 0,
        satBase: 0,
        satVariation: 0,
        valBase: 0.9,
        valVariation: 0.1
    }
};

let classicRendering = {
    water: {
        hueBase: 204 / 360,
        hueVariation: -164/360,
        satBase: 0.4,
        satVariation: 0.1,
        valBase: 1,
        valVariation: 0
    },
    beach: {
        hueBase: 40 / 360,
        hueVariation: 80 / 360,
        satBase: 0.5,
        satVariation: 0.5,
        valBase: 1,
        valVariation: -0.3
    },
    plain: {
        hueBase: 120 / 360,
        hueVariation: -90 / 360,
        satBase: 1,
        satVariation: -0.5,
        valBase: 0.7,
        valVariation: 0.1
    },
    mountain: {
        hueBase: 30 / 360,
        hueVariation: 0,
        satBase: 0.5,
        satVariation: 0.5,
        valBase: 0.8,
        valVariation: -0.4
    },
    highMountain: {
        hueBase: 30 / 360,
        hueVariation: 0,
        satBase: 1,
        satVariation: -0.2,
        valBase: 0.4,
        valVariation: 0.5
    },
    snow: {
        hueBase: 30 / 360,
        hueVariation: 0,
        satBase: 0,
        satVariation: 0,
        valBase: 0.9,
        valVariation: 0.1
    }
};
