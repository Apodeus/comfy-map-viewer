function genericRender(style, height){
    const waterlevelCap = 0;
    const beachlevelCap = 45;
    const plainlevelCap = 450;
    const mountainlevelCap = 1500;
    const highmountainlevelCap = 2000;
    const snowmountainlevelCap = 4000;
    let lowCap = waterlevelCap;
    let topCap = beachlevelCap;
    
    let currentStyle = style.water;
    if (0 > height){
        return {
            hue: 0,
            sat: 1,
            val: 1
        }
    }
    if (height > waterlevelCap && height <= beachlevelCap){
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
    }else if (height > mountainlevelCap && height <= highmountainlevelCap){
        lowCap = mountainlevelCap;
        topCap = highmountainlevelCap;
        currentStyle = style.highMountain;
    }else if (height > highmountainlevelCap && height <= snowmountainlevelCap) {
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

let renderMegadrive = {
    water: {
        hueBase: 240/360,
        hueVariation: 0,
        satBase: 1,
        satVariation: 0,
        valBase: 0.2,
        valVariation: 0
    },
    beach: {
        hueBase: 40/360,
        hueVariation: 80/360,
        satBase: 1,
        satVariation: 0,
        valBase: 0.7,
        valVariation: -0.5
    },
    plain: {
        hueBase: 120/360,
        hueVariation: -90/360,
        satBase: 1,
        satVariation: 0,
        valBase: 0.2,
        valVariation: 0
    },
    mountain: {
        hueBase: 120/360,
        hueVariation: -90/360,
        satBase: 1,
        satVariation: 0,
        valBase: 0.2,
        valVariation: 0
    },
    highMountain: {
        hueBase: 30/360,
        hueVariation: 0,
        satBase: 1,
        satVariation: 0,
        valBase: 0.2,
        valVariation: 0.7
    },
    snow: {
        hueBase: 30/360,
        hueVariation: 0,
        satBase: 0,
        satVariation: 0,
        valBase: 0.9,
        valVariation: 0.1
    }
};

let classicRendering = {
    water: {
        hueBase: 204/360,
        hueVariation: 0,
        satBase: 0.4,
        satVariation: 0,
        valBase: 1,
        valVariation: 0
    },
    beach: {
        hueBase: 40/360,
        hueVariation: 80/360,
        satBase: 0.5,
        satVariation: 0.5,
        valBase: 1,
        valVariation: -0.3
    },
    plain: {
        hueBase: 120/360,
        hueVariation: -90/360,
        satBase: 1,
        satVariation: -0.5,
        valBase: 0.7,
        valVariation: 0.1
    },
    mountain: {
        hueBase: 30/360,
        hueVariation: 0,
        satBase: 0.5,
        satVariation: 0.5,
        valBase: 0.8,
        valVariation: -0.4
    },
    highMountain: {
        hueBase: 30/360,
        hueVariation: 0,
        satBase: 1,
        satVariation: -0.2,
        valBase: 0.4,
        valVariation: 0.5
    },
    snow: {
        hueBase: 30/360,
        hueVariation: 0,
        satBase: 0,
        satVariation: 0,
        valBase: 0.9,
        valVariation: 0.1
    }
};
