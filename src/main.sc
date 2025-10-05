require: slotfilling/slotFilling.sc
  module = sys.zb-common
require: city/city.sc
  module = sys.zb-common
require: functions.js

theme: /

    state: Start
        q!: $regex</start>
        intent!: /hi
        script:
            $client.name = $request.userFrom.firstName
        if: $client.name
            random:
                a: {{ $client.name }}, здравствуйте! Артур из Just Tour на связи. Рад снова видеть вас в чате!
                a: {{ $client.name }}, приветствую! На связи Артур из Just Tour, лучшей в мире туристической компании. Рад снова пообщаться с вами!
        else:
            random:
                a: Здравствуйте! Меня зовут Артур, бот-помощник компании Just Tour. Расскажу все о погоде в городах мира.
                a: Приветствую вас! Я Артур, работаю виртуальным ассистентом в Just Tour, лучшем туристическом агентстве. Проинформирую вас о погоде в разных городах.
        go!: /GetCity

    state: WeatherForecast
        intent!: /weather_forecast
        if: $parseTree._date && $parseTree._city
            script:
                $session.dateFlagWeek = checkWeekDate($request.query)   //флаг true если запрос прогноза на неделю
                $session.date = getDateForRequest($parseTree._date.value).startDate
                $session.onlyDate = getDateForRequest($parseTree._date.value).onlyDateStart
                $session.onlyDateFinal = getDateForRequest($parseTree._date.value).onlyDateFinal
                $session.dateFinalWeek = $session.dateFlagWeek? getDateForRequest($parseTree._date.value).finalDate : null
                $session.city = $caila.inflect($parseTree._city, ["nomn"])
               
            go!: /CheckDate
        elseif: $parseTree._date && !$parseTree._city
            script:
                 $session.date = $parseTree._date.value
                 $session.dateFlagWeek = checkWeekDate($request.query)
            go!: /GetCity
        elseif: !$parseTree._date && $parseTree._city
            script:
                $session.city = $caila.inflect($parseTree._city, ["nomn"])
                $session.lat = getGeoPosition($session.city).lat
                $session.lon = getGeoPosition($session.city).lon
            go!: /GetDate
        else:
            go!: /GetCity
            
    state: GetCity 
        random:
            a: Укажите, пожалуйста, название города, для которого хотите узнать прогноз погоды.
            a: Скажите, пожалуйста, для какого города вы хотите получить прогноз?
            a: Прогноз для какого города хотите получить?
        timeout: /StopSession || interval = "1 minutes"
            
        state: UserCity
            intent: /city
            script:
                $session.date = $parseTree._date? $parseTree._date.value : null
                $session.city = $parseTree._city? $parseTree._city : null;
            if: !$parseTree._city
                go!: /GetCity 
            if: $session.date
                script:
                    $session.dateFlagWeek = checkWeekDate($request.query)   //флаг true если запрос прогноза на неделю
                    $session.date = getDateForRequest($parseTree._date.value).startDate
                    $session.onlyDate = getDateForRequest($parseTree._date.value).onlyDateStart
                    $session.onlyDateFinal = getDateForRequest($parseTree._date.value).onlyDateFinal
                    $session.dateFinalWeek = $session.dateFlagWeek? getDateForRequest($parseTree._date.value).finalDate : null
                go!: /CheckDate
            else:
                go!: /GetDate
         
        state: CatchAll || noContext = true
            event: noMatch
            if: $context.session.lastState !== $context.currentState
                script:
                    $session.stateCounterInARow = 1;
            else:
                script:
                    $session.stateCounterInARow++;
            if: ($session.stateCounterInARow && $session.stateCounterInARow < 3)    
                random:
                    a: Извините, не совсем понял вас. Напишите, пожалуйста, название города, чтобы я смог узнать прогноз погоды для него.
                    a: К сожалению, не понял вас. Укажите, пожалуйста, нужный вам город.
            else:
                a: Простите! Кажется, я пока не умею узнавать прогноз погоды с такими параметрами, но постараюсь поскорее научиться.
                script:
                    resetAllSessionData($session)
                go!: /SomethingElse    
            timeout: /StopSession || interval = "1 minutes"
    
    state: GetDate
        random:
            a: На какую дату требуется прогноз?
            a: Прогноз погоды на какую дату вам нужен?
        timeout: /StopSession || interval = "1 minutes"
        
        state: UserDate
            intent: /date
            script:
                $session.dateFlagWeek = checkWeekDate($request.query)   //флаг true если запрос прогноза на неделю
                $session.date = getDateForRequest($parseTree._date.value).startDate
                $session.onlyDate = getDateForRequest($parseTree._date.value).onlyDateStart
                $session.onlyDateFinal = getDateForRequest($parseTree._date.value).onlyDateFinal
                $session.dateFinalWeek = $session.dateFlagWeek? getDateForRequest($parseTree._date.value).finalDate : null
            go!: /CheckDate
            
        state: CatchAll || noContext = true
            event: noMatch
            script:
                $session.stateCounterInARow = $session.stateCounterInARow? $session.stateCounterInARow++ : 1
            if: ($session.stateCounterInARow && $session.stateCounterInARow < 3) 
                random:
                    a: Извините, не совсем понял вас. Напишите, пожалуйста, нужную вам дату.
                    a: К сожалению, не понял вас. Введите, пожалуйста, дату, которая вам нужна.
            else:
                script:
                    resetAllSessionData($session)
                a: Простите! Кажется, я пока не умею узнавать прогноз погоды с такими параметрами, но постараюсь поскорее научиться.
                go!: /SomethingElse
            timeout: /StopSession || interval = "1 minutes"
    
    state: CheckDate
        if: checkDate($session.date).past
            go!: /ThisDayHasPassed
        elseif: checkDate($session.date).away
            go!: /ThisDayIsNotComingSoon
        else:
            go!: /TellWeather
    
    state: ThisDayHasPassed
        script:
            resetDateData ($session)
            $session.stateCounter = $session.stateCounter? $session.stateCounter++ : 1
        if: ($session.stateCounter < 3)
            random:
                a: К сожалению, я не могу узнать прогноз погоды на период времени в прошлом.
                a: Я не смогу посмотреть прогноз для прошедшего периода.
            go!: /GetDate
        else:
            script:
                resetAllSessionData($session)
            a: Простите! Кажется, я пока не умею узнавать прогноз погоды с такими параметрами, но постараюсь поскорее научиться.
            go!: /SomethingElse
        timeout: /StopSession || interval = "1 minutes"
            
    state: ThisDayIsNotComingSoon
        script:
             resetDateData ($session)
        if: $session.stateCounter
            script:
                $session.stateCounter++
        else:
            script:
                $session.stateCounter = 1
        if: ($session.stateCounter < 3)
            random:
                a: Мне жаль, но метеорологи и я пока не можем дать такие долгосрочные прогнозы.
                a: Извините, посмотреть прогноз на такую далекую дату я не смогу.
            go!: /GetDate
        else:
            script:
                resetAllSessionData($session)
            a: Простите! Кажется, я пока не умею узнавать прогноз погоды с такими параметрами, но постараюсь поскорее научиться.
            go!: /SomethingElse
        timeout: /StopSession || interval = "1 minutes"
    
    state: TellWeather
        script:
            $session.lat = getGeoPosition($session.city).lat;
            $session.lon = getGeoPosition($session.city).lon;
            $temp.temperatureMax = getWeather ($session.lat, $session.lon, $session.date, $session.dateFinalWeek).maxT
            $temp.temperatureMin = getWeather ($session.lat, $session.lon, $session.date, $session.dateFinalWeek).minT
        if: getWeather.err || getGeoPosition.err || !$temp.temperatureMax || !$temp.temperatureMin
            script:
                $session.stateCounter = $session.stateCounter? $session.stateCounter : 0
                log(getGeoPosition.textErr || getWeather.err)
            go!: ./Error
            
        if: !$session.dateFlagWeek //прогноз на 1 день иначе на 1 неделю
            random:
                a: У меня получилось уточнить: на {{ $session.onlyDate }} в {{capitalize($nlp.inflect($session.city, "loct"))}} температура воздуха составит от {{ $temp.temperatureMin }} до {{ $temp.temperatureMax }} {{$nlp.conform("градусов", $temp.temperatureMax)}} по Цельсию.
                a: Смог узнать для вас прогноз: на {{ $session.onlyDate }} в {{capitalize($nlp.inflect($session.city, "loct"))}} будет от {{ $temp.temperatureMin }} до {{ $temp.temperatureMax }} {{$nlp.conform("градусов", $temp.temperatureMax)}} по Цельсию.
        else:                   
            random: 
                a: У меня получилось уточнить: на неделю с {{ $session.onlyDate }} по {{$session.onlyDateFinal}} в {{capitalize($nlp.inflect($session.city, "loct"))}} температура воздуха составит от {{ $temp.temperatureMin }} до {{ $temp.temperatureMax }} {{$nlp.conform("градусов", $temp.temperatureMax)}} по Цельсию.
                a: Смог узнать для вас прогноз: на неделю с {{ $session.onlyDate }} по {{$session.onlyDateFinal}} в {{capitalize($nlp.inflect($session.city, "loct"))}} будет от {{ $temp.temperatureMin }} до {{ $temp.temperatureMax }} {{$nlp.conform("градусов", $temp.temperatureMax)}} по Цельсию.
        script:
            resetAllSessionData($session)
        go!: /SomethingElse
            
        state: Error
            script:
                $session.stateCounter = $session.stateCounter? $session.stateCounter++ : 1
            if: ($session.stateCounter < 3)
                go!: /TellWeather
            else:
                script:
                   resetAllSessionData($session)
                a: Мне очень жаль, но при обращении к сервису, содержащему сведения о погоде, произошла ошибка. Пожалуйста, попробуйте написать мне немного позже. Надеюсь, работоспособность сервиса восстановится.
                go!: /SomethingElse
    
            
    state: SomethingElse
        random:
            a: Хотите спросить что-то еще?
            a: Могу ли я помочь чем-то еще?
            a: Подскажите, у вас остались еще вопросы?
        script:
            $response.replies = $response.replies || [];
            $response.replies.push({
                "type": "buttons",
                "buttons": [
                    {"text": "Узнать прогноз погоды"}
                ]
            });
        timeout: /StopSession || interval = "1 minutes"
            
        
    
    
    
    state: StopSession
        script:
            $jsapi.stopSession();
        
    state: Match
        event!: match
        a: {{$context.intent.answer}}