# SBT-Test

### Варианты запуска:

  - gradle :Server:run <br>
    gradle :Client:run <br>
    В данном случае сервер будет запущен на порту 9999, а клиент будет пытаться подключиться на localhost:9999.
    
  - gradle :Server:run -Dexec.args="%port%" <br>
    gradle :Client:run -Dexec.args="%host% %port%" <br>
    %port% - порт на котором будет запущен сервер, %host% - хост, к которому будет пытаться установить подключение клиент.
    
  - собрать проект gradle clean build <br>
    запустить сервер java -jar Server-1.0-SNAPSHOT.jar <br>
    запустить клиент java -jar Client-1.0-SNAPSHOT.jar <br>
    В данном случае сервер будет запущен на порту 9999, а клиент будет пытаться подключиться на localhost:9999.
      
  - собрать проект gradle clean build <br>
    запустить сервер java -jar Server-1.0-SNAPSHOT.jar %port% <br>
    запустить клиент java -jar Client-1.0-SNAPSHOT.jar %host% %port% <br>
    %port% - порт на котором будет запущен сервер, %host% - хост, к которому будет пытаться установить подключение клиент.
    
  - запустить main классы <br>
    запустить сервер java org.astanis.sbttest.ServerStarter <br>
    запустить клиент java org.astanis.sbttest.ClientStarter <br>
    Для работы потребуется org.apache.log4j <br>
    В данном случае сервер будет запущен на порту 9999, а клиент будет пытаться подключиться на localhost:9999.
    
  - запустить main классы <br>
    запустить сервер java org.astanis.sbttest.ServerStarter %port% <br>
    запустить клиент java org.astanis.sbttest.ClientStarter %host% %port% <br>
    Для работы потребуется org.apache.log4j <br>
    %port% - порт на котором будет запущен сервер, %host% - хост, к которому будет пытаться установить подключение клиент.
