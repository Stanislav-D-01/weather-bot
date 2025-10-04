
bind(
        "postProcess",
        function($context) {
            $context.session.lastState = $context.currentState;
        },
        "/",
        "Remember last state",
        false
    );
    
//функция получения погоды по долготе и широте и дате
function getWeather (lat, lon, dateS, dateF) {
    var dateRegex = /^\d{4}\-\d{2}\-\d{2}T\d{2}:\d{2}/;
    var res = {};
    var resWeather = $http.get("https://api.open-meteo.com/v1/forecast?latitude=${lat}&longitude=${lon}&hourly=temperature_2m&timezone=Europe%2FMoscow&forecast_days=16&wind_speed_unit=ms", {
        timeout: 10000,
        query:{
            lat: lat,
            lon: lon
        }
    });
    
    if ((dateS && !dateRegex.test(dateS)) || (dateF && !dateRegex.test(dateF)))
        return 'error date'
    
    if (resWeather.isOk) {
        var indexWeatherStart = resWeather.data.hourly.time.indexOf(dateS);
        var indexWeatherFinal = dateF ? resWeather.data.hourly.time.indexOf(dateF) : indexWeatherStart+23
        var minT = 99
        var maxT = -99
        
        if (indexWeatherStart !== -1) {
            var i = indexWeatherStart;
            while (i<=indexWeatherFinal){
                if (resWeather.data.hourly.temperature_2m[i] < minT) minT = resWeather.data.hourly.temperature_2m[i];
                if (resWeather.data.hourly.temperature_2m[i] > maxT) maxT = resWeather.data.hourly.temperature_2m[i];
                i++;
            }
            
            return res = {
                minT: Math.round(minT),
                maxT: Math.round(maxT),
            }
            
        }
        else{
            return 'error'
        }
    }
    else {
        return 'error server'
    }
}

    
function getGeoPosition (city) {
    var position = {};
    var res = $http.get("https://geocoding-api.open-meteo.com/v1/search?name=${city}&count=10&language=ru&format=json", {
        timeout: 10000,
        query:{
            city: city
        },
        dataType: "json",
    });
    
    if (res.isOk) {
        return position = {
            lat: res.data.results[0].latitude,
            lon: res.data.results[0].longitude,
            err: false
            }
    }
    else {
        return position = {
            err: true,
        }
    }
    return position
}

function getDateForRequest (date) {
    var dateWithotTime = date.match(/^\d{4}\-\d{2}\-\d{2}/g)[0];
    var dates = {};
    var startDate = new Date(dateWithotTime);
    var tempDate = startDate.getDate();
    var finalDate = new Date(date);
    finalDate.setDate(tempDate + 7);
    startDate.setUTCHours(0, 0, 0, 0);
    finalDate.setUTCHours(23, 0, 0, 0);
    var onlyDateStart = startDate.getDate()+'.'+(startDate.getMonth()+1)+'.'+startDate.getFullYear();
    var onlyDateFinal = finalDate.getDate()+'.'+(finalDate.getMonth()+1)+'.'+finalDate.getFullYear();

    return dates = {
        startDate: startDate.toISOString().match(/^\d{4}\-\d{2}\-\d{2}T\d{2}:\d{2}/g)[0],
        finalDate: finalDate.toISOString().match(/^\d{4}\-\d{2}\-\d{2}T\d{2}:\d{2}/g)[0],
        onlyDateStart: onlyDateStart,
        onlyDateFinal: onlyDateFinal
    }
}    



function checkWeekDate (query) {
    var regWeek = /^(?!.*((\d\d?)|(понедельник.?|вторник.?|сред[уа]|четверг.?|пятниц[уе]|cубб?от[ау]|воскресень[еяю]|через))).*(недел[юеи]).*/gmi;
    var entityWeek = regWeek.test(query);
   
    if (entityWeek){
       return true 
    }
    else {
        return false
    }
    
}

function checkDate(date){
    var today = new Date();
    var dateVar = new Date(date);
    today.setUTCHours(0, 0, 0, 0);
    dateVar.setUTCHours(0, 0, 0, 0);
    dateVar = dateVar.toISOString();
    
    var finalDate = new Date(today);
    var currentDate = today.getDate();
    var res = {};
    
    finalDate.setDate(currentDate + 7);
    finalDate = finalDate.toISOString();
    today = today.toISOString();
    
    if (finalDate < dateVar) {
        return res = {
            past: false,
            away: true,
        }
    } 
    else if (dateVar < today) {
        return res = {
            past: true,
            away: false,
        }
    }
    else {
        return res = {
            past: false,
            away: false,
        }
    }
        
  }
   

