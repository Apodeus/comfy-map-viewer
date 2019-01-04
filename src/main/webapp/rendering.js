function genericRender(style, height){
    const waterlevelCap = 0;
    const beachlevelCap = 15;
    const mountainlevelCap = 80;
    const highmountainlevelCap = 2500;
    const snowmountainlevelCap = 5000;
    let lowCap = waterlevelCap;
    let topCap = beachlevelCap;
    
    let currentStyle = style.water;

    if (height > waterlevelCap && height < beachlevelCap){
        lowCap = waterlevelCap;
        topCap = beachlevelCap;
        currentStyle = style.beach;
    } else if (height > beachlevelCap && height < mountainlevelCap) {
        lowCap = beachlevelCap;
        topCap = mountainlevelCap;
        currentStyle = style.mountain;
    }else if (height > mountainlevelCap && height < highmountainlevelCap){
        lowCap = mountainlevelCap;
        topCap = highmountainlevelCap;
        currentStyle = style.highMountain;
    }else if (height > highmountainlevelCap && height < snowmountainlevelCap) {
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
    }
