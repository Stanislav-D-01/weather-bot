require: slotfilling/slotFilling.sc
  module = sys.zb-common
require: city/city.sc
  module = sys.zb-common
require: functions.js

theme: /

    state: Start
        q!: $regex</start>
        intent!: /hi
        if: $request.userFrom
            script:
                $client.name = ($request.userFrom.firstName && $request.userFrom && $request.userFrom.firstName) ? $request.userFrom.firstName : null
        else: 
            script:
                $client.name = null
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
        if: $session.city
            go!:/CheckDate
        else:
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
        if: $session.date
            go!: /CheckDate
        else:
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
            if: $session.stateCounterInARow
                script:                
                    $session.stateCounterInARow++
            else:
                script:
                    $session.stateCounterInARow = 1
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
        if: $session.stateCounter 
            script:
                $session.stateCounter++
        else:
            script:
                $session.stateCounter = 1
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
            $session.resCity = getGeoPosition($session.city)
            $session.lat = $session.resCity.lat;
            $session.lon = $session.resCity.lon;
            $session.resWeather = getWeather ($session.lat, $session.lon, $session.date, $session.dateFinalWeek)
            $temp.temperatureMax = $session.resWeather.maxT;
            $temp.temperatureMin =$session.resWeather.minT;
        if: $session.resCity.err == true || $session.resWeather.err == true || !$temp.temperatureMax || !$temp.temperatureMin
            go!: ./Error
            
        elseif: !$session.dateFlagWeek //прогноз на 1 день иначе на 1 неделю
            random:
                a: У меня получилось уточнить: на {{ $session.onlyDate }} в {{capitalize($nlp.inflect($session.city, "loct"))}} температура воздуха составит от {{ $temp.temperatureMin }} до {{ $temp.temperatureMax }} {{$nlp.conform("градусов", $temp.temperatureMax)}} по Цельсию.
                a: Смог узнать для вас прогноз: на {{ $session.onlyDate }} в {{capitalize($nlp.inflect($session.city, "loct"))}} будет от {{ $temp.temperatureMin }} до {{ $temp.temperatureMax }} {{$nlp.conform("градусов", $temp.temperatureMax)}} по Цельсию.
        else:                   
            random: 
                a: У меня получилось уточнить: на неделю с {{ $session.onlyDate }} по {{$session.onlyDateFinal}} в {{capitalize($nlp.inflect($session.city, "loct"))}} температура воздуха составит от {{ $temp.temperatureMin }} до {{ $temp.temperatureMax }} {{$nlp.conform("градусов", $temp.temperatureMax)}} по Цельсию.
                a: Смог узнать для вас прогноз: на неделю с {{ $session.onlyDate }} по {{$session.onlyDateFinal}} в {{capitalize($nlp.inflect($session.city, "loct"))}} будет от {{ $temp.temperatureMin }} до {{ $temp.temperatureMax }} {{$nlp.conform("градусов", $temp.temperatureMax)}} по Цельсию.
        script:
        go!: /SomethingElse
            
        state: Error
            if: $session.stateCounter
                script:
                 $session.stateCounter++;
            else:
                script:
                    $session.stateCounter = 1;
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
            
        state: AnotherOne
            intent: /weather_forecast
            script:
                
            if: $parseTree._date  && $parseTree._city
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
                    $session.dateFlagWeek = checkWeekDate($request.query)   //флаг true если запрос прогноза на неделю
                    $session.date = getDateForRequest($parseTree._date.value).startDate
                    $session.onlyDate = getDateForRequest($parseTree._date.value).onlyDateStart
                    $session.onlyDateFinal = getDateForRequest($parseTree._date.value).onlyDateFinal
                    $session.dateFinalWeek = $session.dateFlagWeek? getDateForRequest($parseTree._date.value).finalDate : null
                go!: /GetCity
            elseif: !$parseTree._date && $parseTree._city
                script:
                    $session.city = $caila.inflect($parseTree._city, ["nomn"])
                go!: /GetDate
            else:
                script:
                    resetAllSessionData($session);
                go!: /GetCity
                
        state: Agree
            q: $regexp_i<\b(да|угу|ага|наверное|возможно)\b.*>
            script:
                resetAllSessionData($session);
            go!: /GetCity
                
        state: DontHaveQuestions
            q: $regexp_i<\b(нет|спасибо|не)\b.*>
            intent!: /dont_have_questions
            random:
                a: Вас понял!
                a: Хорошо!
                a: Понял!
            go!: /Goodbye
                
        state: CatchAll
            event: noMatch
            if: $context.session.lastState !== $context.currentState
                script:
                    $session.stateCounterInARow = 1;
            else:
                script:
                    $session.stateCounterInARow++;
            if: ($session.stateCounterInARow && $session.stateCounterInARow < 3)    
                random:
                    a: Извините, не совсем понял. Пожалуйста, подскажите, могу ли я еще чем-то помочь?
                    a: К сожалению, не смог понять, что вы имеете в виду. Подскажите, остались ли у вас еще вопросы?
                script:
                    $response.replies = $response.replies || [];
                    $response.replies.push({
                        "type": "buttons",
                        "buttons": [
                        {"text": "Узнать прогноз погоды"}
                        ]
                    });
            else:
                a: Простите, так и не смог понять, что вы имели в виду.
                go!: /Goodbye
    
    state: Goodbye
        intent!: /bye
        random:
            a: Всего доброго!
            a: Всего вам доброго!
            a: Всего доброго, до свидания!
        go!: /StopSession
                
            
    state: AreYouRobot
        intent!: /who_are_you
        random:
            a: Я Артур — бот-помощник компании Just Tour, всегда готов отвечать на ваши вопросы.
            a: Вы общаетесь с Артуром — чат-ботом, разработанным командой Just Tour, чтобы помогать вам. Всегда рад пообщаться с вами!
        go!: /SomethingElse
    
    state: WhatCanYouDo
        intent!: /what_can_you_do
        random:
            a: Умею рассказывать о погоде в городах мира на ближайшие дни.
            a: С удовольствием расскажу вам о ближайших метеопрогнозах для разных городов.
        go!: /SomethingElse
    
    
    state: GlobalCatchAll || noContext = true
        event!: noMatch
        if: $session.stateCounterInARow
            script:
                $session.stateCounterInARow++
        else:
            script:
                $session.stateCounterInARow = 1
        if: ($session.stateCounterInARow && $session.stateCounterInARow < 3)
            random:
                a: Прошу прощения, не совсем вас понял. Попробуйте, пожалуйста, переформулировать ваш вопрос.
                a: Простите, не совсем понял. Что именно вас интересует?
                a: Простите, не получилось вас понять. Переформулируйте, пожалуйста.
                a: Не совсем понял вас. Пожалуйста, попробуйте задать вопрос по-другому.
        else:
            a: Кажется, этот вопрос не в моей компетенции. Но я постоянно учусь новому, и, надеюсь, совсем скоро научусь отвечать и на него.
            go!: /SomethingElse
    
    
    
    state: StopSession
        script:
            $jsapi.stopSession();
        
    state: AnyError
        event!: match
        # a: {{$context.intent.answer}}
        random:
            a: Извините, произошла техническая ошибка. Специалисты обязательно изучат ее и возьмут в работу. Пожалуйста, напишите в чат позже.
            a: Простите, произошла ошибка в системе. Наши специалисты обязательно ее исправят. Пожалуйста, напишите мне позже.
        script:
                    $response.replies = $response.replies || [];
                    $response.replies.push({
                        "type": "buttons",
                        "buttons": [
                        {"text": "Узнать прогноз погоды"}
                        ]
                    });