require: slotfilling/slotFilling.sc
  module = sys.zb-common
require: city/city.sc
  module = sys.zb-common
theme: /

    state: Start
        q!: $regex</start>
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

    state: GetCity
        random:
            a: Укажите, пожалуйста, название города, для которого хотите узнать прогноз погоды.
            a: Скажите, пожалуйста, для какого города вы хотите получить прогноз?
            a: Прогноз для какого города хотите получить?
            
        state: UserCity
            intent!: /сity
            a: fdsafsa

        state: CatchAll
            event!: noMatch
            script:
                $session.stateCounterInARow ? $session.stateCounterInARow+1 : 0
            if: ($session.stateCounterInARow < 3)
                random:
                    a: Извините, не совсем понял вас. Напишите, пожалуйста, название города, чтобы я смог узнать прогноз погоды для него.
                    a: К сожалению, не понял вас. Укажите, пожалуйста, нужный вам город.
            else:
                a: Простите! Кажется, я пока не умею узнавать прогноз погоды с такими параметрами, но постараюсь поскорее научиться.
        
   
    state: Match
        event!: match
        a: {{$context.intent.answer}}