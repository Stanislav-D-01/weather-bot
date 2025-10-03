
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
function getWeather (lat, lon, date, nowDate) {
    var dateRegex = /^\d{4}\-\d{2}\-\d{2}T\d{2}:\d{2}/;
    var weather = $http.get("https://api.open-meteo.com/v1/forecast?latitude=${lat}&longitude=${lon}&hourly=temperature_2m&timezone=Europe%2FMoscow&forecast_days=16&wind_speed_unit=ms", {
        timeout: 10000,
        query:{
            lat: lat,
            lon: lon
        }
    });
    
   
    
    if (dateRegex.test(date)){
        var res = {};
        var onlyDate = date.slice([8], [10])+"."+date.slice([5], [7])+"."+date.slice([0], [4])
        var dateStartDay = date.match(/^\d{4}\-\d{2}\-\d{2}T/g)[0]+'00:00';
        var dateStartDay = date.match(/^\d{4}\-\d{2}\-\d{2}T/g)[0]+'23:00';
        var indexWeather = weather.data.hourly.time.indexOf(dateStartDay) 
        var minT = 99
        var maxT = -99
       
        if (indexWeather !== -1) {
            for (var i = 0; i < 24; i++) {
                if (weather.data.hourly.temperature_2m[indexWeather+i] < minT) minT = weather.data.hourly.temperature_2m[indexWeather+i];
                if (weather.data.hourly.temperature_2m[indexWeather+i] > maxT) maxT = weather.data.hourly.temperature_2m[indexWeather+i];
            }
            return res = {
                minT: minT,
                maxT: maxT,
                date: onlyDate
            }
        }
        else{
            return 'слишком далеко'
        }
    }
    else {
        return 'error date'
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
        position = {
            lat: res.data.results[0].latitude,
            lon: res.data.results[0].longitude,
            err: false
        }
    }
    else {
        position = {
            err: true,
        }
    }
    return position
}

function checkWeekDate (query) {
    var regWeek = /^(?!.*((\d\d?)|(понедельник.?|вторник.?|сред[уа]|четверг.?|пятниц[уе]|cубб?от[ау]|воскресень[еяю]))).*(недел[юеи]).*/gmi;
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
    
    
    log("переданная дата"+dateVar);
    log("сегодня"+today);
    log("финал"+finalDate);
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
   
  
  
