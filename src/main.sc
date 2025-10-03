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
                $session.dateFlagWeek = checkWeekDate($request.query)
                $session.date = $parseTree._date.value
                $session.city = $caila.inflect($parseTree._city, ["nomn"])
                $session.lat = getGeoPosition($session.city).lat
                $session.lon = getGeoPosition($session.city).lon
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
                $session.date = $parseTree._date && $parseTree._date.value || null
            if: $session.date
                script:
                    $session.dateFlagWeek = checkWeekDate($request.query)
                go!: /CheckDate
            else:
                go!: /GetDate
         
        state: CatchAll noContext = true
            event: noMatch
            if: $context.session.lastState !== $context.currentState
                script:
                    $session.stateCounterInARow = 1;
            else:
                script:
                    $session.stateCounterInARow = $session.stateCounterInARow + 1;
            if: ($session.stateCounterInARow && $session.stateCounterInARow < 3)    
                random:
                    a: Извините, не совсем понял вас. Напишите, пожалуйста, название города, чтобы я смог узнать прогноз погоды для него.
                    a: К сожалению, не понял вас. Укажите, пожалуйста, нужный вам город.
            else:
                a: Простите! Кажется, я пока не умею узнавать прогноз погоды с такими параметрами, но постараюсь поскорее научиться.
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
                $session.dateFlagWeek = checkWeekDate($request.query)
                $session.date = $parseTree._date.value 
            go!: /CheckDate
    
    state: CheckDate
        if: checkDate($session.date).past
            a: прошлое
        elseif: checkDate($session.date).away
            a: далеко
        else:
            a: норм
        
        timeout: /StopSession || interval = "1 minutes"
    
            
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