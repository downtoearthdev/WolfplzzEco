# WolfplzzEco
**Commands:**
- /buycommand <command name\> *wolfplzzeco.buy*
- /rentcommand <command name\> *wolfplzzeco.rent*
- /listpaidcommands *wolfplzzeco.list*
- /reloadwolfplzzeco *wolfplzzeco.reload*

**Configuration:**

~~~
join-commands: //Commands that execute on join
  someplayer:
    run-as-player: "commandname arg1 arg2" //One or the other here
    run-as-server: "commandname arg1 arg2"
    wait-time: 20 //How many seconds after join will this occur?
pay-commands: //All rent and buy commands
  commandalias: //Name of command
    type: "rent" //Type can be rent, buy, or buyonce
    price: 100.00 
    run-as-player: "commandname {player}" //One or the other
    run-as-server: "commandname arg1"
    time: 5 // How many minutes will they get this?
    warning: 2 //How many minutes left will we warn them?
    expire-as-player: "uncommandname" //One or the other, the off command
    expire-as-server: "uncommandname {player}"
  anothercommand:
    type: "buy"
    price: 250.00
    permission: "some.permission" //Permission to be applied if the buy a command forever
  yetanotherbloodycommand:
    type: "buyonce" //Only executes once
    price: 9000.00
    run-as-player: "commandname arg1 arg2"
    run-as-server: "commandname {player} arg1 arg2"
rent-expire-message: "{command} will expire in {timeleft}"
~~~