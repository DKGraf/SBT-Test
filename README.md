# SBT-Test

Варианты запуска:
  - gradle :Server:run
    gradle :Client:run
    В данном случае сервер будет запущен на порту 9999, а клиент будет пытаться подключиться на localhost:9999.
    
  - gradle :Server:run -Dexec.args="%port%"
    gradle :Client:run -Dexec.args="%host% %port%"
    %port% - порт на котором будет запущен сервер, %host% - хост, к которому будет пытаться установить подключение клиент.
    
  - собрать проект gradle clean build
    запустить сервер java -jar Server-1.0-SNAPSHOT.jar
    запустить клиент java -jar Client-1.0-SNAPSHOT.jar
    В данном случае сервер будет запущен на порту 9999, а клиент будет пытаться подключиться на localhost:9999.
      
  - собрать проект gradle clean build
    запустить сервер java -jar Server-1.0-SNAPSHOT.jar %port%
    запустить клиент java -jar Client-1.0-SNAPSHOT.jar %host% %port%
    %port% - порт на котором будет запущен сервер, %host% - хост, к которому будет пытаться установить подключение клиент.
    
  - запустить main классы
    запустить сервер java org.astanis.sbttest.ServerStarter
    запустить клиент java org.astanis.sbttest.ClientStarter
    Для работы потребуется org.apache.log4j
    В данном случае сервер будет запущен на порту 9999, а клиент будет пытаться подключиться на localhost:9999.
    
  - запустить main классы
    запустить сервер java org.astanis.sbttest.ServerStarter %port%
    запустить клиент java org.astanis.sbttest.ClientStarter %host% %port%
    Для работы потребуется org.apache.log4j
    %port% - порт на котором будет запущен сервер, %host% - хост, к которому будет пытаться установить подключение клиент.
