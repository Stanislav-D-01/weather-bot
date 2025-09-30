require: slotfilling/slotFilling.sc
require: city/city.sc

  module = sys.zb-common
theme: /

    state: Start
        q!: $regex<1>
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


    state: Match
        event!: noMatch
        a: {{$context.intent.answer}}