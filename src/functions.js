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
function getWeather (lat, long, date) {
    var dateRegex = /^\d{4}\-\d{2}\-\d{2}T\d{2}:\d{2}$/
    var weather = $http.get("https://api.open-meteo.com/v1/forecast?latitude=${lat}&longitude=${long}&hourly=temperature_2m", {
        timeout: 10000,
        query:{
            lat: lat,
            long: long
        }
    });
    
    if (dateRegex.test(date)){
        var indexWeather = weather.data.hourly.time.indexOf(date) 
    
        if (indexWeather !== -1) {
            return weather.data.hourly.temperature_2m[indexWeather]}
        else{
            return 'error response'
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
            long: res.data.results[0].longitude,
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

